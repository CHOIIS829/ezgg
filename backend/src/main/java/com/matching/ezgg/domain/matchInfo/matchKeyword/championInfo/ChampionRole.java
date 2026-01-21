package com.matching.ezgg.domain.matchInfo.matchKeyword.championInfo;

import lombok.Getter;

public enum ChampionRole {
	AD_BRUISER("AD 브루저"),
	AP_BRUISER("AP 브루저"),
	TANK("탱커"),
	AP_MAGE("AP 메이지"),
	AD_ASSASSIN("AD 암살자"),
	AP_ASSASSIN("AP 암살자"),
	AD_CARRY("AD 원거리 딜러"),
	SUPPORT("서포터"),
	JUNGLE("정글러"),
	AP_DEALER("AP 딜러"),
	AD_DEALER("AD 딜러");

	@Getter
	private final String koreanRoleName;

	ChampionRole(String koreanRoleName) {
		this.koreanRoleName = koreanRoleName;
	}
}