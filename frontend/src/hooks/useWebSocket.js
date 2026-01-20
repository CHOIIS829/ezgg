// useWebSocket.js
// -----------------------------------------------------------------------------
// STOMP WebSocket 커스텀 훅
// - UI에서 사용하는 ViewModel(criteria)을 DTO로 변환 후 서버로 전송
// - onMessage/onConnect/onDisconnect/onError 콜백 지원
// -----------------------------------------------------------------------------
import {useCallback, useRef, useState} from 'react';
import SockJS from 'sockjs-client';
import {Stomp} from '@stomp/stompjs';
import api, {tokenUtils} from '../utils/api';
import {storageService} from "../services/storageService.js";
import {STORAGE_KEYS} from "../utils/constants.js";

// ---------------- ViewModel → DTO 변환기 --------------------------------------
/**
 * 매칭 조건 ViewModel을 DTO로 변환하는 함수.
 *  - 챔피언 객체 → id 문자열 배열로 축소
 *  - 불필요 필드는 제거
 */
const criteriaToDTO = (vm) => ({
    wantLine: vm.wantLine,
    userPreferenceText: vm.userPreferenceText,
    selectedChampions: {
        preferredChampions: vm.selectedChampions?.preferredChampions?.map((c) => c.id) ?? [],
        bannedChampions: vm.selectedChampions?.bannedChampions?.map((c) => c.id) ?? [],
    }
});

