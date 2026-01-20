package com.matching.ezgg.domain.memberInfo.entity;

import java.util.List;

import com.matching.ezgg.global.util.StringListConverter;
import com.matching.ezgg.global.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_info")
public class MemberInfo extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", unique = true, nullable = false)
	private Long memberId;

	@Column(name = "riot_username", unique = false, nullable = false)
	private String riotUsername;

	@Column(name = "riot_tag", unique = false, nullable = false)
	private String riotTag;

	@Column(name = "puuid", unique = true, nullable = true)
	private String puuid;

	@Column(name = "match_ids", unique = false, nullable = true, length = 1000)//TODO 정규화 필요
	@Convert(converter = StringListConverter.class)
	private List<String> matchIds;

	@Column(name = "tier", unique = false, nullable = true)
	private String tier;

	@Column(name = "tier_num", unique = false, nullable = true)
	private String tierNum;

	@Column(name = "wins", unique = false, nullable = true)
	private Integer wins;

	@Column(name = "losses", unique = false, nullable = true)
	private Integer losses;

	@Builder
	public MemberInfo(Long memberId, String riotUsername, String riotTag, String puuid,
		List<String> matchIds, String tier, String tierNum, Integer wins, Integer losses) {
		this.memberId = memberId;
		this.riotUsername = riotUsername;
		this.riotTag = riotTag;
		this.puuid = puuid;
		this.matchIds = matchIds;
		this.tier = tier;
		this.tierNum = tierNum;
		this.wins = wins;
		this.losses = losses;
	}

	public void update(String tier, String tierNum, int wins, int losses, List<String> fetchedMatchIds, boolean existsNewMatchIds){
		this.tier = tier;
		this.tierNum = tierNum;
		this.wins = wins;
		this.losses = losses;
		if(existsNewMatchIds){
			this.matchIds = fetchedMatchIds;
		}
	}
}
