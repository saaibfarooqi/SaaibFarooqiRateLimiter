package com.saaib.ratelimiter;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.saaib.ratelimiter.service.SlidingWindowRateLimiter;

class SlidingWindowRateLimiterTest {

 @Test
 void shouldAllowWhenRedisReturnsOne() {
  var redis = mock(StringRedisTemplate.class);
  when(redis.execute(any(), any(List.class), any(), any(), any(), any()))
          .thenReturn(1L);

  assertTrue(new SlidingWindowRateLimiter(redis).allow("user1"));
 }

 @Test
 void shouldRejectWhenLimitExceeded() {
  var redis = mock(StringRedisTemplate.class);
  when(redis.execute(any(), any(List.class), any(), any(), any(), any()))
          .thenReturn(0L);

  assertFalse(new SlidingWindowRateLimiter(redis).allow("user2"));
 }

 @Test
 void shouldAllowWhenRedisThrowsException() {
  var redis = mock(StringRedisTemplate.class);
  when(redis.execute(any(), any(List.class), any(), any(), any(), any()))
          .thenThrow(new RuntimeException("Redis down"));

  // Fail-open: allow request if Redis is unavailable
  assertTrue(new SlidingWindowRateLimiter(redis).allow("user3"));
 }

 @Test
 void shouldHandleMultipleCallsSequence() {
  var redis = mock(StringRedisTemplate.class);
  when(redis.execute(any(), any(List.class), any(), any(), any(), any()))
          .thenReturn(1L, 1L, 1L, 1L, 1L, 0L);

  var limiter = new SlidingWindowRateLimiter(redis);

  assertTrue(limiter.allow("user4")); // 1st
  assertTrue(limiter.allow("user4")); // 2nd
  assertTrue(limiter.allow("user4")); // 3rd
  assertTrue(limiter.allow("user4")); // 4th
  assertTrue(limiter.allow("user4")); // 5th
  assertFalse(limiter.allow("user4")); // 6th → blocked
 }

 @Test
 void shouldEnforceLimitUnderConcurrentRequests() throws Exception {
  var redis = mock(StringRedisTemplate.class);
  when(redis.execute(any(), any(List.class), any(), any(), any(), any()))
          .thenReturn(1L, 1L, 1L, 1L, 1L, 0L, 0L);

  var limiter = new SlidingWindowRateLimiter(redis);

  ExecutorService executor = Executors.newFixedThreadPool(7);
  List<Future<Boolean>> results = executor.invokeAll(
          List.of(
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX"),
                  () -> limiter.allow("userX")
          )
  );
  executor.shutdown();

  long allowedCount = results.stream()
          .filter(f -> {
           try { return f.get(); } catch (Exception e) { return false; }
          })
          .count();

  assertEquals(5, allowedCount);
 }
}
