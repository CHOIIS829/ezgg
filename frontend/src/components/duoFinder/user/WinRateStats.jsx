import styled from '@emotion/styled';
import {champions} from "../../../data/champions.js";
import {getChampionImageSrc} from "../../../utils/championUtils.js";

export const WinRateStats = ({champions: championStats}) => {
    const getKoreanChampionName = (englishName) => {
        // getChampionImageSrc에서 사용하는 정규화 로직을 활용
        // 이미지 경로에서 파일명만 추출
        const imagePath = getChampionImageSrc(englishName);
        const fileName = imagePath.split('/').pop().replace('.png', '');

        const championData = champions.find(c => c.id === fileName);
        return championData ? championData.name : englishName;
    };

    return (
        <StatsContainer>
            {/*<StatItem>승률:({win || "0"}W / {loss || "0"}L) {winRate || "0"}%</StatItem>*/}

            {championStats && Object.keys(championStats).length > 0 ? (
                <ChampionGrid>
                    {Object.values(championStats).map((champion, index) => (
                        <ChampionCard key={index}>
                            <ChampionAvatar
                                src={getChampionImageSrc(champion.championName)}
                                alt={getKoreanChampionName(champion.championName)}
                            />
                            <ChampionInfo>
                                <ChampionName>
                                    {getKoreanChampionName(champion.championName)}
                                </ChampionName>
                                <ChampionStats>
                                    {champion.wins || "0"}W/{champion.losses || "0"}L
                                </ChampionStats>
                                <WinRateText>
                                    {champion.winRateOfChampion || "0"}%
                                </WinRateText>
                            </ChampionInfo>
                        </ChampionCard>
                    ))}
                </ChampionGrid>
            ) : (
                <StatItem>챔피언 통계가 없습니다.</StatItem>
            )}
        </StatsContainer>
    );
};

const StatsContainer = styled.div`
    padding: 1.5rem;
    background: #2a2a2a;
    border-radius: 8px;
    margin-top: 0.5rem;
`;

const StatItem = styled.p`
    margin: 0.5rem 0;
    font-size: 1.1rem;
    color: #e0e0e0;

    &:first-child {
        font-weight: bold;
        color: #ffd700;
        margin-bottom: 1.5rem;
    }
`;

const ChampionGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1rem;
    margin-top: 0.5rem;
`;
// ChampionCard 컴포넌트는 각 챔피언의 카드 형태로 표시됩니다.
const ChampionCard = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 1.25rem 0.75rem;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    border: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.2s ease;

    &:hover {
        background: rgba(255, 255, 255, 0.08);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    }
`;
//* ChampionAvatar 컴포넌트는 각 챔피언의 아바타 이미지를 표시합니다. */
const ChampionAvatar = styled.img`
    width: 80px;
    height: 80px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.2);
    object-fit: cover;
    margin-bottom: 0.75rem;
`;
// ChampionInfo 컴포넌트는 챔피언의 이름과 통계 정보를 표시합니다.
const ChampionInfo = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 0.4rem;
`;
// ChampionName 컴포넌트는 챔피언의 이름을 강조하여 표시합니다.
const ChampionName = styled.div`
    font-weight: 600;
    color: #ffffff;
    font-size: 1rem;
    line-height: 1.2;
`;
// ChampionStats 컴포넌트는 챔피언의 승리, 패배 및 승률을 표시합니다.
const ChampionStats = styled.div`
    font-size: 0.9rem;
    color: #e0e0e0;
    font-weight: 500;
`;
// WinRateText 컴포넌트는 승률을 강조하여 표시합니다.
const WinRateText = styled.div`
    font-size: 1rem;
    font-weight: 700;
    color: #4CAF50;
`;
