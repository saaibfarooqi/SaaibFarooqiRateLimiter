package com.saaib.ratelimiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

 @GetMapping("/hello")
 public ResponseEntity<String> hello(){
   return ResponseEntity.ok("Api Secured with Jwt ");
 }
}
