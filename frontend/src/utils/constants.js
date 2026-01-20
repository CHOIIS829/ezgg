// utils/constants.js
export const STORAGE_KEYS = {
    TOKEN: 'token',
    USER_INFO: 'userInfo',
    REFRESH_TOKEN: 'refreshToken',
    CURRENT_CHAT_ROOM: 'currentChatRoom',
    MATCHING_STATE: 'matchingState',
    CHAT_MESSAGES: 'chatMessages'
};

export const TIMEOUTS = {
    MATCHING_STATE_EXPIRY: 5 * 60 * 1000, // 5분
    LOGOUT_STEP_DELAY: 500, // 로그아웃 단계별 지연
    WEBSOCKET_DISCONNECT_DELAY: 300 // WebSocket 연결 해제 지연
};

export const LIMITS = {
    MAX_CHAT_MESSAGES: 50 // 저장할 최대 채팅 메시지 수
};
