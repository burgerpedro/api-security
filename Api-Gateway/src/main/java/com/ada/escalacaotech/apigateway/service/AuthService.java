package com.ada.escalacaotech.apigateway.service;

import com.ada.escalacaotech.apigateway.repository.UserRepository;
import com.ada.escalacaotech.apigateway.security.JwtService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public Mono<String> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (user.getPassword().equals(password)) {
                        return Mono.just(jwtService.generateToken(user.getUsername(), user.getRole()));
                    }
                    return Mono.empty();
                });
    }
}
