package com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JugMatchParsingDto {
	private Double visionScoreAdvantageLaneOpponent; // ≥0.5
	private Integer epicMonsterSteals; // ≥2
	private Integer enemyJungleMonsterKills; // ≥20
	private Integer riftHeraldTakedowns; // riftHeraldTakedowns + dragonTakedowns + baronTakedowns = 상대보다 ↑
	private Integer dragonTakedowns;
	private Integer baronTakedowns;
	private Integer opponentRiftHeraldTakeDowns;
	private Integer opponentDragonTakedowns;
	private Integer opponentBaronTakedowns;
	private Boolean firstBloodKill; // true
	private Double moreEnemyJungleThanOpponent; // +10 < 상대 moreEnemyJungleThanOpponent (값이 음수)
	private Double opponentMoreEnemyJungleThanOpponent;
	private Integer multiTurretRiftHeraldCount; // ≥2

}