// ---------------- 커스텀 훅 -----------------------------------------------------
export const useWebSocket = ({onMessage, onConnect, onDisconnect, onError, onChatMessage, onReview, onPartnerLeft}) => {
    const stompClient = useRef(null);
    const [isConnected, setIsConnected] = useState(false);
    const subscriptionsRef = useRef(new Map());

    // 토큰 유효성 검증 함수
    const validateToken = async () => {
        try {
            const token = tokenUtils.get();
            if (!token) {
                console.log('Token not found');
                // 토큰이 없으면 모든 앱 상태를 초기화
                storageService.clearAllAppStates();
                return false;
            }
            // 단순히 API 요청을 보내서 토큰 검증
            // 401이 떨어지면 api.js의 인터셉터가 자동으로 토큰을 재발급 받음
            await api.post('/auth/validateToken');

            return true;
        } catch (error) {
            console.log('Token validation or refresh failed:', error);
            // 토큰이 없으면 모든 앱 상태를 초기화
            storageService.clearAllAppStates();
            return false;
        }
    }

    /** 모든 구독 해제 함수 */
    const unsubscribeAll = useCallback(() => {
        subscriptionsRef.current.forEach((subscription) => {
            try {
                if (subscription && typeof subscription.unsubscribe === 'function') {
                    subscription.unsubscribe();
                }
            } catch (error) {
                console.error(`구독 해제 중 오류 :`, error);
            }
        });
        subscriptionsRef.current.clear();
        console.log(' 모든 구독 해제 완료');
    }, []);

    /** 채팅방 구독 함수 (재사용 가능) */
    const subscribeToChatRoom = useCallback((chattingRoomId) => {
        if (!stompClient.current?.connected) {
            console.warn('연결되지 않음, 채팅방 구독 불가');
            return;
        }
        // 브로드캐스트 구독
        const topicSub = stompClient.current.subscribe(`/topic/chat/${chattingRoomId}`, (chatMsg) => {
            const chatResponse = JSON.parse(chatMsg.body);
            if (onChatMessage) {
                onChatMessage(chatResponse);
            }
        });
        subscriptionsRef.current.set(`topic-chat-${chattingRoomId}`, topicSub);

    }, [onChatMessage]);

    /** STOMP 서버 연결 함수 */
    const connect = useCallback(async (onConnectedCallback, existingChatRoomId = null) => {
        // 이미 연결되어 있다면 기존 채팅방만 구독하고 콜백 실행
        if (stompClient.current && stompClient.current.connected) {
            if (existingChatRoomId) {
                subscribeToChatRoom(existingChatRoomId);
            }
            onConnectedCallback?.();
            return;
        }

        // 기존 연결이 있다면 완전히 정리
        if (stompClient.current) {
            unsubscribeAll();
            try {
                stompClient.current.disconnect();
            } catch (error) {
                console.error('기존 연결 해제 중 오류:', error);
            }
            stompClient.current = null;
        }

        // 토큰 검증
        const isTokenValid = await validateToken();
        if (!isTokenValid) {
            console.error('Invalid or expired token and refresh failed');
            if (onError) onError('인증이 만료되었습니다. 다시 로그인해주세요.');
            return;
        }

        const token = tokenUtils.get();
        console.log('token: ', token);
        const socket = new SockJS(`http://localhost:8888/ws?token=${token}`);
        stompClient.current = Stomp.over(socket);

        socket.onclose = () => {
            setIsConnected(false);
            unsubscribeAll();
            if (onDisconnect) onDisconnect();
        };

        stompClient.current.connect({},
            () => {
                console.log('WebSocket connected');
                setIsConnected(true);

                try {
                    // 매칭 결과 구독
                    const matchingSub = stompClient.current.subscribe(`/user/queue/matching`, (message) => {
                        const response = JSON.parse(message.body);
                        // 매칭 성공 시 채팅방 구독 추가
                        if (response.status === "SUCCESS" && response.data?.chattingRoomId) {
                            subscribeToChatRoom(response.data.chattingRoomId);
                        }

                        if (onMessage) onMessage(response);
                    });
                    subscriptionsRef.current.set('matching', matchingSub);

                    // 에러 구독 (중복 제거)
                    const errorSub = stompClient.current.subscribe(`/user/queue/errors`, (message) => {
                        if (onError) onError(message.body);
                    });
                    subscriptionsRef.current.set('errors', errorSub);

                    // 리뷰 알림 구독
                    const reviewSub = stompClient.current.subscribe('/user/queue/review', (message) => {
                        const [reviewTargetUsername, matchId] = message.body.split(',');
                        if (onReview) onReview(reviewTargetUsername, matchId);
                    });
                    subscriptionsRef.current.set('review', reviewSub);

                    // 파트너 퇴장 알림 구독
                    const partnerLeftSub = stompClient.current.subscribe('/user/queue/partner-left', (message) => {
                        try {
                            const notification = JSON.parse(message.body);
                            console.log('파트너 퇴장 알림 수신:', notification);

                            if (onPartnerLeft) {
                                onPartnerLeft(notification);
                            }
                        } catch (error) {
                            console.error('파트너 퇴장 메시지 파싱 오류:', error);
                            if (onPartnerLeft) {
                                onPartnerLeft({sender: '상대방', message: '상대방이 채팅방을 떠났습니다.'});
                            }
                        }
                    });
                    subscriptionsRef.current.set('partner-left', partnerLeftSub);
                    // 새로고침 후 기존 채팅방이 있다면 구독
                    if (existingChatRoomId) {
                        subscribeToChatRoom(existingChatRoomId);
                    }
                } catch (subscribeError) {
                    console.error('구독 중 오류:', subscribeError);
                }

                if (onConnect) onConnect();
                onConnectedCallback?.();
            },
            (error) => {
                console.error('WebSocket connection error', error);
                setIsConnected(false);
                unsubscribeAll();
                if (onError) onError('웹소켓 연결에 실패했습니다.');
            }
        );
    }, [onConnect, onMessage, onDisconnect, onError, onChatMessage, onReview, unsubscribeAll, subscribeToChatRoom, onPartnerLeft]);

    /** 연결 해제 함수 */
    const disconnect = useCallback(() => {
        // 모든 구독 해제
        unsubscribeAll();

        if (stompClient.current) {
            try {
                stompClient.current.disconnect(() => {
                });
            } catch (error) {
                console.error(' 연결 해제 중 오류:', error);
            }
            stompClient.current = null;
        }

        setIsConnected(false);
        localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
        localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);

        if (onDisconnect) onDisconnect();
    }, [onDisconnect, unsubscribeAll]);

    /**
     * 매칭 요청 전송 함수.
     *  - ViewModel → DTO 변환 후 JSON 직렬화하여 전송
     */
    const sendMatchingRequest = useCallback(async (criteriaVM) => {
        const dtoPayload = criteriaToDTO(criteriaVM);
        const json = JSON.stringify(dtoPayload);

        if (!stompClient.current?.connected) {
            const isTokenValid = await validateToken();
            if (!isTokenValid) {
                if (onError) onError('인증이 만료되었습니다');
                return;
            }
            connect(() => {
                if (stompClient.current?.connected) {
                    stompClient.current.send('/app/matching/start', {}, json);
                    console.log('매칭 요청 전송됨');
                }
            });
            return;
        }
        stompClient.current.send('/app/matching/start', {}, json);
        console.log('매칭 요청 전송됨');
    }, [connect, onError]);

    /**
     * 매칭 취소 요청 전송 함수
     */
    const sendCancelRequest = useCallback(async () => {
        if (!stompClient.current?.connected) {
            console.log('연결되지 않음, 연결 후 매칭 취소 요청 전송');
            const isTokenValid = await validateToken();
            if (!isTokenValid) {
                if (onError) onError('인증이 만료되었습니다');
                return false;
            }
            connect(() => {
                if (stompClient.current?.connected) {
                    stompClient.current.send('/app/matching/stop', {}, JSON.stringify({}));
                    console.log('매칭 취소 요청 전송됨');
                }
            });
            return true;
        }
        stompClient.current.send('/app/matching/stop', {}, JSON.stringify({}));
        console.log('매칭 취소 요청 전송됨');
        return true;
    }, [connect, onError]);

    /**
     * 채팅 메시지 전송 함수
     */
    const sendChatMessage = useCallback(async (chattingRoomId, message, sender) => {
        if (!stompClient.current?.connected) {
            console.log('연결되지 않음, 채팅 메시지 전송 실패');
            if (onError) onError('웹소켓 연결이 끊어졌습니다.');
            return false;
        }

        const chatData = {
            chattingRoomId: chattingRoomId,
            message: message,
            sender: sender,
            timestamp: new Date().toISOString()
        };

        try {
            stompClient.current.send('/app/chat/send', {}, JSON.stringify(chatData));
            return true;
        } catch (error) {
            console.error('채팅 메시지 전송 실패:', error);
            if (onError) onError('채팅 메시지 전송에 실패했습니다.');
            return false;
        }
    }, [onError]);

    const sendLeaveRequest = useCallback(async (chattingRoomId, userId) => {
        if (!stompClient.current?.connected) {
            console.log('연결되지 않음, 채팅방 나가기 실패');
            return false;
        }

        try {
            stompClient.current.send('/app/chat/leave', {}, JSON.stringify({
                chattingRoomId: chattingRoomId,
                userId: userId
            }));
            console.log('채팅방 나가기 요청 전송됨');
            localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
            localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);
            return true;
        } catch (error) {
            console.error('채팅방 나가기 요청 실패:', error);
            return false;
        }
    }, []);


    return {
        socket: stompClient.current,
        connect,
        disconnect,
        sendMatchingRequest,
        sendCancelRequest,
        sendChatMessage,
        subscribeToChatRoom,
        isConnected,
        sendLeaveRequest
    };
};
