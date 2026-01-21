package com.matching.ezgg.domain.member.jwt.repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refreshToken:";
    private static final String BLACKLIST_TOKEN_PREFIX = "blacklist:";

    /**
     * Refresh Token을 Redis에 저장합니다.
     * key는 uuid로 설정하고, value는 memberUsername과 refreshToken을 Map으로 저장합니다.
     */
    public void save(String uuid, String memberUsername, String refreshToken, long expirationTimeMs) {
        String key = REFRESH_TOKEN_PREFIX + uuid;
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("memberUsername", memberUsername);
        valueMap.put("refreshToken", refreshToken);

        redisTemplate.opsForHash().putAll(key, valueMap);
        redisTemplate.expire(key, expirationTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * uuid로 Redis에서 Refresh Token을 조회합니다.
     */
    public String findRefreshTokenByUUID(String uuid) {
        String key = REFRESH_TOKEN_PREFIX + uuid;
        Object token = redisTemplate.opsForHash().get(key, "refreshToken");
        return token != null ? token.toString() : null;
    }

    /**
     * uuid로 Redis에서 memberUsername을 조회합니다.
     */
    public String findMemberUsernameByUUID(String uuid) {
        String key = REFRESH_TOKEN_PREFIX + uuid;
        Object username = redisTemplate.opsForHash().get(key, "memberUsername");
        return username != null ? username.toString() : null;
    }

    /**
     * uuid에 해당하는 Refresh Token을 삭제합니다.
     */
    public void deleteByUUID(String uuid) {
        String key = REFRESH_TOKEN_PREFIX + uuid;
        redisTemplate.delete(key);
    }

    /**
     * 특정 Refresh Token이 Redis에 존재하는지 확인합니다.
     */
    public boolean existsByRefreshToken(String refreshToken) {
        Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_PREFIX + "*");
        if (keys == null) return false;

        for (String key : keys) {
            Object storedToken = redisTemplate.opsForHash().get(key, "refreshToken");
            if (refreshToken.equals(storedToken)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Access Token을 블랙리스트에 추가합니다.
     * 토큰이 만료될 때까지만 블랙리스트에 유지합니다.
     */
    public void addToBlacklist(String accessToken, long remainingTimeMs) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        redisTemplate.opsForValue().set(key, "logout");
        redisTemplate.expire(key, remainingTimeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Access Token이 블랙리스트에 있는지 확인합니다.
     */
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_TOKEN_PREFIX + accessToken;
        return redisTemplate.hasKey(key);
    }
} 
