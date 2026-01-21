package com.matching.ezgg.domain.matchInfo.matchKeyword.keyword;

import lombok.Getter;

@Getter
public enum JugKeyword {
	VISION_DOMINANCE("시야 장악"), //visionScoreAdvantageLaneOpponent
	OBJECTIVE_STEALER("오브젝트 스틸 잘함"), //epicMonsterSteals
	COUNTER_JUNGLER("카운터 정글 잘함"), //enemyJungleMonsterKills
	OBJECTIVE_TAKER("오브젝트 잘 먹음"), //riftHeraldTakedowns, dragonKills, baronKills
	FIRST_BLOOD_MAKER("첫 킬 만들어냄"), //firstBloodKill
	JUNGLE_DOMINANCE("적 정글 장악"), // moreEnemyJungleThanOpponent
	RIFT_HERALD_UTILIZER("전령 활용 잘함"); // multiTurretRiftHeraldCount

	private final String description;

	JugKeyword(String description) {
		this.description = description;
	}
}
