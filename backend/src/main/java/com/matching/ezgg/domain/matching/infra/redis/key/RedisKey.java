package com.matching.ezgg.domain.matching.infra.redis.key;

public enum RedisKey {
    STREAM_KEY("matching-stream"),
    STREAM_ID_HASH_KEY("stream-id-hash"),
    STREAM_GROUP("matching-group"),
    CONSUMER_NAME ("matching-consumer"),
    RETRY_ZSET_KEY("matching-retry-zset"),
    MATCHED_ZSET_KEY("matching-success-zset"),
    DELETE_QUEUE_KEY("matching-delete-queue"),
	REVIEW_PENDING_KEY("review-pending-list");

	private final String value;

	RedisKey(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
