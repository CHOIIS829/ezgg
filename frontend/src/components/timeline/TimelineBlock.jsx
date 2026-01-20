import React from 'react';
import { formatTime, getEventSummary } from '../../utils/timeline.js';
import TimelineEvent from './TimelineEvent';
import styled from '@emotion/styled';

const TimelineTimeBlock = ({ timestamp, events, userMatchInfos, myId, duoId }) => {
  const time = formatTime(timestamp);
  const validEvents = events.filter(e => getEventSummary(e, myId, duoId, userMatchInfos));

  if (validEvents.length === 0) return null;

  return (
    <div>
      <BlockTitle>⏰ {time}</BlockTitle>
      {validEvents.map((e, idx) => (
        <TimelineEvent key={idx} event={e} myId={myId} duoId={duoId} userMatchInfos={userMatchInfos} />
      ))}
    </div>
  );
};

export default TimelineTimeBlock;

const BlockTitle = styled.div`
  font-weight: bold;
  font-size: 16px;
  color: #ccc;
  margin: 12px 0;
`;
