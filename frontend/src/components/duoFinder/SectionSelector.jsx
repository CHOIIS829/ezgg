/** -----------------------------------------------------------------------------
 * SectionSelector.jsx
 * -----------------------------------------------------------------------------
 * 매칭 조건 설정 폼에서 사용되는 다목적 셀렉터 컴포넌트.
 * - 라인(TOP/JUG/MID/AD/SUP) 선택, 챔피언 선호/비선호 선택, 사용자 메모(30자) 입력 세 가지 모드를 가짐.
 * - props 로 전달받은 matchingCriteria 를 직접 수정하지 않고, 반드시 setMatchingCriteria
 *   콜백을 통해 상위 상태를 업데이트한다.
 * ---------------------------------------------------------------------------*/
import styled from '@emotion/styled';
import React, {useState} from "react";
import ReactDOM from 'react-dom';
import {champions} from "../../data/champions.js";
import {keyword} from "../../data/keyword.js";
import {getKeywordWithEmoji} from "../../data/keywordEmojis.js";

/* ------------- 상수 ---------------------------------------------------------------- */

// 라인 버튼에 표시될 라벨 목록
const lines = ['TOP', 'JUG', 'MID', 'AD', 'SUP'];
// 사용자 메모 최대 글자 수
const TEXT_LIMIT = 30;

/* ------------- 보조 함수: 라인 선택 -------------------------------------------------------- */

/**
 * 라인 버튼 클릭 시 상태를 토글하는 함수
 * 같은 라인을 my/partner 양쪽에서 중복 선택하지 않도록 처리한다.
 * @param {('myLine'|'partnerLine')} type            - state 키(my 라인인지 partner 라인인지)
 * @param {string} line                              - 클릭된 라인 값
 * @param {object} matchingCriteria                  - 현재 매칭 기준 상태 객체
 * @param {Function} setMatchingCriteria             - 상태 업데이트 콜백
 */
const handleLineSelect = (type, line, matchingCriteria, setMatchingCriteria) => {
    const currentWantLine = matchingCriteria?.wantLine || {};
    const currentMyLine = currentWantLine.myLine || '';
    const currentPartnerLine = currentWantLine.partnerLine || '';

    // 중복 선택 상황 방지
    if (type === 'partnerLine' && line === currentMyLine) {
        alert('이미 내가 선택한 라인입니다.');
        return;
    }
    if (type === 'myLine' && line === currentPartnerLine) {
        alert('이미 상대방 라인으로 선택된 라인입니다.');
        return;
    }

    // 토글 로직
    const newWantLine = {
        ...currentWantLine,
        [type]: currentWantLine[type] === line ? '' : line
    };

    // 상대 라인이 변경될 때 userPreferenceText 초기화
    if (type === 'partnerLine') {
        const defaultKeywords = JSON.stringify({
            global: {},
            laner: {}
        });

        setMatchingCriteria({
            ...matchingCriteria,
            wantLine: newWantLine,
            userPreferenceText: defaultKeywords  // 플레이스타일 키워드 초기화
        });
    } else {
        setMatchingCriteria({
            ...matchingCriteria,
            wantLine: newWantLine
        });
    }
};

/* ------------- 챔피언 모달 컴포넌트 ---------------------------------------------------- */

const ChampionModal = ({isOpen, onClose, onSelect, kind, term, setTerm}) => {
    if (!isOpen) return null;

    const filteredChampions = champions.filter(champion =>
        !term ||
        champion.name.toLowerCase().includes(term.toLowerCase()) ||
        champion.id.toLowerCase().includes(term.toLowerCase())
    );

    return ReactDOM.createPortal(
        <ModalOverlay onClick={onClose}>
            <ModalContent onClick={e => e.stopPropagation()}>
                <ModalHeader>
                    <h3>{kind === 'preferred' ? '선호 챔피언 선택' : '비선호 챔피언 선택'}</h3>
                    <SearchInput>
                        <input
                            type="text"
                            placeholder="챔피언 검색..."
                            value={term}
                            onChange={e => setTerm(e.target.value)}

                            autoFocus
                        />
                    </SearchInput>
                </ModalHeader>
                <ChampionGrid>
                    {filteredChampions.map(champion => (
                        <ChampionCard
                            key={champion.id}
                            onClick={() => {
                                onSelect(champion);
                                onClose();
                            }}
                        >
                            <img src={`/champions/${champion.image}`} alt={champion.name}/>
                            <span>{champion.name}</span>
                        </ChampionCard>
                    ))}
                </ChampionGrid>
            </ModalContent>
        </ModalOverlay>,
        document.body
    );
};

