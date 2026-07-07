package com.urlshortener.cache;

import com.urlshortener.dto.FullUrl;
import com.urlshortener.dto.ShortUrl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
public class UrlCacheAspect {

    private static final Logger logger = LoggerFactory.getLogger(UrlCacheAspect.class);

    private static final String SHORT_TO_FULL_PREFIX = "url:s2f:";
    private static final String FULL_TO_SHORT_PREFIX = "url:f2s:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public UrlCacheAspect(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("execution(* com.urlshortener.service.UrlService.getFullUrl(String)) && args(shortenString)")
    public Object aroundGetFullUrl(ProceedingJoinPoint joinPoint, String shortenString) throws Throwable {
        String key = SHORT_TO_FULL_PREFIX + shortenString;

        String cached = safeGet(key);
        if (cached != null) {
            logger.info("Cache HIT for short code '{}'", shortenString);
            return new FullUrl(cached);
        }

        logger.info("Cache MISS for short code '{}'", shortenString);
        Object result = joinPoint.proceed();
        if (result instanceof FullUrl) {
            safeSet(key, ((FullUrl) result).getFullUrl());
        }
        return result;
    }

    @Around("execution(* com.urlshortener.service.UrlService.getShortUrl(..)) && args(fullUrl)")
    public Object aroundGetShortUrl(ProceedingJoinPoint joinPoint, FullUrl fullUrl) throws Throwable {
        String originalUrl = fullUrl.getFullUrl();
        String key = FULL_TO_SHORT_PREFIX + originalUrl;

        String cached = safeGet(key);
        if (cached != null) {
            logger.info("Cache HIT for full url '{}'",
        originalUrl);
            return new ShortUrl(cached);
        }

        logger.info("Cache MISS for full url '{}'",
        originalUrl);
        Object result = joinPoint.proceed();
        if (result instanceof ShortUrl) {
            safeSet(key, ((ShortUrl) result).getShortUrl());
        }
        return result;
    }

    private String safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.warn("Redis unavailable on read for key '{}' ({}); bypassing cache", key, e.getMessage());
            return null;
        }
    }

    private void safeSet(String key, String value) {
        if (value == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, TTL);
        } catch (Exception e) {
            logger.warn("Redis unavailable on write for key '{}' ({}); skipping cache populate", key, e.getMessage());
        }
    }
}
