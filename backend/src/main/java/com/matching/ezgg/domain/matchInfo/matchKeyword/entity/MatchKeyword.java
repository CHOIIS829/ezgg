package com.matching.ezgg.domain.matchInfo.matchKeyword.entity;

import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MatchKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", unique = false, nullable = false)
	private Long memberId;

	@Column(name = "riot_match_id", unique = false, nullable = false)
	private String riotMatchId;

	@Column(name = "keyword", unique = false, nullable = false)
	private String keyword;

	@Enumerated(EnumType.STRING)
	@Column(name = "lane", unique = false, nullable = false)
	private Lane lane;

	@Builder
	public MatchKeyword(Long id, Long memberId, String riotMatchId, String keyword, Lane lane) {
		this.id = id;
		this.memberId = memberId;
		this.riotMatchId = riotMatchId;
		this.keyword = keyword;
		this.lane = lane;
	}
}