/* ------------- 챔피언 선택 컴포넌트 ---------------------------------------------------- */

const ChampionSelector = ({kind, matchingCriteria, setMatchingCriteria}) => {
    const [showModal, setShowModal] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    const selectedChampions = kind === 'preferred'
        ? matchingCriteria.selectedChampions?.preferredChampions || []
        : matchingCriteria.selectedChampions?.bannedChampions || [];

    const handleChampionSelect = (champion) => {
        const key = kind === 'preferred' ? 'preferredChampions' : 'bannedChampions';
        const otherKey = kind === 'preferred' ? 'bannedChampions' : 'preferredChampions';
        const currentChampions = [...(matchingCriteria?.selectedChampions?.[key] || [])];
        const otherChampions = [...(matchingCriteria?.selectedChampions?.[otherKey] || [])];

        if (currentChampions.length >= 3) {
            alert('최대 3개까지만 선택할 수 있습니다.');
            return;
        }

        if (currentChampions.some(c => c.id === champion.id)) {
            alert('이미 선택한 챔피언입니다.');
            return;
        }

        if (otherChampions.some(c => c.id === champion.id)) {
            alert('동일한 챔피언이 선택되었습니다. 다시 확인해주세요.');
            return;
        }

        const newCriteria = {
            ...matchingCriteria,
            selectedChampions: {
                ...matchingCriteria?.selectedChampions,
                [key]: [...currentChampions, champion]
            }
        };

        setMatchingCriteria(newCriteria);
        setShowModal(false);
        setSearchTerm('');
    };

    const handleRemoveChampion = (championId) => {
        const key = kind === 'preferred' ? 'preferredChampions' : 'bannedChampions';

        const newCriteria = {
            ...matchingCriteria,
            selectedChampions: {
                ...matchingCriteria.selectedChampions,
                [key]: matchingCriteria.selectedChampions[key].filter(c => c.id !== championId)
            }
        };

        setMatchingCriteria(newCriteria);
    };

    return (
        <div>
            <SearchInput>
                <input
                    type="text"
                    placeholder={selectedChampions.length > 0
                        ? selectedChampions.map(c => c.name).join(', ')
                        : "챔피언을 선택하세요..."}
                    onClick={() => {
                        // 3개 이상 선택된 경우 모달 열지 않음
                        if (selectedChampions.length >= 3) {
                            alert('최대 3개까지만 선택할 수 있습니다.');
                            return;
                        }
                        setShowModal(true);
                    }}
                    readOnly
                />
            </SearchInput>
            <ChampionModal
                isOpen={showModal}
                onClose={() => {
                    setShowModal(false);
                    setSearchTerm('');
                }}
                onSelect={handleChampionSelect}
                kind={kind}
                term={searchTerm}
                setTerm={setSearchTerm}
            />
            <ChampionTags>
                <ChampionTagsHeader>
                    <span className="required">최소 1개 이상 선택해주세요</span>
                    {selectedChampions.length === 0 && (
                        <span className="warning">필수 항목입니다</span>
                    )}
                </ChampionTagsHeader>
                <ChampionTagsList>
                    {selectedChampions.map(champion => (
                        <ChampionTag key={champion.id}>
                            <img src={`/champions/${champion.image}`} alt={champion.name}/>
                            {champion.name}
                            <button
                                type="button"
                                onClick={() => handleRemoveChampion(champion.id)}
                            >
                                ×
                            </button>
                        </ChampionTag>
                    ))}
                </ChampionTagsList>
            </ChampionTags>
        </div>
    );
};

