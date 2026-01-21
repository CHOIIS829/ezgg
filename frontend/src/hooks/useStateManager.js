// hooks/useStateManager.js
import {useCallback} from 'react';
import {storageService} from '../services/storageService.js';
import {STORAGE_KEYS, TIMEOUTS} from '../utils/constants.js';
import {getInitialCriteria} from "../utils/initialStates.js";

export const useStateManager = ({
                                    setIsMatching,
                                    setMatchResult,
                                    setMatchingCriteria,
                                    setCurrentChatRoomId,
                                    setChatMessages,
                                    resetMatchingState,
                                    subscribeToChatRoom
                                }) => {

    // 앱 상태 복원 함수
    const restoreAppState = useCallback(async () => {
        console.log('앱 상태 복원 시작');

        try {
            //먼저 채팅 메시지를 복원 (한 번만)
            const savedMessages = storageService.getChatMessages();

            if (savedMessages.length > 0) {
                setChatMessages(savedMessages);
            }

            // 1단계: 매칭 상태 복원 시도
            const savedMatchingState = storageService.getMatchingState();

            if (savedMatchingState) {
                // 매칭 중이었다면 복원
                if (savedMatchingState.isMatching) {
                    setIsMatching(true);

                    // 매칭 조건도 복원
                    if (savedMatchingState.matchingCriteria) {
                        setMatchingCriteria(savedMatchingState.matchingCriteria);
                    }
                }

                // 매칭 결과도 있다면 복원
                if (savedMatchingState.matchResult) {
                    setMatchResult(savedMatchingState.matchResult);
                    setCurrentChatRoomId(savedMatchingState.matchResult.chattingRoomId);
                }

                return; // 매칭 상태가 있으면 여기서 종료
            }

            // 2단계: 매칭 상태가 없을 때만 채팅방 상태 복원
            const savedChatRoom = storageService.getChatRoom();

            if (savedChatRoom) {
                // 매칭 결과 복원 (매칭 완료 상태)
                setMatchResult({
                    matched: true,
                    chattingRoomId: savedChatRoom.chattingRoomId,
                    opponentInfo: savedChatRoom.opponentInfo,
                    data: {chattingRoomId: savedChatRoom.chattingRoomId}
                });
                setCurrentChatRoomId(savedChatRoom.chattingRoomId);
                setIsMatching(false); // 매칭 완료 상태
            }

        } catch (error) {
            console.error('[useStateManager] 상태 복원 실패:', error);
            // 복원 실패 시 손상된 데이터 정리
            storageService.clearAllAppStates();
        }
    }, [
        setIsMatching,
        setMatchResult,
        setMatchingCriteria,
        setCurrentChatRoomId,
        setChatMessages,
        subscribeToChatRoom
    ]);

    // 매칭 상태 저장 함수
    const saveMatchingState = useCallback((isMatching, matchResult, matchingCriteria) => {
        const matchingState = {
            isMatching,
            matchResult,
            matchingCriteria
        };

        storageService.saveMatchingState(matchingState);

        // 매칭 완료 시 별도로 채팅방 정보도 저장
        if (matchResult?.chattingRoomId && !isMatching) {
            storageService.saveChatRoom({
                chattingRoomId: matchResult.chattingRoomId,
                opponentInfo: matchResult.opponentInfo,
                matchedAt: new Date().toISOString()
            });
        }
    }, []);

    // 채팅 메시지 저장 함수
    const saveChatMessages = useCallback((messages) => {
        if (messages.length > 0) {
            storageService.saveChatMessages(messages);
        }
    }, []);

    // 모든 상태 초기화 함수
    const clearAllStates = useCallback(async () => {
        // 저장소 정리
        storageService.clearAllAppStates();
        localStorage.removeItem(STORAGE_KEYS.MATCHING_STATE);

        // React 상태 초기화
        resetMatchingState();
        setChatMessages([]);
        setCurrentChatRoomId(null);
    }, [resetMatchingState, setChatMessages, setCurrentChatRoomId]);

    //채팅방 정리 함수 수정 - STORAGE_KEYS 사용
    const clearChatState = useCallback(() => {
        setMatchResult(null);
        setCurrentChatRoomId(null);
        setChatMessages([]);
        setMatchingCriteria(getInitialCriteria())
        //수정: STORAGE_KEYS 상수 사용
        localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
        localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);

        console.log('로컬스토리지에서 채팅 데이터 제거 완료');
    }, [setMatchResult, setCurrentChatRoomId, setChatMessages, setMatchingCriteria]);

    // 로그아웃 시 단계별 정리 함수
    const performLogoutSteps = useCallback(async (isMatching, handleMatchCancel, isConnected, disconnect) => {
        try {
            // Step 1: 매칭 중이라면 먼저 매칭 취소
            if (isMatching) {
                await handleMatchCancel();
                await new Promise(resolve => setTimeout(resolve, TIMEOUTS.LOGOUT_STEP_DELAY));
            }

            // Step 2: WebSocket 연결 해제
            if (isConnected) {
                disconnect();
                await new Promise(resolve => setTimeout(resolve, TIMEOUTS.WEBSOCKET_DISCONNECT_DELAY));
            }

            // Step 3: 모든 상태 초기화
            await clearAllStates();
        } catch (error) {
            console.error('로그아웃 처리 중 오류:', error);
            // 오류가 있어도 상태는 초기화
            await clearAllStates();
        }
    }, [clearAllStates]);

    return {
        restoreAppState,
        saveMatchingState,
        saveChatMessages,
        clearChatState,
        performLogoutSteps
    };
};
