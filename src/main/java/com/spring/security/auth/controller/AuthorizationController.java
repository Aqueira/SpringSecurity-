package com.spring.security.auth.controller;

import com.spring.security.auth.service.AuthorizationService;
import com.spring.security.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    @PostMapping("/registration")
    public void registration(@RequestBody User user) {
        authorizationService.register(user);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authorization(@RequestBody User user) {
        return ResponseEntity.ok(authorizationService.login(user));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody String token) {
        return ResponseEntity.ok(authorizationService.refreshAccessToken(token));
    }

}
