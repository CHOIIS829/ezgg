package com.matching.ezgg.domain.matchInfo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matching.ezgg.domain.matchInfo.entity.MatchInfo;

public interface MatchInfoRepository extends JpaRepository<MatchInfo, Long> {

	Optional<MatchInfo> findByMemberIdAndRiotMatchId(Long memberId, String matchId);
}
