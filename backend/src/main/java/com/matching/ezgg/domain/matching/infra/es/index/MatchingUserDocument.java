package com.matching.ezgg.domain.matching.infra.es.index;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(indexName = "matching-user", createIndex = false)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingUserDocument {
	// 필수조건: memberId, riot Id, 태그, 티어, 내 라인, 상대 라인
	// 가중치 조건: 사용자가 입력한 조건 벡터, 라인별 키워드 벡터, 선호 챔피언명 리스트, 비선호 챔피언명 리스트, 모스트 3 챔피언명 리스트, 리뷰
	@Id
	private Long memberId;

	@Field(type = FieldType.Object)
	private UserProfile userProfile;// riot Id 태그, 티어, 모스트 3 챔피언명 리스트, 라인별 키워드 벡터, 리뷰

	@Field(type = FieldType.Object)
	private PartnerPreference partnerPreference; // 사용자가 입력한 조건 벡터, 내 라인, 상대 라인, 선호 챔피언명 리스트, 비선호 챔피언명 리스트

	@Setter
	private float matchingScore;
}
