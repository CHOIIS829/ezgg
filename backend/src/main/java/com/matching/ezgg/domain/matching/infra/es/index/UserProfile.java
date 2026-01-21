package com.matching.ezgg.domain.matching.infra.es.index;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 벡터값이 없을 때 필드 자체를 넣지 않기 위해(null) 선언
public class UserProfile {

	private String riotUsername;
	private String riotTag;
	@Field(type = FieldType.Keyword)
	private String tier;

	@Field(type = FieldType.Object)
	private RecentTwentyMatchStats recentTwentyMatchStats;

	//Scaled_Float = 내부에서 정수값으로 저장하고 검색.조회시 자동으로 실수로 변환
	//scalingFactor = 10 이면 소숫점 1자리를 기준으로 10배로 곱해서 저장하는 것
	// ex) 4.3 -> es내부 저장값 = 43 -> 검색/조회시 다시 받는 값 = 4.3
	@Field(type = FieldType.Scaled_Float, scalingFactor = 10)
	private Double reviewScore;
	//TODO
	// 저장할 때, BigDecimal로 반올림하여 정확히 한 자리로 맞춘 뒤 저장
	// BigDecimal score = BigDecimal.valueOf(4.34)
	// 								.setScale(1, RoundingMode.HALF_UP); // 4.3
	// document.setReviewScore(score.doubleValue());

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class RecentTwentyMatchStats {
		private List<String> most3Champions;
		@Field(type = FieldType.Dense_Vector, dims = 1536)
		private float[] topAnalysisVector;
		@Field(type = FieldType.Dense_Vector, dims = 1536)
		private float[] jugAnalysisVector;
		@Field(type = FieldType.Dense_Vector, dims = 1536)
		private float[] midAnalysisVector;
		@Field(type = FieldType.Dense_Vector, dims = 1536)
		private float[] adAnalysisVector;
		@Field(type = FieldType.Dense_Vector, dims = 1536)
		private float[] supAnalysisVector;
	}
}
