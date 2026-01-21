import React from 'react';
import { getEventSummary, getEventType } from '../../utils/timeline.js';
import styled from '@emotion/styled';

const TimelineEvent = ({ event, myId, duoId, userMatchInfos }) => {
  const summary = getEventSummary(event, myId, duoId, userMatchInfos);
  if (!summary) return null;
  const type = getEventType(event, myId, duoId);

  return <EventBox type={type}>{summary}</EventBox>;
};

export default TimelineEvent;

const EventBox = styled.div`
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
  ${({ type }) => {
  switch (type) {
    case 'KILL':
      return `background: #4b1e1e; color: #ffb3b3;`;
    case 'DEATH':
      return `background: #1e274b; color: #b3cfff;`;
    case 'ASSIST':
      return `background: #263341; color: #c0eaff;`;
    case 'TURRET':
      return `background: #3f2e1e; color: #ffd9b3;`;
    case 'BUILDING':
      return `background: #2f2f2f; color: #cccccc;`;
    case 'MONSTER':
      return `background: #3a1e4b; color: #e0b3ff;`;
    default:
      return `background: #333; color: white;`;
  }
}}
`;
