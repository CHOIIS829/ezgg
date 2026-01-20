package com.matching.ezgg.domain.review.entity;

import jakarta.persistence.Column;
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
@Table(name = "review")
public class Review {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "partner_member_id", nullable = false)
	private Long partnerMemberId;

	@Column(name = "partner_riot_username", nullable = false)
	private String partnerRiotUsername;

	@Column(name = "match_id", nullable = false)
	private String matchId;

	@Column(name = "review_score", nullable = false)
	private int reviewScore;

	@Builder
	public Review(Long memberId, Long partnerMemberId, String partnerRiotUsername, String matchId, int reviewScore) {
		this.memberId = memberId;
		this.partnerMemberId = partnerMemberId;
		this.partnerRiotUsername = partnerRiotUsername;
		this.matchId = matchId;
		this.reviewScore = reviewScore;
	}

	public void updateReviewScore(int reviewScore) {
		this.reviewScore = reviewScore;
	}
}
