import React, { useState } from "react";
import styled from '@emotion/styled';
import api from "../../utils/api";

const ReviewModal = ({ visible, onClose, targetUsername, matchId }) => {
    const [rating, setRating] = useState(0);
    const [hover, setHover] = useState(0);

    if(!visible) return null;

    const handleSubmit = () => {
        // TODO: 서버로 별점 전송 로직 구현
        console.log(`별점 ${rating}점을 ${targetUsername}님에게 부여했습니다.`);
        console.log(`matchId: ${matchId}`);
        api.post('/review', {
            partnerRiotUsername: targetUsername,
            matchId: matchId,
            reviewScore: rating,
        });
        onClose();
    };

    return (
        <ModalOverlay>
            <ModalContent>
                <ModalHeader>
                    <h2>{targetUsername}님의 별점을 남겨주세요!</h2>
                </ModalHeader>
                <StarContainer>
                    {[1, 2, 3, 4, 5].map((star) => (
                        <Star
                            key={star}
                            onClick={() => setRating(star)}
                            onMouseEnter={() => setHover(star)}
                            onMouseLeave={() => setHover(0)}
                            style={{
                                color: star <= (hover || rating) ? '#FFD700' : '#e4e5e9'
                            }}
                        >
                            ★
                        </Star>
                    ))}
                </StarContainer>
                <SubmitButton onClick={handleSubmit} disabled={rating === 0}>
                    별점 남기기
                </SubmitButton>
            </ModalContent>
        </ModalOverlay>
    );
};

const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
`;

const ModalContent = styled.div`
    background: #1a1a1a;
    padding: 2rem;
    border-radius: 8px;
    width: 90%;
    max-width: 500px;
    color: white;
`;

const ModalHeader = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 2rem;
    text-align: center;

    h2 {
        margin: 0;
        font-size: 1.5rem;
    }
`;

const CloseButton = styled.button`
    background: none;
    border: none;
    color: white;
    font-size: 1.5rem;
    cursor: pointer;
    padding: 0.5rem;
    
    &:hover {
        opacity: 0.7;
    }
`;

const StarContainer = styled.div`
    display: flex;
    justify-content: center;
    gap: 1rem;
    margin-bottom: 2rem;
`;

const Star = styled.span`
    font-size: 2.5rem;
    cursor: pointer;
    transition: color 0.2s;
    
    &:hover {
        transform: scale(1.1);
    }
`;

const SubmitButton = styled.button`
    width: 100%;
    padding: 1rem;
    background: #FF416C;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.9;
    }

    &:disabled {
        background: #666;
        cursor: not-allowed;
    }
`;

export default ReviewModal;