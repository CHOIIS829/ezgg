import React from 'react';
import styled from '@emotion/styled';
import { keyframes } from '@emotion/react';

const spin = keyframes`
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
`;

const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  height: 400px; // 임의로 높이 설정 (없으면 너무 얇음)
  color: white;
  font-size: 1.2rem;
`;

const Spinner = styled.div`
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: ${spin} 1s linear infinite;
  margin-bottom: 16px;
`;

const messages = {
  user: '회원정보를 불러오는 중...',
  match: '매칭 시도 중...',
  timeline: '타임라인 정보 불러오는 중...'
};

export const LoadingSpinner = ({ type }) => (
  <LoadingContainer><Spinner />{messages[type] || '로딩 중...'}</LoadingContainer>
);
