export const formatTime = (timestamp) => {
  const seconds = Math.floor(timestamp / 1000);
  const minutes = Math.floor(seconds / 60);
  const remain = seconds % 60;
  return `${minutes}:${remain.toString().padStart(2, '0')}`;
};

export const getUsernameById = (participantId, userMatchInfos = {}) => {
  const info = userMatchInfos[participantId];
  if (!info) return null;
  const { riotUsername, riotTag } = info.timelineMemberInfoDto;
  return `${riotUsername} #${riotTag}`;
};

export const getEventType = (event, myId, duoId) => {
  if (event.type === 'CHAMPION_KILL') {
    if (event.killerId === myId || event.killerId === duoId) return 'KILL';
    if (event.victimId === myId || event.victimId === duoId) return 'DEATH';
    if ((event.assistingParticipantIds || []).includes(myId) || (event.assistingParticipantIds || []).includes(duoId)) return 'ASSIST';
    return 'CHAMPION_KILL';
  }
  if (event.type === 'TURRET_PLATE_DESTROYED') return 'TURRET';
  if (event.type === 'BUILDING_KILL') return 'BUILDING';
  if (event.type === 'ELITE_MONSTER_KILL') return 'MONSTER';
  return 'ETC';
};

export const getEventSummary = (event, myId, duoId, userMatchInfos) => {
  const { killerId, victimId, assistingParticipantIds = [] } = event;

  switch (event.type) {
    case 'CHAMPION_KILL':
      if (killerId === myId || killerId === duoId)
        return `⚔️ ${getUsernameById(killerId, userMatchInfos)}: 킬`;
      if (victimId === myId || victimId === duoId)
        return `💀 ${getUsernameById(victimId, userMatchInfos)}: 죽음`;
      if (assistingParticipantIds.includes(myId) || assistingParticipantIds.includes(duoId)) {
        const assists = assistingParticipantIds
          .filter(id => id === myId || id === duoId)
          .map(id => getUsernameById(id, userMatchInfos));
        return `🧩 ${assists.join(', ')}: 어시스트`;
      }
      break;
    case 'ELITE_MONSTER_KILL':
      if (killerId === myId || killerId === duoId)
        return `🐉 ${getUsernameById(killerId, userMatchInfos)}: 몬스터 처치`;
      break;
    case 'BUILDING_KILL':
      if (killerId === myId || killerId === duoId)
        return `🏰 ${getUsernameById(killerId, userMatchInfos)}: 건물 파괴`;
      break;
    case 'TURRET_PLATE_DESTROYED':
      if (killerId === myId || killerId === duoId)
        return `🪙 ${getUsernameById(killerId, userMatchInfos)}: 포탑 골드`;
      break;
    default:
      return null;
  }

  return null;
};
