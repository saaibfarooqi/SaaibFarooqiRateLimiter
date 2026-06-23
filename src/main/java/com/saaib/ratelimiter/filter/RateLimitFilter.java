package com.saaib.ratelimiter.filter;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.saaib.ratelimiter.service.RateLimiterService;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService limiter;

    public RateLimitFilter(RateLimiterService limiter) {
        this.limiter = limiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            if (!limiter.allow(auth.getName())) {
                res.setStatus(429);
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
