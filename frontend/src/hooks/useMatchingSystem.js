import {useCallback, useEffect, useRef, useState} from 'react';
import {isValidCriteria} from '../utils/validation.js';
import {getInitialCriteria} from "../utils/initialStates.js";


export const useMatchingSystem = ({
    socket,
    sendMatchingRequest,
    sendCancelRequest,
}) => {

    const [matchResult, setMatchResult] = useState(null);
    const [matchingCriteria, setMatchingCriteria] = useState(getInitialCriteria());
    const [isMatching, setIsMatching] = useState(false);

    // 사용자가 직접 취소했는지 구분하는 ref
    const userCancelledRef = useRef(false);
    const isUnmountingRef = useRef(false);

    const handleMatchStart = useCallback((criteria) => {
        if (!isValidCriteria(criteria)) {
            alert('라인과 선호/비선호 챔피언은 필수로 선택해주세요.');
            return;
        }

        if (!socket) {
            alert('웹소켓 연결이 필요합니다.');
            return;
        }

        setIsMatching(true);
        setMatchingCriteria(criteria);
        userCancelledRef.current = false;
        sendMatchingRequest(criteria);
    }, [socket, sendMatchingRequest]);

    const handleMatchCancel = useCallback(() => {

        userCancelledRef.current = true;

        if (isMatching) {
            // 매칭 중일 때만 취소 요청을 백엔드로 전송
            sendCancelRequest();
        }

        // 상태 초기화
        setMatchResult(null);
        setIsMatching(false);
        setMatchingCriteria(getInitialCriteria());
    }, [isMatching, sendCancelRequest]);

    // 상태 완전 초기화 함수 (로그아웃 시 사용)
    const resetMatchingState = useCallback(() => {
        setIsMatching(false);
        setMatchResult(null);
        setMatchingCriteria(getInitialCriteria());
    }, []);


    useEffect(() => {
        return () => {
            isUnmountingRef.current = true;

            // 사용자가 직접 취소하지 않은 상태에서 매칭 중이라면
            // 새로고침이나 페이지 이동일 가능성이 높으므로 취소하지 않음
            if (isMatching && !userCancelledRef.current) {
                console.log('새로고침/페이지이동 감지, 매칭 상태 유지');
                // 취소 요청을 보내지 않음
                return;
            }
            // 사용자가 직접 취소했거나 로그아웃한 경우에만 취소 요청
            if (isMatching && userCancelledRef.current) {
                console.log('사용자 의도적 취소, 매칭 취소 요청');
                sendCancelRequest();
            }
        };
    }, [isMatching, sendCancelRequest]);

    useEffect(() => {
        if (matchResult?.matched) {
            userCancelledRef.current = false;
        }
    }, [matchResult]);

    return {
        matchResult,
        setMatchResult,
        matchingCriteria,
        setMatchingCriteria,
        isMatching,
        setIsMatching,
        handleMatchStart,
        handleMatchCancel,
        resetMatchingState,
    };
};
