package com.matching.ezgg.domain.recentTwentyMatch.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matching.ezgg.domain.recentTwentyMatch.entity.RecentTwentyMatch;

public interface RecentTwentyMatchRepository extends JpaRepository<RecentTwentyMatch, Long> {
	boolean existsByMemberId(Long matchId);

	Optional<RecentTwentyMatch> findByMemberId(Long matchId);
}
