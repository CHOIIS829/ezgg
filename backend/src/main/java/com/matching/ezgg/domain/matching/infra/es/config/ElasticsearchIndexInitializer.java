package com.matching.ezgg.domain.matching.infra.es.config;

import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.matching.infra.es.index.MatchingUserDocument;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements ApplicationListener<ContextClosedEvent> {

	private final ElasticsearchOperations elasticsearchOperations;

	@PostConstruct
	public void createIndex() {
		IndexOperations indexOps = elasticsearchOperations.indexOps(MatchingUserDocument.class);
		// log.info("인덱스 존재 여부: {}", indexOps.exists()); // 서버 부팅 과부화 시 확인 필요
		if (!indexOps.exists()) {
			indexOps.create(); // 빈 인덱스 생성

			Map<String, Object> mapping = Map.of(
				"properties", Map.of(
					// userProfile
					"userProfile", Map.of(
						"properties", Map.of(
							// tier ⇒ keyword 타입으로 매핑
							"tier", Map.of("type", "keyword"),

							"recentTwentyMatchStats", Map.of(
								"properties", Map.of(
									"most3Champions", Map.of("type", "keyword"),
									"topAnalysisVector", Map.of("type", "dense_vector", "dims", 1536),
									"jugAnalysisVector", Map.of("type", "dense_vector", "dims", 1536),
									"midAnalysisVector", Map.of("type", "dense_vector", "dims", 1536),
									"adAnalysisVector",  Map.of("type", "dense_vector", "dims", 1536),
									"supAnalysisVector", Map.of("type", "dense_vector", "dims", 1536)
								)
							),
							// reviewScore ⇒ scaled_float 타입으로 매핑
							"reviewScore", Map.of("type", "scaled_float", "scaling_factor", 10)
						)
					),

					// partnerPreference
					"partnerPreference", Map.of(
						"properties", Map.of(
							"userPreferenceTextVector", Map.of(
								"type", "dense_vector", "dims", 1536
							),
							"lineRequirements", Map.of(
								"properties", Map.of(
									// myLine / partnerLine ⇒ keyword 타입으로 매핑
									"myLine",      Map.of("type", "keyword"),
									"partnerLine", Map.of("type", "keyword")
								)
							),
							"championsPreference", Map.of(
								"properties", Map.of(
									// 선호/비선호 챔피언 배열 ⇒ keyword 타입으로 매핑
									"preferredChampions",   Map.of("type", "keyword"),
									"unpreferredChampions", Map.of("type", "keyword")
								)
							)
						)
					)
				)
			);

			Document document = Document.from(mapping);
			indexOps.putMapping(document);
			log.info("Elasticsearch 인덱스 + dense_vector 매핑 생성 완료");
		}
	}

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		IndexOperations indexOps = elasticsearchOperations.indexOps(MatchingUserDocument.class);
		if (indexOps.exists()) {
			indexOps.delete();
			log.info("Elasticsearch 인덱스 삭제 완료");
		}
	}
}
