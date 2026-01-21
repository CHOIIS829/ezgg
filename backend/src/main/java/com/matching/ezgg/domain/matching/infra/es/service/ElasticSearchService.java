package com.matching.ezgg.domain.matching.infra.es.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.matching.ezgg.domain.matching.infra.es.index.MatchingUserDocument;
import com.matching.ezgg.global.exception.EsAccessFailException;
import com.matching.ezgg.global.exception.EsDocDeleteFailException;
import com.matching.ezgg.global.exception.EsDocNotFoundException;
import com.matching.ezgg.global.exception.EsPostFailException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class ElasticSearchService {

	private final ElasticsearchClient esClient;
	private final static String indexName = "matching-user";

	/**
	 * 주어진 {@code memberId}로 Elasticsearch 문서를 조회하는 메서드
	 *
	 * @param memberId 조회하려는 회원 ID
	 * @return 해당 회원의 {@link MatchingUserDocument} 객체
	 * @throws EsDocNotFoundException   문서를 찾지 못한 경우
	 * @throws EsAccessFailException    통신·직렬화 등 시스템 레벨 예외가 발생한 경우
	 */
	public MatchingUserDocument getDocByMemberId(Long memberId) {

		GetRequest getRequest = GetRequest.of(g -> g
			.index(indexName)
			.id(String.valueOf(memberId))
		);

		try {
			GetResponse<MatchingUserDocument> response =
				esClient.get(getRequest, MatchingUserDocument.class);

			if (response.found() && response.source() != null) {
				log.info("[INFO] 매칭 시도중인 유저의 ES Document 조회 성공: memberId={}", memberId);
				return response.source();
			}
			// NotFound Exception
			throw new EsDocNotFoundException(memberId);
		} catch (IOException e) {
			// 통신·직렬화 등 시스템 레벨 예외
			throw new EsAccessFailException(e);
		}
	}

	/**
	 * 매칭 조건 정보를 Elasticsearch에 저장(혹은 업데이트)하는 메서드
	 *
	 * @param matchingUserDocument 저장할 매칭 조건 DTO
	 * @throws EsPostFailException Elasticsearch에 문서 저장이 실패한 경우
	 */
	public void postDoc(MatchingUserDocument matchingUserDocument) {
		IndexRequest<MatchingUserDocument> postRequest = IndexRequest.of(r -> r
			.index(indexName)
			.id(String.valueOf(matchingUserDocument.getMemberId()))
			.document(matchingUserDocument));
		try {
			IndexResponse response = esClient.index(postRequest);
			log.info("[INFO] ES 저장: {}", response);
		} catch (IOException e) {
			log.error("[ERROR] Elasticsearch Doc 저장 중 IOException 발생");
			throw new EsPostFailException();
		}
	}

	/**
	 * 주어진 {@code memberId}를 ID로 갖는 Elasticsearch 문서를 삭제하는 메서드
	 *
	 * @param memberId 삭제하려는 회원 ID
	 * @throws EsDocDeleteFailException 삭제 과정에서 IOException이 발생한 경우
	 */
	public void deleteDocByMemberId(Long memberId) {
		try {
			DeleteRequest deleteRequest = DeleteRequest.of(d -> d
				.index(indexName)
				.id(String.valueOf(memberId)) // memberId를 직접 문서 ID로 사용
			);
			DeleteResponse response = esClient.delete(deleteRequest);
			log.info("[INFO] Doc 삭제 완료: {}", response);
		} catch (IOException e) {
			log.error("[ERROR] Elasticsearch Doc 삭제 중 IOException 발생", e);
			throw new EsDocDeleteFailException();
		}
	}
}
