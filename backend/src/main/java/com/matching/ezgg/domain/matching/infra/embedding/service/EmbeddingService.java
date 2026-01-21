package com.matching.ezgg.domain.matching.infra.embedding.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
	private final EmbeddingModel embeddingModel;

	public float[] embed(String text) {
		// text가 null·공백·탭·줄바꿈 뿐이면 null, 그렇지 않으면 공백을 trim한 뒤 임베딩 배열 반환
		// null을 리턴하면 document에서 해당 필드 자체가 없애 vector 쿼리에서 에러가 안나게한다.
		if (text == null || text.trim().isEmpty()) {
			return null;                    // ← dense_vector 필드 생략용
		}
		return embeddingModel.embed(text);
	}
}
