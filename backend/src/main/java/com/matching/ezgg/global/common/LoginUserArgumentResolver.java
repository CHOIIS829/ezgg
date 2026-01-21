package com.matching.ezgg.global.common;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.matching.ezgg.global.annotation.LoginUser;
import com.matching.ezgg.domain.member.jwt.dto.CustomUserDetails;
import com.matching.ezgg.domain.member.entity.Member;

@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

	public Object resolveArgument(MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null; // 인증 안 됐으면 null 리턴
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomUserDetails userDetails) {
			return userDetails.getMemberId();
		}

		return null;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(LoginUser.class)
			&& (parameter.getParameterType().equals(Member.class) || parameter.getParameterType().equals(Long.class));
	}
}
