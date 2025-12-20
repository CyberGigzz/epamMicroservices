package com.gym.crm.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.gym.crm.security.JwtUtil; 
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final Cache<String, Boolean> blacklistCache;

    public TokenBlacklistService() {
        blacklistCache = CacheBuilder.newBuilder()
                .expireAfterWrite(JwtUtil.JWT_TOKEN_VALIDITY, TimeUnit.MILLISECONDS)
                .maximumSize(1000) 
                .build();
    }

    public void blacklistToken(String token) {
        blacklistCache.put(token, true);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistCache.getIfPresent(token) != null;
    }
}