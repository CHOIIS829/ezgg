package com.matching.ezgg.domain.matchInfo.matchKeyword.keyword;

import lombok.Getter;

@Getter
public enum LanerKeyword {
	EARLY_ROAM("초반 로밍 적극적"), //killsOnOtherLanesEarlyJungleAsLaner, getTakedownsInAllLanesEarlyJungleAsLaner
	LANE_DOMINANCE("라인전 리드함"), //turretPlatesTaken(상대와 비교)
	TURRET_KILLS("포탑 철거 잘함"), //turretTakekills, turretLost
	TURRET_DEFENSE("포탑 다이브 버팀"), //killsUnderOwnTurret
	TURRET_DIVE("포탑 다이브 잘함"), //killsNearEnemyTurret
	CS_ADVANTAGE("CS 리드"), //maxCsAdvantageOnLaneOpponent
	FIRST_BLOOD_MAKER("첫 킬 만들어냄"), //firstBloodKill
	ASSASSIN("암살 잘함"); // killAfterHiddenWithAlly

	private final String description;

	LanerKeyword(String description) {
		this.description = description;
	}
}