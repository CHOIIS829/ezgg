// services/storageService.js
import {LIMITS, STORAGE_KEYS, TIMEOUTS} from '../utils/constants.js';

// 데이터 검증 함수들
const validateMatchingState = (data) => {
    return data &&
        typeof data.isMatching === 'boolean' &&
        data.timestamp &&
        !isNaN(new Date(data.timestamp).getTime());
};

const validateChatRoom = (data) => {
    return data &&
        data.chattingRoomId &&
        data.opponentInfo;
};

// 저장소 서비스 함수들
export const storageService = {
    // 매칭 상태 저장
    saveMatchingState: (matchingState) => {
        try {
            const stateWithTimestamp = {
                ...matchingState,
                timestamp: new Date().toISOString()
            };
            localStorage.setItem(STORAGE_KEYS.MATCHING_STATE, JSON.stringify(stateWithTimestamp));
            console.log('매칭 상태 저장:', stateWithTimestamp);
        } catch (error) {
            console.error('매칭 상태 저장 실패:', error);
        }
    },

    // 매칭 상태 가져오기
    getMatchingState: () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEYS.MATCHING_STATE);
            if (!saved) return null;

            const data = JSON.parse(saved);
            if (!validateMatchingState(data)) {
                console.warn('유효하지 않은 매칭 상태, 제거');
                localStorage.removeItem(STORAGE_KEYS.MATCHING_STATE);
                return null;
            }

            // 만료 시간 체크
            const stateTime = new Date(data.timestamp);
            const expiryTime = new Date(Date.now() - TIMEOUTS.MATCHING_STATE_EXPIRY);

            if (stateTime < expiryTime) {
                console.log('매칭 상태 만료, 제거');
                localStorage.removeItem(STORAGE_KEYS.MATCHING_STATE);
                return null;
            }

            return data;
        } catch (error) {
            console.error('매칭 상태 가져오기 실패:', error);
            localStorage.removeItem(STORAGE_KEYS.MATCHING_STATE);
            return null;
        }
    },

    // 채팅방 정보 저장
    saveChatRoom: (chatRoomData) => {
        try {
            const dataWithTimestamp = {
                ...chatRoomData,
                savedAt: new Date().toISOString()
            };
            localStorage.setItem(STORAGE_KEYS.CURRENT_CHAT_ROOM, JSON.stringify(dataWithTimestamp));
            console.log('[StorageService] 채팅방 정보 저장:', dataWithTimestamp);
        } catch (error) {
            console.error('[StorageService] 채팅방 정보 저장 실패:', error);
        }
    },

    // 채팅방 정보 가져오기
    getChatRoom: () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
            if (!saved) return null;

            const data = JSON.parse(saved);
            if (!validateChatRoom(data)) {
                console.warn('유효하지 않은 채팅방 정보, 제거');
                localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
                return null;
            }

            return data;
        } catch (error) {
            console.error('채팅방 정보 가져오기 실패:', error);
            localStorage.removeItem(STORAGE_KEYS.CURRENT_CHAT_ROOM);
            return null;
        }
    },

    // 채팅 메시지 저장
    saveChatMessages: (messages) => {
        try {
            const recentMessages = messages.slice(-LIMITS.MAX_CHAT_MESSAGES);
            localStorage.setItem(STORAGE_KEYS.CHAT_MESSAGES, JSON.stringify(recentMessages));
            console.log('채팅 메시지 저장:', recentMessages.length, '개');
        } catch (error) {
            console.error('채팅 메시지 저장 실패:', error);
        }
    },

    // 채팅 메시지 가져오기
    getChatMessages: () => {
        try {
            const saved = localStorage.getItem(STORAGE_KEYS.CHAT_MESSAGES);
            if (!saved) return [];

            const messages = JSON.parse(saved);
            return Array.isArray(messages) ? messages : [];
        } catch (error) {
            console.error(' 채팅 메시지 가져오기 실패:', error);
            localStorage.removeItem(STORAGE_KEYS.CHAT_MESSAGES);
            return [];
        }
    },

    // 모든 앱 상태 제거
    clearAllAppStates: () => {
        try {
            const keysToRemove = [
                STORAGE_KEYS.MATCHING_STATE,
                STORAGE_KEYS.CURRENT_CHAT_ROOM,
                STORAGE_KEYS.CHAT_MESSAGES,
                STORAGE_KEYS.TOKEN,
                STORAGE_KEYS.USER_INFO,
                STORAGE_KEYS.REFRESH_TOKEN
            ];

            keysToRemove.forEach(key => {
                localStorage.removeItem(key);
            });

            console.log('모든 앱 상태 제거 완료');
        } catch (error) {
            console.error('상태 제거 실패:', error);
        }
    }
};
