package com.matching.ezgg.domain.matching.infra.redis.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CancelData {
	private int count;
	private long timestamp;
}
