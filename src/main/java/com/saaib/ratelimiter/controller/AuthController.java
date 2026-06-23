package com.saaib.ratelimiter.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import com.saaib.ratelimiter.dto.LoginRequest;
import com.saaib.ratelimiter.security.JwtUtil;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager auth;
    private final JwtUtil jwt;

    public AuthController(AuthenticationManager auth, JwtUtil jwt) {
        this.auth = auth;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest req) {

        auth.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));

        return ResponseEntity.ok(Map.of("token", jwt.generate(req.username())));
    }
}
