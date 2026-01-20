package com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GlobalMatchParsingDto {
	private Double killParticipation; //≥ 40% ≥ 60% ≥ 50% ≥ 50% ≥ 60%
	private Double kda; //≥ 4.5 ≥ 4.0 ≥ 4.5.≥ 4.5 ≥ 3.5
	private Boolean bestTeamDamagePercentage; //팀 내 teamDamagePercent 1위 && win = True
	private Boolean win;
	private Double damagePerMinute; //≥ 800 ≥ 800 ≥ 900 ≥ 900 ≥ 400
	private Integer maxLevelLeadLaneOpponent; //2 이상
	private Integer immobilizeAndKillWithAlly; //5 이상
	private Boolean gameEndedInSurrender; //False
	private Integer multiKillOneSpell; //1 이상
	private Integer pickKillWithAlly; //20 이상
	private Integer lostAnInhibitor; // lostAnInhibitor>=2 && win=True
	private Integer takedownsBeforeJungleMinionSpawn; // >= 2
	private Integer longestTimeSpentLiving;
	private Integer gameDuration;
}