/* ------------- 메인 컴포넌트 ---------------------------------------------------------- */

/**
 * SectionSelector 컴포넌트
 * ---------------------------------------------------------------------------
 * 라인 선택 UI 혹은 챔피언 선택 UI를 렌더링하는 함수형 컴포넌트.
 *
 * @prop {object}   matchingCriteria          - 상위 컴포넌트의 상태 객체
 * @prop {Function} setMatchingCriteria       - 상태 변경 함수(상태를 불변성 지켜 갱신)

 * @prop {'line'|'champion'|'userPreferenceText'} type - 라인 선택 모드 or 챔피언 선택 모드 or 텍스트 입력 모드
 * @prop {'my'|'partner'|'preferred'|'banned'} kind - 세부 유형(라인: my/partner, 챔피언: preferred/banned)

 * @prop {string}   selectedValue             - (라인 모드) 현재 선택된 값
 * @prop {string}   disabledValue             - (라인 모드) 클릭 불가능한 값(상대가 이미 선택한 라인)
 */
const SectionSelector = ({
                             matchingCriteria,
                             setMatchingCriteria,
                             type,
                             kind,
                             selectedValue,
                             disabledValue
                         }) => {
    /* ---------------- 렌더링: 라인 선택 모드 --------------------------------- */
    if (type === 'line') {
        return (
            <Section>
                <Label>
                    {kind === 'my' ? '라인 선택' : '상대 라인 선택'}
                    <span style={{color: '#FF416C'}}>*</span>
                </Label>
                <LaneGroup>
                    <LaneButtonGroup>
                        {lines.map(line => (
                            <LaneButton
                                key={`${kind}-${line}`}
                                type="button"
                                selected={selectedValue === line}
                                disabled={disabledValue === line}
                                onClick={() => {
                                    handleLineSelect(
                                        kind === 'my' ? 'myLine' : 'partnerLine',
                                        line,
                                        matchingCriteria,
                                        setMatchingCriteria
                                    )
                                }}
                            >
                                {line}
                            </LaneButton>
                        ))}
                    </LaneButtonGroup>
                    {!selectedValue && (
                        <RequiredMessage>필수 항목입니다</RequiredMessage>
                    )}
                </LaneGroup>
            </Section>
        );
    } else if (type === 'champion') {
        return (
            <Section>
                <Label>{kind === 'preferred' ? '선호 챔피언' : '비선호 챔피언'} <span style={{color: '#FF416C'}}>*</span></Label>
                <ChampionSelector
                    kind={kind}
                    matchingCriteria={matchingCriteria}
                    setMatchingCriteria={setMatchingCriteria}
                />
            </Section>
        );
    } else if (type === 'userPreferenceText') {
        const selectedLine = matchingCriteria?.wantLine?.partnerLine || '';

        // 라인이 선택되지 않았으면 렌더링하지 않음
        if (!selectedLine) {
            return null;
        }

        // 라인에 따른 키워드 세트 결정
        const getKeywordsByLine = (line) => {
            const keywords = {...keyword.global};

            if (line === 'JUG') {
                return {...keywords, ...keyword.jungle};
            } else if (line === 'SUP') {
                return {...keywords, ...keyword.support};
            } else {
                return {...keywords, ...keyword.laner};
            }
        };

        const availableKeywords = getKeywordsByLine(selectedLine);
        let selectedKeywords = [];

        if (matchingCriteria.userPreferenceText) {
            try {
                const preferences = JSON.parse(matchingCriteria.userPreferenceText);
                selectedKeywords = [
                    ...Object.entries(preferences.global || {})
                        .filter(([, value]) => value === "매우 좋음")
                        .map(([key]) => key),
                    ...Object.entries(preferences.laner || {})
                        .filter(([, value]) => value === "매우 좋음")
                        .map(([key]) => key)
                ];
            } catch (e) {
                selectedKeywords = [];
            }
        }

        const handleKeywordClick = (value) => {
            const keywordIndex = selectedKeywords.indexOf(value);
            let newKeywords;

            // 키워드 추가 또는 제거
            if (keywordIndex === -1) {
                if (selectedKeywords.length >= 5) return;
                newKeywords = [...selectedKeywords, value];
            } else {
                newKeywords = selectedKeywords.filter((k) => k !== value);
            }

            // 모든 라인의 키워드를 global과 laner로 통일
            const globalKeywords = keyword.global;
            let lanerKeywords;

            // 라인별로 적절한 키워드 세트 선택
            if (selectedLine === 'JUG') {
                lanerKeywords = keyword.jungle;
            } else if (selectedLine === 'SUP') {
                lanerKeywords = keyword.support;
            } else {
                lanerKeywords = keyword.laner;
            }

            // JSON 객체 생성 (모든 라인이 global과 laner로 통일)
            const preferenceObject = {
                global: {},
                laner: {}
            };

            // global 키워드 처리
            Object.entries(globalKeywords).forEach(([, val]) => {
                preferenceObject.global[val] = newKeywords.includes(val) ? "매우 좋음" : "없음";
            });

            // laner 키워드 처리 (jungle이나 support 키워드도 laner로 저장)
            Object.entries(lanerKeywords).forEach(([, val]) => {
                preferenceObject.laner[val] = newKeywords.includes(val) ? "매우 좋음" : "없음";
            });

            setMatchingCriteria({
                ...matchingCriteria,
                userPreferenceText: JSON.stringify(preferenceObject)
            });
        };

        return (
            <Section>
                <Label>원하는 상대의 플레이 스타일 (최대 5개)</Label>
                <KeywordContainer>
                    {Object.entries(availableKeywords).map(([text, value]) => (
                        <KeywordButton
                            key={value}
                            type="button"
                            selected={selectedKeywords.includes(value)}
                            disabled={!selectedKeywords.includes(value) && selectedKeywords.length >= 5}
                            onClick={() => handleKeywordClick(value)}
                        >
                            {getKeywordWithEmoji(text)}
                        </KeywordButton>
                    ))}
                </KeywordContainer>
            </Section>
        );
    }

    return null;  // 방어 코드
};

