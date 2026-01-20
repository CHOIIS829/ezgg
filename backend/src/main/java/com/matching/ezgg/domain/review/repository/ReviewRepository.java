package com.matching.ezgg.domain.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matching.ezgg.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	Optional<Review> findByMemberIdAndMatchIdAndPartnerRiotUsername(Long memberId, String matchId, String partnerRiotUsername);
	List<Review> findTop10ByMemberIdOrderByIdDesc(Long memberId);
}
