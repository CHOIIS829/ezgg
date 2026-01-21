package com.matching.ezgg.domain.recentTwentyMatch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.matching.ezgg.domain.recentTwentyMatch.entity.RecentTwentyMatch;
import com.matching.ezgg.domain.recentTwentyMatch.dto.RecentTwentyMatchDto;
import com.matching.ezgg.domain.recentTwentyMatch.repository.RecentTwentyMatchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentTwentyMatchService {

	private final RecentTwentyMatchRepository recentTwentyMatchRepository;

	// memberId로 recentTwentyMatch 조회 // 엔티티가 없을 시 memberId만 박힌 빈 엔티티 리턴하도록 수정
	public RecentTwentyMatch getRecentTwentyMatchByMemberId(Long memberId) {
		return recentTwentyMatchRepository.findByMemberId(memberId)
			.orElseGet(() -> RecentTwentyMatch.builder()
				.memberId(memberId)
				.build());
		// .orElseThrow(RecentTwentyMatchNotFoundException::new);
	}

	@Transactional
	public RecentTwentyMatch upsertRecentTwentyMatch(RecentTwentyMatchDto recentTwentyMatchDto) {
		log.info("recentTwentyMatch 업데이트 시작");

		RecentTwentyMatch recentTwentyMatch = recentTwentyMatchRepository
			.findByMemberId(recentTwentyMatchDto.getMemberId())
					.orElseGet(() -> RecentTwentyMatch.builder()
						.memberId(recentTwentyMatchDto.getMemberId())
						.build());

		recentTwentyMatch.update(
			recentTwentyMatchDto.getSumKills(),
			recentTwentyMatchDto.getSumDeaths(),
			recentTwentyMatchDto.getSumAssists(),
			recentTwentyMatchDto.getChampionStats(),
			recentTwentyMatchDto.getWinRate(),
			recentTwentyMatchDto.getTopAnalysis(),
			recentTwentyMatchDto.getJugAnalysis(),
			recentTwentyMatchDto.getMidAnalysis(),
			recentTwentyMatchDto.getAdAnalysis(),
			recentTwentyMatchDto.getSupAnalysis()
		);

		// 새로 만든 경우에만 persist, 기존이면 dirty checking → UPDATE
		recentTwentyMatchRepository.save(recentTwentyMatch);

		log.info("recentTwentyMatch 업데이트 종료");
		return recentTwentyMatch;
	}
}
