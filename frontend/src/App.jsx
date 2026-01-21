import React, {useCallback, useEffect, useState} from 'react';
import {BrowserRouter as Router, Link, Navigate, Route, Routes, useLocation} from 'react-router-dom';
import styled from '@emotion/styled';
import api from './utils/api';
import DuoFinder from './components/duoFinder/DuoFinder'
import Login from './components/auth/Login';
import Join from './components/auth/Join';
import {useMatchingSystem} from "./hooks/useMatchingSystem.js";
import {MatchingButtonPanel} from "./components/duoFinder/matching/MatchingButtonPanel.jsx";
import {useWebSocket} from './hooks/useWebSocket';
import DuoTimeline from "./components/timeline/DuoTimeline.jsx";
import ReviewModal from './components/review/ReviewModal';
import {STORAGE_KEYS} from './utils/constants';
import {useStateManager} from "./hooks/useStateManager.js";
import {getInitialCriteria} from "./utils/initialStates.js";

// 로그인 상태에 따라 리다이렉트하는 보호된 라우트 컴포넌트
const ProtectedRoute = ({element, isLoggedIn}) => {
    const location = useLocation();

    // 현재 위치가 /login 페이지이면서 이미 로그인 상태라면 홈으로 리다이렉트
    if (location.pathname === '/login' && isLoggedIn) {
        return <Navigate to="/" replace/>;
    }
    // 로그인이 필요한 페이지이고 로그인되지 않은 경우 로그인 페이지로 리다이렉트
    if (!isLoggedIn) {
        return <Navigate to="/login" state={{from: location}} replace/>;
    }
    // 로그인 상태이면 요청한 페이지 렌더링
    return element;
};

