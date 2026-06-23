package com.saaib.ratelimiter.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class SlidingWindowRateLimiter implements RateLimiterService {

 private final StringRedisTemplate redis;

 // Configurable properties from application.properties
 @Value("${rate.limit.window:60000}") // default 60 seconds
 private long windowMillis;

 @Value("${rate.limit.max:5}") // default 5 requests
 private int maxRequests;

 public SlidingWindowRateLimiter(StringRedisTemplate redis) {
  this.redis = redis;
 }

 @Override
 public boolean allow(String userId) {
  String script =
          "redis.call('ZREMRANGEBYSCORE', KEYS[1], 0, ARGV[1]-ARGV[2]) " +
                  "local c = redis.call('ZCARD', KEYS[1]) " +
                  "if c >= tonumber(ARGV[3]) then return 0 end " +
                  "redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4]) " +
                  "redis.call('PEXPIRE', KEYS[1], ARGV[2]) " +
                  "return 1";

  DefaultRedisScript<Long> lua = new DefaultRedisScript<>();
  lua.setScriptText(script);
  lua.setResultType(Long.class);

  try {
   Long result = redis.execute(
           lua,
           Collections.singletonList("rate:" + userId),
           String.valueOf(System.currentTimeMillis()), // ARGV[1]
           String.valueOf(windowMillis),               // ARGV[2]
           String.valueOf(maxRequests),                // ARGV[3]
           UUID.randomUUID().toString()                // ARGV[4]
   );

   return Long.valueOf(1).equals(result);
  } catch (Exception e) {
   // Redis down or execution error → fail open (allow request)
   System.err.println("Rate limiter error: " + e.getMessage());
   return true;
  }
 }
}
