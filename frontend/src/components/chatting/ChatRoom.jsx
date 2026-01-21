import React, {useEffect, useRef, useState} from 'react';
import styled from '@emotion/styled';

const ChatRoom = ({userInfo, matchResult, chatMessages, sendChatMessage, isConnected}) => {
    const [messages, setMessages] = useState([
        {id: 1, sender: 'System', text: '채팅방에 오신 것을 환영합니다!', timestamp: new Date().toISOString()}
    ]);
    const [input, setInput] = useState('');
    const messagesEndRef = useRef(null);

    // App.jsx에서 전달받은 채팅 메시지를 messages에 추가 - 단순화
    useEffect(() => {
        console.log('[ChatRoom] chatMessages 업데이트:', chatMessages);

        if (chatMessages && chatMessages.length > 0) {
            const formattedMessages = chatMessages.map((msg, index) => ({
                id: msg.id || Date.now() + index,
                sender: msg.sender || '알 수 없음',
                text: msg.message || msg.text || '',
                timestamp: msg.timestamp || new Date().toISOString(),
                isOwn: msg.sender === userInfo.riotUsername
            }));

            // 시스템 메시지 + 모든 채팅 메시지로 완전 동기화
            setMessages([
                {id: 1, sender: 'System', text: '채팅방에 오신 것을 환영합니다!', timestamp: new Date().toISOString()},
                ...formattedMessages
            ]);

            console.log('[ChatRoom] 메시지 완전 동기화 완료:', formattedMessages.length, '개');
        }
    }, [chatMessages, userInfo.riotUsername]);

    // 새 메시지 스크롤 아래로 자동 이동
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
    }, [messages]);

    // 메시지 보내기 함수
    const sendMessage = () => {
        if (!input.trim()) return;
        if (!matchResult?.chattingRoomId) {
            alert('채팅방 정보가 없습니다.');
            return;
        }

        console.log('[ChatRoom] 메시지 전송:', {
            chattingRoomId: matchResult.chattingRoomId,
            message: input.trim(),
            sender: userInfo.riotUsername
        });


        // WebSocket으로 메시지 전송
        sendChatMessage(
            matchResult.chattingRoomId,
            input.trim(),
            userInfo.riotUsername || '익명'
        );

        setInput('');
    };

    // Enter키로 메시지 보내기
    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            if (e.nativeEvent.isComposing) {
                return;
            }
            e.preventDefault();
            sendMessage();
        }
    };

    console.log('[ChatRoom] 렌더링 - 메시지 수:', messages.length, '연결 상태:', isConnected);

    return (
        <ChatContainer>
            <ChatHeader>
                💬 채팅방 - {matchResult?.opponentInfo?.riotUsername || '상대방'}#{matchResult?.opponentInfo?.riotTag || 'KR'}
                {isConnected ?
                    <ConnectionStatus connected>● 연결됨</ConnectionStatus> :
                    <ConnectionStatus>● 연결 끊김</ConnectionStatus>
                }
            </ChatHeader>
            <Messages>
                {messages.map(msg => (
                    <Message key={msg.id} isOwn={msg.isOwn || msg.sender === userInfo.riotUsername}>
                        <MessageContent isOwn={msg.isOwn || msg.sender === userInfo.riotUsername}>
                            <Sender isOwn={msg.isOwn || msg.sender === userInfo.riotUsername}>
                                {msg.sender}:
                            </Sender>
                            <MessageText>{msg.text}</MessageText>
                            <MessageTime>
                                {new Date(msg.timestamp).toLocaleTimeString('ko-KR', {
                                    hour: '2-digit',
                                    minute: '2-digit'
                                })}
                            </MessageTime>
                        </MessageContent>
                    </Message>
                ))}
                <div ref={messagesEndRef}/>
            </Messages>
            <InputWrapper>
                <ChatInput
                    type="text"
                    placeholder={`메시지 입력... (연결: ${isConnected ? 'ON' : 'OFF'})`}
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
                <SendButton onClick={sendMessage} disabled={!input.trim() || !isConnected}>
                    전송
                </SendButton>
            </InputWrapper>
        </ChatContainer>
    );
};

export default ChatRoom;

// 스타일 컴포넌트
const ChatContainer = styled.div`
    width: 75%;
    max-width: 600px;
    height: 400px;
    margin: 1rem auto;
    background: #1e1e1e;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
`;

const ChatHeader = styled.div`
    padding: 0.75rem 1rem;
    background: #222;
    color: white;
    font-weight: bold;
    border-radius: 8px 8px 0 0;
    border-bottom: 1px solid #333;
    display: flex;
    justify-content: space-between;
    align-items: center;
`;

const ConnectionStatus = styled.span`
    font-size: 0.8rem;
    color: ${({connected}) => (connected ? '#4caf50' : '#f44336')};
    font-weight: normal;
`;

const Messages = styled.div`
    flex-grow: 1;
    padding: 1rem;
    overflow-y: auto;
    color: white;
    font-size: 0.9rem;

    &::-webkit-scrollbar {
        width: 6px;
    }

    &::-webkit-scrollbar-track {
        background: #2a2a2a;
    }

    &::-webkit-scrollbar-thumb {
        background: #555;
        border-radius: 3px;
    }
`;

const Message = styled.div`
    margin-bottom: 0.5rem;
    display: flex;
    justify-content: ${({isOwn}) => (isOwn ? 'flex-end' : 'flex-start')};
`;

const MessageContent = styled.div`
    max-width: 70%;
    padding: 0.5rem 0.75rem;
    border-radius: 12px;
    background: ${({isOwn}) => (isOwn ? '#4caf50' : '#333')};
    word-wrap: break-word;
`;

const Sender = styled.div`
    font-weight: 600;
    font-size: 0.8rem;
    margin-bottom: 0.2rem;
    color: ${({isOwn}) => (isOwn ? '#e8f5e8' : '#bbb')};
`;

const MessageText = styled.div`
    color: white;
    line-height: 1.4;
    margin-bottom: 0.2rem;
`;

const MessageTime = styled.div`
    font-size: 0.7rem;
    color: ${({isOwn}) => (isOwn ? '#c8e6c9' : '#999')};
    text-align: right;
`;

const InputWrapper = styled.div`
    display: flex;
    padding: 0.5rem 1rem;
    background: #222;
    border-radius: 0 0 8px 8px;
    border-top: 1px solid #333;
`;

const ChatInput = styled.input`
    flex-grow: 1;
    padding: 0.5rem 0.75rem;
    background: #333;
    border: none;
    border-radius: 4px;
    color: white;
    font-size: 1rem;
    font-family: inherit;

    &:focus {
        outline: none;
        background: #444;
    }

    &::placeholder {
        color: #888;
    }
`;

const SendButton = styled.button`
    margin-left: 0.75rem;
    background: ${({disabled}) => (disabled ? '#666' : '#FF416C')};
    color: white;
    border: none;
    border-radius: 4px;
    padding: 0 1rem;
    cursor: ${({disabled}) => (disabled ? 'not-allowed' : 'pointer')};
    transition: all 0.2s;

    &:hover {
        opacity: ${({disabled}) => (disabled ? '1' : '0.9')};
        transform: ${({disabled}) => (disabled ? 'none' : 'translateY(-1px)')};
    }
`;
