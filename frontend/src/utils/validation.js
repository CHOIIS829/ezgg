export const isValidCriteria = (criteria) => {
  if (!criteria || !isValidLine(criteria)) return false;

  // 선호/비선호 챔피언 체크
  if (!criteria.selectedChampions?.preferredChampions?.length || 
      !criteria.selectedChampions?.bannedChampions?.length) {
    return false;
  }

  // 확장 포인트: 추가 필수 조건
  // if (criteria.새로운조건 !== requiredValue) return false;

  return true;
};

const isValidLine = (criteria) => {
  return criteria?.wantLine?.myLine &&
    criteria?.wantLine?.partnerLine &&
    criteria.wantLine.myLine !== criteria.wantLine.partnerLine;
}
