package com.matching.ezgg.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupResponse {

	private String memberUsername;
	private String email;
	private String riotUsername;
	private String riotTag;

	@Builder
	public SignupResponse(String memberUsername, String email, String riotUsername, String riotTag) {
		this.memberUsername = memberUsername;
		this.email = email;
		this.riotUsername = riotUsername;
		this.riotTag = riotTag;
	}
}
