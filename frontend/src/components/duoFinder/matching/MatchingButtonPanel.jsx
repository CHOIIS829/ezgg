import styled from '@emotion/styled';

export const MatchingButtonPanel = ({
                                      matchingCriteria,
                                      matchResult,
                                      isMatching,
                                      penaltyTime,
                                      isPenaltyActive,
                                      onStart,
                                      onCancel,
                                      handleBackButton
                                    }) => {
  // mm:ss 포맷으로 변환
  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  };

  return (
    <Panel>
      {isPenaltyActive ? (
        <PenaltyTimer>페널티 남은 시간: {formatTime(penaltyTime)}</PenaltyTimer>
      ) : (
        <>
          {!(matchingCriteria.wantLine.myLine && matchingCriteria.wantLine.partnerLine && isMatching) && !matchResult && (
            <button onClick={onStart}>매칭 시작</button>
          )}
          {(isMatching && matchingCriteria.wantLine.myLine && matchingCriteria.wantLine.partnerLine) && !matchResult && (
            <button onClick={onCancel}>매칭 중지</button>
          )}
          {matchResult && (
            <button onClick={handleBackButton}>돌아가기</button>
          )}
        </>
      )}
    </Panel>
  );
};

const Panel = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 150px;
    width: 100vw;
`;

const PenaltyTimer = styled.div`
  font-size: 32px;
  font-weight: bold;
  color: white;
`;