const ChampionListContainer = styled.div`
    position: absolute;
    top: calc(100% + 4px);
    left: 0;
    right: 0;
    background: rgba(26, 26, 26, 0.95);
    border-radius: 4px;
    max-height: 200px;
    overflow-y: auto;
    z-index: 9999;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);
    outline: 1px solid rgba(255, 255, 255, 0.05);
    outline-offset: -2px;

    &::-webkit-scrollbar {
        width: 8px;
    }

    &::-webkit-scrollbar-track {
        background: transparent;
    }

    &::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 4px;
    }

    &::-webkit-scrollbar-thumb:hover {
        background: rgba(255, 255, 255, 0.3);
    }
`;

const ChampionListItem = styled.div`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem;
    cursor: pointer;
    color: white;
    transition: background-color 0.2s;
    background: ${props => props.selected ? 'rgba(255, 255, 255, 0.15)' : 'transparent'};

    &:hover {
        background: rgba(255, 255, 255, 0.15);
    }

    img {
        width: 24px;
        height: 24px;
        border-radius: 12px;
    }

    span {
        flex: 1;
    }
`;

/* ------------- 스타일 컴포넌트 --------------------------------------------- */

const Section = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    position: relative;
`;

const Label = styled.div`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: white;
    font-size: 0.9rem;
`;

const RequiredMessage = styled.div`
    color: #FF416C;
    font-size: 0.8rem;
    font-style: italic;
    margin-top: 0.2rem;
`;

const LaneGroup = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
`;

const LaneButtonGroup = styled.div`
    display: flex;
    gap: 0.5rem;
`;

