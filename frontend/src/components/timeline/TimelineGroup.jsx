import React, { useState } from 'react';
import TimelineTimeBlock from './TimelineBlock.jsx';
import { getUsernameById } from '../../utils/timeline.js';
import styled from '@emotion/styled';

const TimelineGroup = ({ timeline, userMatchInfos, myId, duoId }) => {
  const [open, setOpen] = useState(false);

  return (
    <MatchCard>
      <MatchHeader onClick={() => setOpen(!open)}>
        <div className="title">
          <span className="icon">🕹️</span>
          <span className="text">
            <strong>{getUsernameById(myId, userMatchInfos)}</strong>
            <span className="and"> & </span>
            <strong>{getUsernameById(duoId, userMatchInfos)}</strong>
          </span>
        </div>
        <span className="toggle">{open ? '▲' : '▼'}</span>
      </MatchHeader>

      {open && (
        <TimelineBlock>
          {timeline.map((block, i) => {
            const events = block.events.filter((e) => {
              const relatedIds = [e.killerId, e.victimId, ...(e.assistingParticipantIds || [])];
              return relatedIds.some(id => id === myId || id === duoId);
            });
            if (events.length === 0) return null;
            return (
              <TimelineTimeBlock
                key={i}
                timestamp={block.timestamp}
                events={events}
                userMatchInfos={userMatchInfos}
                myId={myId}
                duoId={duoId}
              />
            );
          })}
        </TimelineBlock>
      )}
    </MatchCard>
  );
};

export default TimelineGroup;


const MatchCard = styled.div`
  background: #1e1e1e;
  border-radius: 16px;
  margin-bottom: 24px;
  overflow: hidden;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.05);
`;

const MatchHeader = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 auto;
    padding: 12px 16px;
    background-color: #2c2c2c;
    font-size: 14px;
    font-weight: 500;
    color: white;
    border-radius: 12px;
    cursor: pointer;

  .title {
    display: contents;
    gap: 10px;
    overflow: hidden;
  }

  .icon {
    font-size: 16px;
    flex-shrink: 0;
  }
    
  .and {
    color: #aaa;
    font-weight: normal;
  }

  .toggle {
    font-size: 12px;
    color: #ccc;
  }
`;

const TimelineBlock = styled.div`
  padding: 16px;
  max-height: 300px;
  overflow-y: auto;
`;
