import styled from '@emotion/styled';
import SectionSelector from "./SectionSelector.jsx";

const DuoFinderForm = (
    {
        matchingCriteria,
        setMatchingCriteria,
    }) => {

    return (
        <Form>
            <SectionSelector
                matchingCriteria={matchingCriteria}
                setMatchingCriteria={setMatchingCriteria}
                type={'line'}
                kind={'my'}
                selectedValue={matchingCriteria.wantLine.myLine}
                disabledValue={matchingCriteria.wantLine.partnerLine}
            />
            <SectionSelector
                matchingCriteria={matchingCriteria}
                setMatchingCriteria={setMatchingCriteria}
                type={'line'}
                kind={'partner'}
                selectedValue={matchingCriteria.wantLine.partnerLine}
                disabledValue={matchingCriteria.wantLine.myLine}
            />
            <SectionSelector
                matchingCriteria={matchingCriteria}
                setMatchingCriteria={setMatchingCriteria}
                type={'champion'}
                kind={'preferred'}
            />
            <SectionSelector
                matchingCriteria={matchingCriteria}
                setMatchingCriteria={setMatchingCriteria}
                type={'champion'}
                kind={'banned'}
            />
            <SectionSelector
                matchingCriteria={matchingCriteria}
                setMatchingCriteria={setMatchingCriteria}
                type={'userPreferenceText'}
            />
        </Form>
    );
};

export default DuoFinderForm;

const Form = styled.form`
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
`;
