import {RankBadge} from "./RankBadge.jsx";
import {WinRateStats} from "./WinRateStats.jsx";
import {LoadingSpinner} from "../../layout/LoadingSpinner.jsx";
import styled from '@emotion/styled';

export const UserProfileCard = ({userInfo, memberDataBundle, isLoading}) => {
    const {memberInfoDto, recentTwentyMatchDto} = memberDataBundle || {};
    const effectiveMemberInfo = memberInfoDto || userInfo;

    const handleCopyClick = () => {
        const riotId = `${effectiveMemberInfo?.riotUsername || "사용자"}#${effectiveMemberInfo?.riotTag || "0000"}`;
        navigator.clipboard.writeText(riotId);
    };

    const calculateWinRate = () => {
        const wins = memberInfoDto?.wins || 0;
        const losses = memberInfoDto?.losses || 0;
        return wins + losses > 0 ? Math.round(wins / (wins + losses) * 100) : 0;
    };

    // 유저 프로필 티어별 배경색 및 그라데이션 설정
    const getTierStyle = (tier) => {
        const tierColors = {
            'IRON': 'linear-gradient(135deg, #3a3a3a 0%, #0a0a0a 100%)',
            'BRONZE': 'linear-gradient(135deg, #8b4500 0%, #2d1500 100%)',
            'SILVER': 'linear-gradient(135deg, #6a6a6a 0%, #1a1a1a 100%)',
            'GOLD': 'linear-gradient(135deg, #b8860b 0%, #3d2800 100%)',
            'PLATINUM': 'linear-gradient(135deg, #00a066 0%, #002415 100%)',
            'EMERALD': 'linear-gradient(135deg, #00b33c 0%, #002a0f 100%)',
            'DIAMOND': 'linear-gradient(135deg, #2266cc 0%, #0a1a33 100%)',
            'MASTER': 'linear-gradient(135deg, #cc0000 0%, #330000 100%)',
            'GRANDMASTER': 'linear-gradient(135deg, #990000 0%, #1a0000 100%)',
            'CHALLENGER': 'linear-gradient(135deg, #ff8000 0%, #4d1a00 100%)'
        };

        return tierColors[tier?.toUpperCase()] || 'linear-gradient(135deg, #3a3a3a 0%, #0a0a0a 100%)';
    };

    return (
        <ProfileCard>
            {isLoading ? (
                <LoadingSpinner type={'user'}/>
            ) : (
                <>
                    <ProfileInfo>
                        <RankBadge
                            tier={memberInfoDto?.tier}
                            tierNum={memberInfoDto?.tierNum}
                        />
                        <UserInfoWrapper tierStyle={getTierStyle(memberInfoDto?.tier)}>
                            <UserNameRow>
                                <ProfileTitle>
                                    {effectiveMemberInfo?.riotUsername || "사용자"} #{effectiveMemberInfo?.riotTag || "0000"}
                                </ProfileTitle>
                                <CopyButton onClick={handleCopyClick}>
                                    복사
                                </CopyButton>
                            </UserNameRow>
                            <WinRateRow>
                                <SeasonWinRate>
                                    ({memberInfoDto?.wins || 0}W / {memberInfoDto?.losses || 0}L) {calculateWinRate()}%
                                </SeasonWinRate>
                            </WinRateRow>
                        </UserInfoWrapper>
                    </ProfileInfo>
                    <WinRateStats
                        win={memberInfoDto?.wins || 0}
                        loss={memberInfoDto?.losses || 0}
                        winRate={calculateWinRate()}
                        champions={recentTwentyMatchDto?.championStats}
                    />
                </>
            )}
        </ProfileCard>
    )
};

const ProfileCard = styled.div`
    flex: 1;
    width: 100%;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    min-height: 635px;
    padding: 2rem;

    @media (max-width: 1024px) {
        max-width: 100%;
    }
`;

const ProfileInfo = styled.div`
    color: white;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 2rem;
    text-align: center;
`;


const UserInfoWrapper = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
    padding: 2rem;
    background: ${props => props.tierStyle};
    border-radius: 20px;
    border: 2px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(10px);
    width: 100%;

    @media (max-width: 768px) {
        //width: 100%;
        padding: 1.5rem;
    }

    @media (max-width: 480px) {
        padding: 1rem;
    }
`;

const UserNameRow = styled.div`
    display: flex;
    align-items: center;
    gap: 1rem;
    justify-content: center;
    flex-wrap: wrap;
    width: 100%;
`;

const WinRateRow = styled.div`
    display: flex;
    justify-content: center;
    width: 100%;
`;

const ProfileTitle = styled.h3`
    font-size: 2rem;
    color: white;
    font-weight: 800;
    margin: 0;
    white-space: nowrap;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
`;

const CopyButton = styled.button`
    background: rgba(0, 0, 0, 0.3);
    color: white;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 600;
    transition: all 0.2s;
    white-space: nowrap;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);

    &:hover {
        background: rgba(0, 0, 0, 0.5);
        transform: translateY(-1px);
    }
`;

const SeasonWinRate = styled.div`
    font-size: 1rem;
    font-weight: 700;
    color: white;
    white-space: nowrap;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.8);
`;
