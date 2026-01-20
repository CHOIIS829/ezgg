import React from 'react';
import styled from '@emotion/styled';
import {UserProfileCard} from "./user/UserProfileCard.jsx";
import {MatchingInterface} from "./matching/MatchingInterface.jsx";
import ChatRoom from "../chatting/ChatRoom.jsx";

const DuoFinder = (
    {
        memberDataBundle,
        isLoading,
        userInfo,
        matchingCriteria,
        isMatching,
        setMatchingCriteria,
        matchResult,
        chatMessages,
        sendChatMessage,
        isConnected
    }
) => {
    return (
        <>
            <Container>
                <UserProfileCard
                    userInfo={userInfo}
                    memberDataBundle={memberDataBundle}
                    isLoading={isLoading}
                />

                {matchResult ? (
                    <UserProfileCard
                        memberDataBundle={matchResult.data}
                        isLoading={isLoading}
                        isOpponent={true}
                    />
                ) : (
                    <MatchingInterface
                        matchResult={matchResult}
                        matchingCriteria={matchingCriteria}
                        isMatching={isMatching}
                        setMatchingCriteria={setMatchingCriteria}
                    />
                )}
            </Container>

            {matchResult && (
                <Container>
                    <ChatRoom
                        userInfo={userInfo}
                        matchResult={matchResult}
                        chatMessages={chatMessages}
                        sendChatMessage={sendChatMessage}
                        isConnected={isConnected}
                    />
                </Container>
            )}
        </>
    )
};

export default DuoFinder;

const Container = styled.div`
    display: flex;
    gap: 2rem;
    padding: 2rem;
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;
    align-items: flex-start;
    justify-content: center;

    @media (max-width: 1024px) {
        width: 95%;
        flex-direction: column;
        align-items: center;
    }
`;
