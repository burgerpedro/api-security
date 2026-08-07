package com.ada.escalacaotech.apigateway.controller;

import com.ada.escalacaotech.apigateway.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody Mono<Map<String, String>> credentialsMono) {

        return credentialsMono.flatMap(credentials -> {
            String username = credentials.get("username");
            String password = credentials.get("password");

                      return authService.authenticate(username, password)
                    .map(token -> ResponseEntity.ok(Map.of("token", token)))
                    .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Credenciais inválidas")));
        });
    }
}
