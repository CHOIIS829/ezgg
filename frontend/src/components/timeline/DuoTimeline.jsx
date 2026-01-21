import { useEffect, useState } from 'react';
import styled from '@emotion/styled';
import api from "../../utils/api.js";
import {LoadingSpinner} from "../layout/LoadingSpinner.jsx";
import TimelineGroup from "./TimelineGroup.jsx";
import {Link} from "react-router-dom";

const DuoTimeline = ({ memberData }) => {
  const [matchTimelines, setMatchTimelines] = useState([]);
  const [loading, setLoading] = useState(true);

  /**
   * List<DuoTimelineDto> - timeline api 조회 : /duo/timeline
   */
  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await api.get('/duo/timeline');
        setMatchTimelines(res.data.data);
      } catch (error) {
        console.error('Error fetching timelines:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading || !memberData) {
    return <LoadingSpinner type="timeline" />;
  }

  if (matchTimelines.length === 0) {
    return (
      <Wrapper>
        <WrapperText>
          유저 데이터가 없습니다.
        </WrapperText>
        <Link to="/">
          <span style={{ color: 'white' }}>듀오 진행을 해주세요.</span>
        </Link>
      </Wrapper>
    );
  } else {
    return (
      <Wrapper>
        <WrapperText>
          🎮 듀오 매치 타임라인
        </WrapperText>
        <TimelineContainer>
          {matchTimelines.map((match, idx) => {
            const userMatchInfos = match.userMatchInfos;
            const duoParticipantIds = Object.keys(userMatchInfos).sort((a, b) => {
              const aIsMine = userMatchInfos[a].timelineMemberInfoDto.memberId === memberData.memberInfoDto.memberId;
              const bIsMine = userMatchInfos[b].timelineMemberInfoDto.memberId === memberData.memberInfoDto.memberId;
              return aIsMine ? -1 : bIsMine ? 1 : 0;
            }).map(Number);

            const myId = duoParticipantIds[0];
            const duoId = duoParticipantIds[1];

            return (
              <TimelineGroup
                key={idx}
                matchIndex={idx}
                timeline={match.timeline}
                userMatchInfos={userMatchInfos}
                myId={myId}
                duoId={duoId}
              />
            );
          })}
        </TimelineContainer>
      </Wrapper>
    );
  }
};

export default DuoTimeline;


const Wrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 16px;
    background-color: black;
    min-height: 100vh;
    overflow-y: auto;   // 넘칠 때만 스크롤
    overscroll-behavior: contain; // 내부 스크롤이 끝난 후 외부 스크롤로 전환
`;

const WrapperText = styled.h2`
    color: white;
    font-size: 28px;
    font-weight: bold;
    margin-bottom: '24px';
`;
const TimelineContainer = styled.div`
  width: 100%;
  max-width: 720px;
`;