const App = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userInfo, setUserInfo] = useState({
        riotUsername: '',
        riotTag: ''
    });
    const [memberDataBundle, setMemberDataBundle] = useState(null);
    const [userDataLoading, setUserDataLoading] = useState(true);
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    // 채팅 관련 상태
    const [chatMessages, setChatMessages] = useState([]);
    const [currentChatRoomId, setCurrentChatRoomId] = useState(null);

    // 리뷰 모달 관련 상태
    const [reviewModalVisible, setReviewModalVisible] = useState(false);
    const [reviewTargetUsername, setReviewTargetUsername] = useState('');
    const [reviewMatchId, setReviewMatchId] = useState('');

    const [penaltyTime, setPenaltyTime] = useState(0);
    const [isPenaltyActive, setIsPenaltyActive] = useState(false);

    // 페널티 타이머 작동
    useEffect(() => {
        if (!isPenaltyActive || penaltyTime <= 0) return;

        const interval = setInterval(() => {
            setPenaltyTime(prev => {
                if (prev <= 1) {
                    clearInterval(interval);
                    setIsPenaltyActive(false);
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);

        return () => clearInterval(interval);
    }, [isPenaltyActive]);

    // WebSocket 관련 핸들러 정의
    const handleSocketMessage = (message) => {

        // 매칭 성공 시 matchResult 업데이트 (서버 응답 구조에 맞게 수정)
        if (message.status === "SUCCESS" && message.data?.chattingRoomId) {
            console.log('매칭 성공! 결과:', message);

            // 서버 응답 구조에 맞게 matchResult 생성
            const matchResult = {
                matched: true,
                chattingRoomId: message.data.chattingRoomId,
                opponentInfo: {
                    riotUsername: message.data.memberInfoDto.riotUsername,
                    riotTag: message.data.memberInfoDto.riotTag,
                    tier: message.data.memberInfoDto.tier,
                    tierNum: message.data.memberInfoDto.tierNum,
                    wins: message.data.memberInfoDto.wins,
                    losses: message.data.memberInfoDto.losses
                },
                data: message.data
            };

            fetchUserInfo();
            setMatchResult(matchResult);
            setCurrentChatRoomId(message.data.chattingRoomId);
            setIsMatching(false);
        } else if (message.cancelled) {
            setIsMatching(false);
            console.log('매칭이 취소되었습니다.');
        } else if (message.error) {
            setIsMatching(false);
            alert('매칭 중 오류가 발생했습니다: ' + message.error);
        }
    };

    const handleChatMessage = (message) => {
        setChatMessages(prev => {
            // 동일한 메시지가 이미 있는지 확인 (timestamp와 sender, message로 판단)
            const isDuplicate = prev.some(existingMsg =>
                existingMsg.timestamp === message.timestamp &&
                existingMsg.sender === message.sender &&
                existingMsg.message === message.message
            );

            if (isDuplicate) {
                return prev; // 기존 배열 그대로 반환
            }

            return [...prev, message];
        });
    };

    const handleSocketConnect = () => {
        console.log('웹소켓 연결 성공');
    };

    const handleSocketDisconnect = () => {
        console.log('웹소켓 연결 해제');
    };

    const handleSocketError = (error) => {
        console.error('웹소켓 에러:', error);
    };

    const handleReviewRequest = (username, matchId) => {
        setReviewTargetUsername(username);
        setReviewMatchId(matchId);
        setReviewModalVisible(true);
    };

    const handlePartnerLeft = useCallback((notification) => {
        if (notification && notification.sender) {
            alert(`${notification.sender}님이 채팅방을 나갔습니다.`);
        }

        // 상태 초기화
        setMatchResult(null);
        setIsMatching(false);
        setChatMessages([]);
        setCurrentChatRoomId(null);
        setMatchingCriteria(getInitialCriteria());
        localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
        localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);
    }, []);

    // useWebSocket 훅 사용 - App에서만 연결 관리
    const {
        socket,
        connect,
        disconnect,
        sendChatMessage,
        sendMatchingRequest,
        sendCancelRequest,
        subscribeToChatRoom,
        isConnected,
        sendLeaveRequest
    } = useWebSocket({
        onMessage: handleSocketMessage,
        onConnect: handleSocketConnect,
        onDisconnect: handleSocketDisconnect,
        onError: handleSocketError,
        onChatMessage: handleChatMessage,
        onReview: handleReviewRequest,
        onPartnerLeft: handlePartnerLeft
    });

    // useMatchingSystem에 소켓 전달
    const {
        matchResult,
        setMatchResult,
        matchingCriteria,
        setMatchingCriteria,
        isMatching,
        setIsMatching,
        handleMatchStart,
        handleMatchCancel,
        resetMatchingState
    } = useMatchingSystem({
        socket,
        sendMatchingRequest,
        sendCancelRequest
    });

    // useStateManager 훅 사용
    const {
        restoreAppState,
        saveMatchingState,
        saveChatMessages,
        performLogoutSteps,
        clearChatState,
    } = useStateManager(
        {
            setIsMatching,
            setMatchResult,
            setMatchingCriteria,
            setCurrentChatRoomId,
            setChatMessages,
            resetMatchingState,
            subscribeToChatRoom
        });

    const handleLeaveChatRoom = useCallback(async () => {
        if (!currentChatRoomId || !userInfo.riotUsername) {
            console.log('채팅방 정보가 없습니다');
            return;
        }

        try {
            const response = await api.get('/cancel/increase');
            const count = response.data?.data ?? 0;

            if (count >= 2) {
                setIsPenaltyActive(true);
                setPenaltyTime(10); // 5분 - 300
            }

            // sendLeaveRequest 사용
            if (sendLeaveRequest) {
                const success = await sendLeaveRequest(currentChatRoomId, userInfo.riotUsername);
                if (success) {
                    console.log('채팅방 나가기 요청 성공');
                }
            } else if (socket && socket.connected) {
                // fallback: sendLeaveRequest가 없으면 직접 전송
                socket.send('/app/chat/leave', {}, JSON.stringify({
                    chattingRoomId: currentChatRoomId,
                    userId: userInfo.riotUsername
                }));
            }
        } catch (error) {
            console.error('채팅방 나가기 처리 중 오류:', error);
        }

        // 로컬 상태 초기화
        setMatchResult(null);
        setIsMatching(false);
        setChatMessages([]);
        setCurrentChatRoomId(null);
        setMatchingCriteria(getInitialCriteria());
        localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
        localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);
    }, [currentChatRoomId, userInfo.riotUsername, sendLeaveRequest, socket, setMatchingCriteria]);

    const handleBackButton = useCallback(() => {
        if (matchResult && currentChatRoomId) {
            // 채팅방이 있는 경우 나가기 처리
            if (window.confirm('채팅방을 나가시겠습니까?\n상대방에게 알림이 전송됩니다.')) {
                handleLeaveChatRoom();
            }
        } else {
            // 매칭 중인 경우 취소만
            handleMatchCancel();
        }
    }, [matchResult, currentChatRoomId, handleLeaveChatRoom, handleMatchCancel]);

    // 앱 시작 시 로컬 스토리지에서 토큰을 확인하여 로그인 상태 유지 - 단순화
    useEffect(() => {
        const initializeApp = async () => {
            const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
            if (token) {
                setIsLoggedIn(true);
                restoreAppState();
                fetchUserInfo();
            } else {
                setUserDataLoading(false);
            }
        };

        initializeApp();
    }, []); // 빈 배열로 변경

    // 로그인 상태 변경 시 웹소켓 연결/해제
    useEffect(() => {
        if (isLoggedIn && !isConnected && !userDataLoading) {
            console.log('로그인 상태 - 웹소켓 연결 시작');
            connect(null, currentChatRoomId);
        } else if (!isLoggedIn && isConnected) {
            console.log('로그아웃 상태 - 웹소켓 연결 해제');
            disconnect();
        }
    }, [isLoggedIn, isConnected, userDataLoading, connect, disconnect, currentChatRoomId]);

    // 매칭 상태 변경 시마다 저장하는 useEffect
    useEffect(() => {
        if (isLoggedIn) {
            saveMatchingState(isMatching, matchResult, matchingCriteria);
        }
    }, [isMatching, matchResult, matchingCriteria, isLoggedIn, saveMatchingState]);

    // 채팅 메시지 변경 시 저장하는 useEffect
    useEffect(() => {
        if (isLoggedIn && chatMessages.length > 0) {
            saveChatMessages(chatMessages);
        }
    }, [chatMessages, isLoggedIn, saveChatMessages]);

    // 토큰을 사용하여 사용자 정보 가져오기 - 에러 처리만 강화
    const fetchUserInfo = async () => {
        setUserDataLoading(true);
        try {
            const memberInfoResponse = await api.get('/auth/memberinfo');

            if (memberInfoResponse.data && memberInfoResponse.data.data) {
                setUserInfo({
                    riotUsername: memberInfoResponse.data.data.riotUsername || '사용자',
                    riotTag: memberInfoResponse.data.data.riotTag || 'KR'
                });

                try {
                    const dataBundleResponse = await api.get('/auth/memberdatabundle');

                    if (dataBundleResponse.data && dataBundleResponse.data.data) {
                        setMemberDataBundle(dataBundleResponse.data.data);
                    }
                } catch (bundleError) {
                    console.error('사용자 데이터 번들 가져오기 실패:', bundleError);
                    // 에러가 발생해도 기본값으로 설정하여 앱이 계속 동작하도록
                    setMemberDataBundle({
                        memberInfoDto: {
                            riotUsername: memberInfoResponse.data.data.riotUsername || '사용자',
                            riotTag: memberInfoResponse.data.data.riotTag || 'KR'
                        },
                        recentTwentyMatchDto: null // null로 설정
                    });
                }
            }
        } catch (error) {
            console.error('사용자 정보 가져오기 실패:', error);
            // 인증 에러시 로그아웃 처리
            if (error.response?.status === 401) {
                handleLogout();
            }
        } finally {
            setUserDataLoading(false);
        }
    };

    useEffect(() => {
        if (userInfo) {
            console.log('userInfo 변경됨:');
        }
    }, [userInfo]);

    // 로그아웃 함수
    const handleLogout = async () => {
        console.log('로그아웃 처리 시작');
        setIsLoggingOut(true);

        try {
            const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
            if (!token) {
                console.warn('토큰이 없습니다. 로컬에서만 로그아웃합니다.');
            } else {
                try {
                    await api.post('/auth/logout');
                } catch (serverError) {
                    console.error('서버 로그아웃 실패 (계속 진행):', serverError);
                }
            }

            resetMatchingState();
            clearChatState();

            await performLogoutSteps(isMatching, handleMatchCancel, isConnected, disconnect);

            setIsLoggedIn(false);
            setUserInfo({riotUsername: '', riotTag: ''});
            setMemberDataBundle(null);
            setUserDataLoading(false);

            delete api.defaults.headers.common['Authorization'];
        } catch (error) {
            console.error('로그아웃 과정에서 오류 발생:', error);
        } finally {
            console.log(' 로그아웃 처리 완료');
            setIsLoggingOut(false);
        }
    };

    return (
        <Router>
            <AppContainer>
                <Header>
                    <a href="/">
                        <Logo>
                            <LogoIcon>
                                <svg viewBox="0 0 24 24">
                                    <path
                                        d="M21.58,16.09l-1.09-7.66C20.21,6.46,18.52,5,16.53,5H7.47C5.48,5,3.79,6.46,3.51,8.43l-1.09,7.66 C2.2,17.63,3.39,19,4.94,19h0c0.68,0,1.32-0.27,1.8-0.75L9,16h6l2.25,2.25c0.48,0.48,1.13,0.75,1.8,0.75h0 C20.61,19,21.8,17.63,21.58,16.09z M11,11H9v2H8v-2H6v-1h2V8h1v2h2V11z M15,10c-0.55,0-1-0.45-1-1c0-0.55,0.45-1,1-1s1,0.45,1,1 C16,9.55,15.55,10,15,10z M17,13c-0.55,0-1-0.45-1-1c0-0.55,0.45-1,1-1s1,0.45,1,1C18,12.55,17.55,13,17,13z"/>
                                </svg>
                            </LogoIcon>
                            <h1>EZGG</h1>
                        </Logo>
                    </a>
                    <UserSection>
                        {isLoggedIn ? (
                            <>
                                <Link to="/timeline">
                                    <UserInfo>
                                        <span>{userInfo.riotUsername} #{userInfo.riotTag}</span>
                                    </UserInfo>
                                </Link>
                                <LogoutButton
                                    onClick={handleLogout}
                                    disabled={isLoggingOut}
                                    style={{opacity: isLoggingOut ? 0.7 : 1}}
                                >
                                    {isLoggingOut ? '로그아웃 중...' : '로그아웃'}
                                </LogoutButton>
                            </>
                        ) : (
                            <LoginButton to="/login">
                                Login
                            </LoginButton>
                        )}
                    </UserSection>
                </Header>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute
                            element={
                                <>
                                    <DuoFinder
                                        memberDataBundle={memberDataBundle}
                                        isLoading={userDataLoading}
                                        userInfo={userInfo}
                                        matchingCriteria={matchingCriteria}
                                        isMatching={isMatching}
                                        setMatchingCriteria={setMatchingCriteria}
                                        matchResult={matchResult}
                                        chatMessages={chatMessages}
                                        sendChatMessage={sendChatMessage}
                                        isConnected={isConnected}
                                    />
                                    <MatchingButtonPanel
                                      matchingCriteria={matchingCriteria}
                                      matchResult={matchResult}
                                      isMatching={isMatching}
                                      penaltyTime={penaltyTime}
                                      isPenaltyActive={isPenaltyActive}
                                      onStart={() => handleMatchStart(matchingCriteria)}
                                      onCancel={handleMatchCancel}
                                      handleBackButton={handleBackButton}
                                    />
                                </>
                            }
                            isLoggedIn={isLoggedIn}
                        />
                    }/>
                    <Route path="/timeline" element={
                        <ProtectedRoute
                            isLoggedIn={isLoggedIn}
                            element={<DuoTimeline memberData={memberDataBundle}/>}
                        />
                    }/>
                    <Route path="/login"
                           element={<Login setIsLoggedIn={setIsLoggedIn} onLoginSuccess={fetchUserInfo}/>}/>
                    <Route path="/join" element={<Join/>}/>
                    <Route path="*" element={<Navigate to="/" replace/>}/>
                </Routes>
                <ReviewModal
                    visible={reviewModalVisible}
                    onClose={() => setReviewModalVisible(false)}
                    targetUsername={reviewTargetUsername}
                    matchId={reviewMatchId}
                />
            </AppContainer>
        </Router>
    );
};

