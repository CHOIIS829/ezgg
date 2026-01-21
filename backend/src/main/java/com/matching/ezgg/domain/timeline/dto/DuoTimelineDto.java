package com.matching.ezgg.domain.timeline.dto;

import java.util.List;
import java.util.Map;

import com.matching.ezgg.domain.matchInfo.dto.TimelineMatchInfoDto;
import com.matching.ezgg.domain.memberInfo.dto.TimelineMemberInfoDto;

import lombok.Data;

@Data
public class DuoTimelineDto {

	private List<DuoFrameEventDto> timeline;
	private Map<Integer, UserMatchInfoDto> userMatchInfos;

	@Data
	public static class UserMatchInfoDto {
		private TimelineMemberInfoDto timelineMemberInfoDto;
		private TimelineMatchInfoDto timelineMatchInfoDto;
	}

	@Data
	public static class DuoFrameEventDto {
		private long timestamp;
		private List<TimelineEventDto> events;
		private Map<Integer, ParticipantStatusDto> participantStatus;
	}

	@Data
	public static class TimelineEventDto {
		private String type;
		private Integer killerId;
		private Integer victimId;
		private List<Integer> assistingParticipantIds;
		private String detail; // ex: SPECIAL_KILL
	}

	@Data
	public class ParticipantStatusDto {
		private int level;
		private int totalGold;
		private int minionsKilled;
		private int jungleMinionsKilled;
		private DamageStatsDto damageStats;
	}

	@Data
	public class DamageStatsDto {
		private int totalDamageDealt;
		private int totalDamageTaken;
		private int damageToChampions;
	}
}
