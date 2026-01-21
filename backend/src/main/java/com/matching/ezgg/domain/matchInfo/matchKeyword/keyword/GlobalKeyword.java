package com.matching.ezgg.domain.matchInfo.matchKeyword.keyword;

import lombok.Getter;

@Getter
public enum GlobalKeyword {
	HIGH_KILL_PARTICIPATION("킬 관여율 높음"), //killParticipation
	HIGH_KDA("kda 높음"), //kda
	CLUTCH_WINNER("게임 캐리함"), //teamDamagePercent, win
	HIGH_DAMAGE("딜 잘 넣음"), //damagePerMinute
	LEVEL_DIFF("레벨 차이 냄"), //maxLevelLeadLaneOpponent
	GOOD_SYNERGY("스킬 연계 잘함"), //immobilizeAndKillWithAlly
	NEVER_GIVES_UP("항복 안 함"), //gameEndedInSurrender
	SURVIVABILITY("생존 잘함"), //longestTimeSpentLiving
	TEAM_FIGHTER("한타 잘함"), //multiKillOneSpell
	COOPERATIVE("협동 잘함"), // pickKillWithAlly
	INVADING("인베이드 잘함"), // takedownsBeforeJungleMinionSpawn
	COMEBACK_WIN("위기 후 승리"); // lostAnInhibitor, win

	private final String description;

	GlobalKeyword(String description) {
		this.description = description;
	}
}
