package com.matching.ezgg.domain.riotApi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.matching.ezgg.domain.matchInfo.dto.TimelineMatchInfoDto;
import com.matching.ezgg.domain.memberInfo.dto.TimelineMemberInfoDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.GlobalMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.JugMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.LanerMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.dto.parsingDto.SupMatchParsingDto;
import com.matching.ezgg.domain.matchInfo.matchKeyword.lane.Lane;
import com.matching.ezgg.domain.riotApi.dto.MatchDto;
import com.matching.ezgg.domain.riotApi.dto.MatchReviewDto;
import com.matching.ezgg.domain.timeline.dto.DuoTimelineDto;
import com.matching.ezgg.domain.timeline.dto.DuoTimelineDto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchMapper {
	private final ObjectMapper objectMapper;

	public MatchDto toMatchDto(String rawJson, Long memberId, String puuid) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);

			// matchId 지정
			String riotMatchId = root.path("metadata").path("matchId").asText();

			// 타겟 유저 정보 노드 검색
			JsonNode targetMemberNode = findTargetMember(root.path("info").path("participants"), puuid);

			return MatchDto.builder()
				.memberId(memberId)
				.riotMatchId(riotMatchId)
				.kills(targetMemberNode.path("kills").asInt())
				.deaths(targetMemberNode.path("deaths").asInt())
				.assists(targetMemberNode.path("assists").asInt())
				.teamPosition(targetMemberNode.path("teamPosition").asText())
				.championName(targetMemberNode.path("championName").asText())
				.win(targetMemberNode.path("win").asBoolean())
				.build();

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → MatchDto 변환 실패", e);
		}
	}

	// DuoTimeline에 사용될 데이터 추출 메서드
	public DuoTimelineDto extractDuoTimelineDto(
		JsonNode timelineData, TimelineMemberInfoDto memberInfoDto, TimelineMemberInfoDto duoMemberInfoDto,
		TimelineMatchInfoDto timelineMatchInfoDto, TimelineMatchInfoDto duoTimelineMatchInfoDto
	) {
		Map<Integer, UserMatchInfoDto> userMatchInfos = extractUserMatchInfos(
			timelineData.path("participants"), memberInfoDto, duoMemberInfoDto, timelineMatchInfoDto,
			duoTimelineMatchInfoDto
		);
		List<DuoTimelineDto.DuoFrameEventDto> timeline = extractDuoFrameEvents(
			timelineData.path("frames"), userMatchInfos.keySet()
		);

		DuoTimelineDto dto = new DuoTimelineDto();
		dto.setTimeline(timeline);
		dto.setUserMatchInfos(userMatchInfos);
		return dto;
	}

	private Map<Integer, UserMatchInfoDto> extractUserMatchInfos(
		JsonNode participantsNode, TimelineMemberInfoDto memberInfo, TimelineMemberInfoDto duoMemberInfo,
		TimelineMatchInfoDto memberMatchInfo, TimelineMatchInfoDto duoMemberMatchInfo
	) {
		Map<Integer, UserMatchInfoDto> map = new HashMap<>();

		for (JsonNode participant : participantsNode) {
			String puuid = participant.path("puuid").asText();
			int participantId = participant.path("participantId").asInt();

			if (puuid.equals(memberInfo.getPuuid())) {
				DuoTimelineDto.UserMatchInfoDto info = new DuoTimelineDto.UserMatchInfoDto();
				info.setTimelineMemberInfoDto(memberInfo);
				info.setTimelineMatchInfoDto(memberMatchInfo);
				map.put(participantId, info);
			} else if (puuid.equals(duoMemberInfo.getPuuid())) {
				DuoTimelineDto.UserMatchInfoDto info = new DuoTimelineDto.UserMatchInfoDto();
				info.setTimelineMemberInfoDto(duoMemberInfo);
				info.setTimelineMatchInfoDto(duoMemberMatchInfo);
				map.put(participantId, info);
			}
		}

		return map;
	}

	private List<DuoTimelineDto.DuoFrameEventDto> extractDuoFrameEvents(
		JsonNode framesNode, Set<Integer> participantIds) {
		List<DuoTimelineDto.DuoFrameEventDto> result = new ArrayList<>();

		for (JsonNode frameNode : framesNode) {
			List<DuoTimelineDto.TimelineEventDto> events = new ArrayList<>();

			for (JsonNode eventNode : frameNode.path("events")) {
				String type = eventNode.path("type").asText();
				if (isTargetEvent(type) && isDuoRelated(eventNode, participantIds)) {
					events.add(convertToTimelineEventDto(eventNode, type));
				}
			}

			// todo participantStatus 추출 (현재는 빈 Map, 향후 구현 필요)
			Map<Integer, DuoTimelineDto.ParticipantStatusDto> participantStatus = new HashMap<>();

			if (events.isEmpty() && participantStatus.isEmpty()) {
				continue;
			}

			DuoTimelineDto.DuoFrameEventDto frameEvent = new DuoTimelineDto.DuoFrameEventDto();
			frameEvent.setTimestamp(frameNode.path("timestamp").asLong());
			frameEvent.setEvents(events);
			frameEvent.setParticipantStatus(participantStatus);

			result.add(frameEvent);
		}

		return result;
	}

	private DuoTimelineDto.TimelineEventDto convertToTimelineEventDto(JsonNode eventNode, String type) {
		DuoTimelineDto.TimelineEventDto dto = new DuoTimelineDto.TimelineEventDto();
		dto.setType(type);
		if (eventNode.has("killerId"))
			dto.setKillerId(eventNode.get("killerId").asInt());
		if (eventNode.has("victimId"))
			dto.setVictimId(eventNode.get("victimId").asInt());
		if (eventNode.has("assistingParticipantIds")) {
			List<Integer> assists = new ArrayList<>();
			eventNode.get("assistingParticipantIds").forEach(a -> assists.add(a.asInt()));
			dto.setAssistingParticipantIds(assists);
		}
		if (type.equals("CHAMPION_SPECIAL_KILL") && eventNode.has("killType")) {
			dto.setDetail(eventNode.get("killType").asText());
		}
		return dto;
	}

	private boolean isDuoRelated(JsonNode eventNode, Set<Integer> participantIdSet) {
		return (eventNode.has("participantId") && participantIdSet.contains(eventNode.get("participantId").asInt())) ||
			(eventNode.has("killerId") && participantIdSet.contains(eventNode.get("killerId").asInt())) ||
			(eventNode.has("victimId") && participantIdSet.contains(eventNode.get("victimId").asInt())) ||
			(eventNode.has("assistingParticipantIds") &&
				StreamSupport.stream(eventNode.get("assistingParticipantIds").spliterator(), false)
					.map(JsonNode::asInt)
					.anyMatch(participantIdSet::contains));
	}

	private boolean isTargetEvent(String type) {
		return Set.of(
			"CHAMPION_KILL",
			"CHAMPION_SPECIAL_KILL",
			"ELITE_MONSTER_KILL",
			"BUILDING_KILL",
			"TURRET_PLATE_DESTROYED"
		).contains(type);
	}

	// participant 배열에서 puuid가 일치하는 노드 리턴
	private JsonNode findTargetMember(JsonNode participants, String puuid) {
		// map 혹은 stream으로 metadata node에 있는 puuid로 검색할 수도 있지만, 현재 상황(1명의 info만 검색)에서는 for 루프의 성능이 가장 좋다!
		for (JsonNode participant : participants) {
			if (puuid.equals(participant.path("puuid").asText())) {
				return participant;
			}
		}
		throw new IllegalArgumentException("해당 Match에서 멤버를 찾을 수 없습니다.");
	}

	public List<MatchReviewDto> toMatchReviewDto(String rawJson) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			List<MatchReviewDto> matchReviewDtoList = new ArrayList<>();

			JsonNode participants = root.path("info").path("participants");

			for (JsonNode participant : participants) {

				MatchReviewDto matchReviewDto = MatchReviewDto.builder()
					.riotUsername(participant.path("riotIdGameName").asText())
					.riotTag(participant.path("riotIdTagline").asText())
					.teamId(participant.path("teamId").asInt())
					.build();

				matchReviewDtoList.add(matchReviewDto);
			}

			return matchReviewDtoList;
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → MathReviewDto 변환 실패", e);
		}
	}

	// participant 배열에서 상대 라인의 노드 리턴
	private JsonNode findOpponentMember(JsonNode participants, String puuid, String teamPosition) {
		for (JsonNode participant : participants) {
			if (!puuid.equals(participant.path("puuid").asText()) && teamPosition.equals(
				participant.path("teamPosition").asText())) {
				return participant;
			}
		}
		throw new IllegalArgumentException("해당 Match에서 멤버를 찾을 수 없습니다.");
	}

	// participant 배열에서 puuid가 일치하는 노드에서 challenges 노드 리턴
	private JsonNode findChallenges(JsonNode memberNode) {
		JsonNode challengesNode = memberNode.path("challenges");
		if (challengesNode.isMissingNode() || challengesNode.isNull()) {
			throw new IllegalArgumentException("해당 멤버의 Challenges를 찾을 수 없습니다.");
		}
		return challengesNode;
	}

	/**
	 * 팀에서 TeamDamagePercentage가 가장 높은지 확인하는 메서드
	 * @param root
	 * @param puuid
	 * @return 가장 높으면 True, 아니면 False
	 */
	private boolean isTopTeamDamagePercentPlayer(JsonNode root, String puuid) {

		JsonNode globalNode = findTargetMember(root.path("info").path("participants"), puuid);

		boolean isTopTeamDamagePercentage = false;
		int teamId = globalNode.path("teamId").asInt();
		String participantPuuid = "";
		double maxDamage = 0.0;

		JsonNode info = root.get("info");
		JsonNode participants = info.get("participants");

		for (JsonNode participant : participants) {
			int participantTeamId = participant.path("teamId").asInt();

			if (participantTeamId == teamId) {
				JsonNode challenges = participant.path("challenges");
				if (challenges.has("teamDamagePercentage")) {
					double damage = challenges.get("teamDamagePercentage").doubleValue();

					if (damage > maxDamage) {
						maxDamage = damage;
						participantPuuid = participant.path("puuid").asText();
					}
				}
			}

		}
		if (participantPuuid.equals(puuid)) {
			isTopTeamDamagePercentage = true;
		}
		return isTopTeamDamagePercentage;
	}

	public GlobalMatchParsingDto toGlobalMatchParsingDto(String rawJson, String puuid) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			JsonNode globalNode = findTargetMember(root.path("info").path("participants"), puuid);
			JsonNode challengesNode = findChallenges(globalNode);

			return GlobalMatchParsingDto.builder()
				.win(globalNode.path("win").asBoolean())
				.damagePerMinute(challengesNode.path("damagePerMinute").asDouble())
				.gameEndedInSurrender(globalNode.path("gameEndedInSurrender").asBoolean())
				.killParticipation(challengesNode.path("killParticipation").asDouble())
				.kda(challengesNode.path("kda").asDouble())
				.bestTeamDamagePercentage(isTopTeamDamagePercentPlayer(root, puuid))
				.maxLevelLeadLaneOpponent(challengesNode.path("maxLevelLeadLaneOpponent").asInt())
				.immobilizeAndKillWithAlly(challengesNode.path("immobilizeAndKillWithAlly").asInt())
				.multiKillOneSpell(challengesNode.path("multiKillOneSpell").asInt())
				.lostAnInhibitor(challengesNode.path("lostAnInhibitor").asInt())
				.takedownsBeforeJungleMinionSpawn(challengesNode.path("takedownsBeforeJungleMinionSpawn").asInt())
				.pickKillWithAlly(challengesNode.path("pickKillWithAlly").asInt())
				.longestTimeSpentLiving(globalNode.path("longestTimeSpentLiving").asInt())
				.gameDuration(root.path("info").path("gameDuration").asInt())
				.build();

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → GlobalMatchParsingDto 변환 실패", e);
		}
	}

	public LanerMatchParsingDto toLanerMatchParsingDto(String rawJson, String puuid, String teamPosition) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			JsonNode lanerNode = findTargetMember(root.path("info").path("participants"), puuid);
			JsonNode lanerChallengesNode = findChallenges(lanerNode);

			//상대 라이너 정보 파싱
			JsonNode opponentLanerNode = findOpponentMember(
				root.path("info").path("participants"), puuid,
				teamPosition
			);
			JsonNode opponentLanerChallengesNode = findChallenges(opponentLanerNode);

			return LanerMatchParsingDto.builder()
				.turretKills(lanerNode.path("turretKills").asInt())
				.turretsLost(lanerNode.path("turretsLost").asInt())
				.firstBloodKill(lanerNode.path("firstBloodKill").asBoolean())
				.killsOnOtherLanesEarlyJungleAsLaner(
					lanerChallengesNode.path("killsOnOtherLanesEarlyJungleAsLaner").asInt())
				.getTakedownsInAllLanesEarlyJungleAsLaner(
					lanerChallengesNode.path("getTakedownsInAllLanesEarlyJungleAsLaner").asInt())
				.turretPlatesTaken(lanerChallengesNode.path("turretPlatesTaken").asInt())
				.killsUnderOwnTurret(lanerChallengesNode.path("killsUnderOwnTurret").asInt())
				.killsNearEnemyTurret(lanerChallengesNode.path("killsNearEnemyTurret").asInt())
				.maxCsAdvantageOnLaneOpponent(lanerChallengesNode.path("maxCsAdvantageOnLaneOpponent").asDouble())
				.opponentTurretPlatesTaken(opponentLanerChallengesNode.path("turretPlatesTaken").asInt())
				.opponentTurretsLost(opponentLanerNode.path("turretsLost").asInt())
				.killAfterHiddenWithAlly(lanerChallengesNode.path("killAfterHiddenWithAlly").asInt())
				.build();

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → LanerMatchParsingDto 변환 실패", e);
		}
	}

	public JugMatchParsingDto toJugMatchParsingDto(String rawJson, String puuid) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);
			JsonNode jugNode = findTargetMember(root.path("info").path("participants"), puuid);
			JsonNode jugChallengesNode = findChallenges(jugNode);

			//상대 정글 정보 파싱
			JsonNode opponentJugNode = findOpponentMember(
				root.path("info").path("participants"), puuid,
				Lane.JUNGLE.name()
			);
			JsonNode opponentJugChallengesNode = findChallenges(opponentJugNode);

			return JugMatchParsingDto.builder()
				.visionScoreAdvantageLaneOpponent(
					jugChallengesNode.path("visionScoreAdvantageLaneOpponent").doubleValue())
				.epicMonsterSteals(jugChallengesNode.path("epicMonsterSteals").asInt())
				.enemyJungleMonsterKills(jugChallengesNode.path("enemyJungleMonsterKills").asInt())
				.riftHeraldTakedowns(jugChallengesNode.path("riftHeraldTakedowns").asInt())
				.dragonTakedowns(jugChallengesNode.path("dragonTakedowns").asInt())
				.baronTakedowns(jugChallengesNode.path("baronTakedowns").asInt())
				.opponentRiftHeraldTakeDowns(opponentJugChallengesNode.path("riftHeraldTakedowns").asInt())
				.opponentDragonTakedowns(opponentJugChallengesNode.path("dragonTakedowns").asInt())
				.opponentBaronTakedowns(opponentJugChallengesNode.path("baronTakedowns").asInt())
				.firstBloodKill(jugNode.path("firstBloodKill").asBoolean())
				.moreEnemyJungleThanOpponent(jugChallengesNode.path("moreEnemyJungleThanOpponent").asDouble())
				.opponentMoreEnemyJungleThanOpponent(
					opponentJugChallengesNode.path("moreEnemyJungleThanOpponent").asDouble())
				.multiTurretRiftHeraldCount(jugChallengesNode.path("multiTurretRiftHeraldCount").asInt())
				.build();

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → JugMatchParsingDto 변환 실패", e);
		}
	}

	public SupMatchParsingDto toSupMatchParsingDto(String rawJson, String puuid) {
		try {
			JsonNode root = objectMapper.readTree(rawJson);

			//participants 노드 안에 있는 요소 파싱
			JsonNode supNode = findTargetMember(root.path("info").path("participants"), puuid);

			//challenges 노드 안에 있는 요소 파싱
			JsonNode supChallengesNode = findChallenges(supNode);

			return SupMatchParsingDto.builder()
				.wardPlaced(supNode.path("wardsPlaced").asInt())
				.assists(supNode.path("assists").asInt())
				.visionScoreAdvantageLaneOpponent(supChallengesNode.path("visionScoreAdvantageLaneOpponent").asDouble())
				.saveAllyFromDeath(supChallengesNode.path("saveAllyFromDeath").asInt())
				.gameDuration(root.path("info").path("gameDuration").asInt())
				.build();

		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("JSON → SupMatchParsingDto 변환 실패", e);
		}
	}
}
