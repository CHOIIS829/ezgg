package com.matching.ezgg.domain.matchInfo.matchKeyword.keyword;

import lombok.Getter;

@Getter
public enum SupKeyword {
	VISION_DOMINANCE("시야 장악"), //visionScoreAdvantageLaneOpponent
	WARDS_PLACED("시야 밝혀줌"), //wardsPlaced, gameDuration
	SAVE_ALLY("팀원 살림"), //saveAllyFromDeath
	ASSIST_KING("킬 도움"); // assists

	private final String description;

	SupKeyword(String description) {
		this.description = description;
	}
}