const LaneButton = styled.button`
    flex: 1;
    padding: 0.5rem;
    border: none;
    background: ${props => props.selected ? '#FF416C' : props.disabled ? 'rgba(255, 255, 255, 0.05)' : 'rgba(255, 255, 255, 0.1)'};
    color: ${props => props.disabled ? 'rgba(255, 255, 255, 0.3)' : 'white'};
    border-radius: 4px;
    cursor: ${props => props.disabled ? 'not-allowed' : 'pointer'};
    font-size: 0.9rem;
    transition: all 0.2s;

    &:hover {
        background: ${props => props.selected ? '#FF416C' : props.disabled ? 'rgba(255, 255, 255, 0.05)' : 'rgba(255, 255, 255, 0.2)'};
    }
`;

const SearchInput = styled.div`
    position: relative;
    z-index: 1;

    input {
        width: 100%;
        padding: 0.5rem 2rem 0.5rem 0.8rem;
        background: rgba(255, 255, 255, 0.1);
        border: none;
        border-radius: 4px;
        color: white;
        font-size: 0.9rem;

        &::placeholder {
            color: rgba(255, 255, 255, 0.5);
        }

        &:focus {
            outline: none;
            background: rgba(255, 255, 255, 0.15);
        }
    }
`;

const ChampionTags = styled.div`
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    margin-top: 0.5rem;
`;

const ChampionTagsHeader = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;

    span.required {
        color: #FF416C;
        font-size: 0.8rem;
    }

    span.warning {
        color: #FF416C;
        font-size: 0.8rem;
        font-style: italic;
    }
`;

const ChampionTagsList = styled.div`
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
`;

const ChampionTag = styled.div`
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    color: white;
    font-size: 0.9rem;

    img {
        width: 20px;
        height: 20px;
        border-radius: 10px;
    }

    button {
        background: none;
        border: none;
        color: rgba(255, 255, 255, 0.7);
        cursor: pointer;
        padding: 0;
        font-size: 1.2rem;
        line-height: 1;

        &:hover {
            color: white;
        }
    }
`;

const KeywordContainer = styled.div`
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-top: 0.5rem;
`;

const KeywordButton = styled.button`
    padding: 0.5rem 1rem;
    background: ${props => props.selected ? '#FF416C' : 'rgba(255, 255, 255, 0.1)'};
    border: none;
    border-radius: 4px;
    color: ${props => props.disabled ? 'rgba(255, 255, 255, 0.3)' : 'white'};
    font-size: 0.9rem;
    cursor: ${props => props.disabled ? 'not-allowed' : 'pointer'};
    transition: all 0.2s;

    &:hover {
        background: ${props => props.selected ? '#FF416C' : props.disabled ? 'rgba(255, 255, 255, 0.1)' : 'rgba(255, 255, 255, 0.2)'};
    }
`;

const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
`;

const ModalContent = styled.div`
    background: #1a1a1a;
    border-radius: 8px;
    width: 90%;
    max-width: 800px;
    max-height: 80vh;
    overflow-y: auto;
    padding: 1.5rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.2);

    &::-webkit-scrollbar {
        width: 8px;
    }

    &::-webkit-scrollbar-track {
        background: transparent;
    }

    &::-webkit-scrollbar-thumb {
        background: rgba(255, 255, 255, 0.2);
        border-radius: 4px;
    }

    &::-webkit-scrollbar-thumb:hover {
        background: rgba(255, 255, 255, 0.3);
    }
`;

const ModalHeader = styled.div`
    margin-bottom: 1.5rem;

    h3 {
        color: white;
        margin: 0 0 1rem 0;
        font-size: 1.2rem;
    }
`;

const ChampionGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 1rem;
    padding: 0.5rem;
`;

const ChampionCard = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
        background: rgba(255, 255, 255, 0.2);
    }

    img {
        width: 48px;
        height: 48px;
        border-radius: 24px;
    }

    span {
        color: white;
        font-size: 0.9rem;
        text-align: center;
    }
`;

export default SectionSelector;
