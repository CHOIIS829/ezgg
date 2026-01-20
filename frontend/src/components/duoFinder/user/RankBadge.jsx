import styled from '@emotion/styled';
import {formatTierText, getRankImageSrc} from '../../../utils/rankUtils.js';

export const RankBadge = ({tier, tierNum}) => (
    <RankBadgeContainer>
        <img
            src={getRankImageSrc(tier)}
            alt={tier || "Unranked"}
            aria-label={formatTierText(tier, tierNum)}
        />
        <span>{formatTierText(tier, tierNum)}</span>
    </RankBadgeContainer>
);
// 티어 아이콘 및 텍스트 컴포넌트
const RankBadgeContainer = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 0 1.5rem;
    border-radius: 8px;

    img {
        width: 75px;
        height: 75px;
    }

    span {
        color: white;
        font-size: 2rem;
        font-weight: 700;
    }
`;
