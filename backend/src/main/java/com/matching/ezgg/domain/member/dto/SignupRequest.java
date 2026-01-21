package com.matching.ezgg.domain.member.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

	private String memberUsername;
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()-+=])[A-Za-z\\d!@#$%^&*()\\-+=]{6,20}$", message = "패스워드 6~20자의 영문 대소문자,숫자,특수문자 하나 이상 포함되어야합니다.")
	private String password;
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()-+=])[A-Za-z\\d!@#$%^&*()\\-+=]{6,20}$", message = "패스워드 6~20자의 영문 대소문자,숫자,특수문자 하나 이상 포함되어야합니다.")
	private String confirmPassword;
	private String email;
	private String riotUsername;
	private String riotTag;

	@Builder
	public SignupRequest(String memberUsername, String password, String confirmPassword, String email,
		String riotUsername, String riotTag) {
		this.memberUsername = memberUsername;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.email = email;
		this.riotUsername = riotUsername;
		this.riotTag = riotTag;
	}
}