export default App;

const AppContainer = styled.div`
    min-height: 100vh;
    background: #0F0F0F;
`;

const Header = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem 2rem;
    background: rgba(0, 0, 0, 0.3);
    backdrop-filter: blur(10px);
`;

const Logo = styled.div`
    display: flex;
    align-items: center;
    gap: 0.5rem;

    h1 {
        color: white;
        font-size: 1.5rem;
        font-weight: 800;
    }
`;

const LogoIcon = styled.div`
    width: 28px;
    height: 28px;
    background: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    svg {
        width: 18px;
        height: 18px;
        fill: black;
    }
`;

const UserSection = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
`;

const UserInfo = styled.div`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: white;
    cursor: pointer;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.1);
    transition: all 0.2s;
    position: relative;

    &:hover {
        background: rgba(255, 255, 255, 0.2);
    }

    &:after {
        content: 'Timeline';
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        color: white;
        opacity: 0;
        transition: opacity 0.3s ease;
    }

    &:hover:after {
        opacity: 1;
    }

    span {
        transition: opacity 0.3s ease;
    }

    &:hover > span {
        opacity: 0;
    }
`;

const LoginButton = styled(Link)`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: white;
    cursor: pointer;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    background: #FF416C;
    text-decoration: none;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.9;
    }
`;

const LogoutButton = styled.button`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: white;
    cursor: pointer;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    background: #FF416C;
    border: none;
    text-decoration: none;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.9;
    }
`;
