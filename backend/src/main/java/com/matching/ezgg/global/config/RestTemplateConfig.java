package com.matching.ezgg.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

	@Bean("asia")
	public RestTemplate asiaRestTemplate(RestTemplateBuilder b) {
		return b
			.rootUri("https://asia.api.riotgames.com") // base URL
			.build();
	}

	@Bean("kr")
	public RestTemplate krRestTemplate(RestTemplateBuilder b) {
		return b
			.rootUri("https://kr.api.riotgames.com")
			.build();
	}
}
