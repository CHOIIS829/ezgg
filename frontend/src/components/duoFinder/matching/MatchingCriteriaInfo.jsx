import styled from '@emotion/styled';
import { keyword } from '../../../data/keyword';

const MatchingCriteriaInfo = ({ matchingCriteria }) => {
  // 키워드 value를 한국어로 변환하는 함수
  const getKeywordText = (keywordValue) => {
    // global, jungle, support, laner의 모든 키워드를 하나의 객체로 합침
    const allKeywords = {
      ...keyword.global,
      ...keyword.jungle,
      ...keyword.support,
      ...keyword.laner
    };
    
    // value에 해당하는 key(한국어)를 찾음
    return Object.entries(allKeywords).find(entry => entry[1] === keywordValue)?.[0] || keywordValue;
  };

  // 선택된 키워드 텍스트 가져오기
  const getSelectedKeywordsText = () => {
    const { userPreferenceText, wantLine } = matchingCriteria;
    
    if (!userPreferenceText) return '없음';
    
    // 모든 라인의 키워드가 global과 laner로 통일됨
    if (['TOP', 'MID', 'AD', 'JUG', 'SUP'].includes(wantLine.partnerLine)) {
      try {
        const preferences = JSON.parse(userPreferenceText);
        const selectedKeywords = [
          ...Object.entries(preferences.global || {})
            .filter(([, value]) => value === "매우 좋음")
            .map(([key]) => getKeywordText(key)),
          ...Object.entries(preferences.laner || {})
            .filter(([, value]) => value === "매우 좋음")
            .map(([key]) => getKeywordText(key))
        ];
        
        return selectedKeywords.length > 0 ? selectedKeywords.join(', ') : '없음';
      } catch (e) {
        return '없음';
      }
    }
    
    return '없음';
  };

  return (
    <Container>
      <LoadingSpinner />
      <Message>듀오를 찾는 중입니다...</Message>
        <Stats>
            <p>내 선호 라인: {matchingCriteria.wantLine.myLine}</p>
            <p>상대방 선호 라인: {matchingCriteria.wantLine.partnerLine}</p>
            <p>
                선호 챔피언: {
                matchingCriteria.selectedChampions.preferredChampions.length > 0
                    ? matchingCriteria.selectedChampions.preferredChampions.map(c => c.name).join(', ')
                    : '없음'
            }
            </p>
            <p>
                비선호 챔피언: {
                matchingCriteria.selectedChampions.bannedChampions.length > 0
                    ? matchingCriteria.selectedChampions.bannedChampions.map(c => c.name).join(', ')
                    : '없음'
            }
            </p>
            <p>원하는 상대의 플레이 스타일: {getSelectedKeywordsText()}</p>
        </Stats>
    </Container>
  );
};

export default MatchingCriteriaInfo;

const Container = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;

  @media (max-width: 1024px) {
    max-width: 100%;
  }

  @media (max-width: 768px) {
    padding: 1rem;
  }
`;

const LoadingSpinner = styled.div`
  width: 50px;
  height: 50px;
  border: 5px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  border-top-color: #FF416C;
  animation: spin 1s ease-in-out infinite;

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }
`;

const Message = styled.div`
  color: white;
  font-size: 1.2rem;
  text-align: center;
  margin-top: 2rem;
`;

const Stats = styled.div`
  font-size: 1.2rem;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  padding: 0 1.5rem;
  margin-top: 1.5rem;

    p {
    margin: 0.8rem 0;
  }

  @media (max-width: 768px) {
    font-size: 0.9rem;
    padding: 0 1rem;

    p {
      margin: 0.5rem 0;
    }
  }
`;
