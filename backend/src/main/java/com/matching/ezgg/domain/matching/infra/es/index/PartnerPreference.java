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
public class PartnerPreference {

	@Field(type = FieldType.Dense_Vector, dims = 1536)
	private float[] userPreferenceTextVector;

	@Field(type = FieldType.Object)
	private LineRequirements lineRequirements;

	@Field(type = FieldType.Object)
	private ChampionsPreference championsPreference;

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class LineRequirements {
		@Field(type = FieldType.Keyword)
		private String myLine;
		@Field(type = FieldType.Keyword)
		private String partnerLine;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ChampionsPreference {
		private List<String> preferredChampions;
		private List<String> unpreferredChampions;
	}

}
