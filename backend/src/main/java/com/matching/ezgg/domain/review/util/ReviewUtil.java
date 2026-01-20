package com.matching.ezgg.domain.review.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.matching.ezgg.domain.riotApi.dto.MatchReviewDto;

/**
 * 리뷰 관련 유틸리티 클래스
 */
public final class ReviewUtil {
	public static boolean isSameMatchIds(List<String> matchIds1, List<String> matchIds2) {
		if (matchIds1.size() != matchIds2.size()) {
			return false;
		}
		Set<String> set1 = new HashSet<>(matchIds1);
		Set<String> set2 = new HashSet<>(matchIds2);
		return set1.equals(set2);
	}
	/**
	 * 리스트1과 리스트2의 교집합을 구하는 메서드
	 * @param list1
	 * @param list2
	 * @return 교집합된 요소들의 리스트
	 */
	public static List<String> getCommonElements(List<String> list1, List<String> list2) {
		Set<String> set2 = new HashSet<>(list2);
		List<String> result = new ArrayList<>();
		for (String item : list1) {
			if (set2.contains(item)) {
				result.add(item);
			}
		}
		return result;
	}
	/**
	 * 내 팀의 ID를 가져오는 메서드
	 * @param matchReviewDtoList
	 * @param riotUsername
	 * @return 내 팀의 ID (100, 200)
	 */
	public static int getMyTeamId(List<MatchReviewDto> matchReviewDtoList, String riotUsername) {
		for (MatchReviewDto matchReviewDto : matchReviewDtoList) {
			if (matchReviewDto.getRiotUsername().equals(riotUsername)) {
				return matchReviewDto.getTeamId();
			}
		}
		return -1;
	}
	/**
	 * 리뷰 가능 여부를 확인하는 메서드
	 * @param matchReviewDtoList
	 * @param partnerRiotUsername
	 * @param myTeamId
	 * @return 가능하면 true, 불가능하면 false
	 */
	public static boolean existsReviewableUser(List<MatchReviewDto> matchReviewDtoList, String partnerRiotUsername,
		int myTeamId) {
		for(MatchReviewDto matchReviewDto : matchReviewDtoList) {
			if(matchReviewDto.getRiotUsername().equals(partnerRiotUsername) && matchReviewDto.getTeamId() == myTeamId) {
				return true;
			}
		}
		return false;
	}
}
