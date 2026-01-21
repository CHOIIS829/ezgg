package com.matching.ezgg.domain.matching.infra.es.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.matching.ezgg.domain.matching.infra.es.index.MatchingUserDocument;

@Repository
public interface MatchingUserRepository extends ElasticsearchRepository<MatchingUserDocument, Long> {
} 
