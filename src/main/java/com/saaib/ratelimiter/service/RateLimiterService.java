package com.saaib.ratelimiter.service;

public interface RateLimiterService {
 boolean allow(String userId);
}
