package com.matching.ezgg.domain.review.service;

import static com.matching.ezgg.domain.review.util.ReviewUtil.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.matching.ezgg.domain.cancelPenalty.CancelPenaltyService;
import com.matching.ezgg.domain.matching.infra.redis.service.RedisService;
import com.matching.ezgg.domain.memberInfo.dto.MemberInfoDto;
import com.matching.ezgg.domain.memberInfo.service.MemberInfoService;
import com.matching.ezgg.domain.review.dto.CreateReviewDto;
import com.matching.ezgg.domain.review.dto.ReviewTimelineResponseDto;
import com.matching.ezgg.domain.review.entity.Review;
import com.matching.ezgg.domain.review.repository.ReviewRepository;
import com.matching.ezgg.domain.riotApi.dto.MatchReviewDto;
import com.matching.ezgg.domain.riotApi.service.ApiService;
import com.matching.ezgg.domain.riotApi.util.MatchMapper;
import com.matching.ezgg.global.exception.ReviewNotFoundException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

	private final ReviewRepository reviewRepository;
	private final MemberInfoService memberInfoService;
	private final ReviewNotificationService reviewNotificationService;
	private final RedisService redisService;
	private final ApiService apiService;
	private final MatchMapper matchMapper;
	private final CancelPenaltyService cancelPenaltyService;

	@Transactional
	public void findDuoGame(Long memberId1, Long memberId2, Map<String, String> updateMatchedUser) {
		MemberInfoDto memberInfoByMember1 = memberInfoService.getMemberInfoByMemberId(memberId1);
		MemberInfoDto memberInfoByMember2 = memberInfoService.getMemberInfoByMemberId(memberId2);
		List<String> findMember1MatchIds = apiService.getMemberMatchIds(memberInfoByMember1.getPuuid());
		List<String> findMember2MatchIds = apiService.getMemberMatchIds(memberInfoByMember2.getPuuid());

		List<String> matchIds1 = memberInfoByMember1.getMatchIds();
		List<String> matchIds2 = memberInfoByMember2.getMatchIds();

		if(isSameMatchIds(matchIds1, findMember1MatchIds) || isSameMatchIds(matchIds2, findMember2MatchIds)) {
			log.info("[INFO] {}와 {}은 새로운 게임이 존재하지 않음", memberInfoByMember1.getRiotUsername(), memberInfoByMember2.getRiotUsername());
			return;
		}

		List<String> commonElements = getCommonElements(findMember1MatchIds, findMember2MatchIds);
		log.info("[INFO] 공통된 게임 ID: {}", commonElements.toString());

		for(String matchId : commonElements) {
			String match = apiService.getMatch(matchId);
			List<MatchReviewDto> matchReviewDtoList = matchMapper.toMatchReviewDto(match);
			int myTeamId = getMyTeamId(matchReviewDtoList, memberInfoByMember1.getRiotUsername());
			if (existsReviewableUser(matchReviewDtoList, memberInfoByMember2.getRiotUsername(), myTeamId)) {
				// 리뷰 저장 (score는 0으로 초기화)
				reviewRepository.save(
					Review.builder()
						.memberId(memberId1)
						.partnerMemberId(memberId2)
						.partnerRiotUsername(memberInfoByMember2.getRiotUsername())
						.matchId(matchId)
						.reviewScore(0)
						.build()
				);
				reviewRepository.save(
					Review.builder()
						.memberId(memberId2)
						.partnerMemberId(memberId1)
						.partnerRiotUsername(memberInfoByMember1.getRiotUsername())
						.matchId(matchId)
						.reviewScore(0)
						.build()
				);
				// redis에서 실제 듀오게임 한 유저 삭제
				redisService.deleteMatchedUser(updateMatchedUser);
				cancelPenaltyService.resetCancelCount(memberId1);
				cancelPenaltyService.resetCancelCount(memberId2);

				// 리뷰 요청 전송
				reviewNotificationService.sendOrQueueReview(memberId1, memberInfoByMember1.getRiotUsername(), memberId2, memberInfoByMember2.getRiotUsername(), matchId);
				break;
			}
		}
	}

	public List<ReviewTimelineResponseDto> getRecentReviews(Long memberId) {
		List<Review> reviews = reviewRepository.findTop10ByMemberIdOrderByIdDesc(memberId);
		return reviews.stream()
			.map(this::toDto)
			.collect(Collectors.toList());
	}

	private ReviewTimelineResponseDto toDto(Review review) {
		return ReviewTimelineResponseDto.builder()
			.memberId(review.getMemberId())
			.partnerMemberId(review.getPartnerMemberId())
			.matchId(review.getMatchId())
			.build();
	}

	@Transactional
	public void createReview(Long memberId, CreateReviewDto createReviewDto) {
		Review findReview = reviewRepository.findByMemberIdAndMatchIdAndPartnerRiotUsername(
			memberId, createReviewDto.getMatchId(),
			createReviewDto.getPartnerRiotUsername())
			.orElseThrow(ReviewNotFoundException::new);

		if(findReview.getReviewScore() != 0) {
			log.info("[INFO] {}에 대한 리뷰가 이미 작성되었습니다.", createReviewDto.getPartnerRiotUsername());
			return;
		}

		findReview.updateReviewScore(createReviewDto.getReviewScore());
		reviewNotificationService.deletePendingReview(memberId, createReviewDto.getPartnerRiotUsername(),
			createReviewDto.getMatchId());
	}
}
