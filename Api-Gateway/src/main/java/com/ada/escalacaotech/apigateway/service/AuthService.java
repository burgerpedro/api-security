package com.ada.escalacaotech.apigateway.service;

import com.ada.escalacaotech.apigateway.repository.UserRepository;
import com.ada.escalacaotech.apigateway.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<String> authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.just(jwtService.generateToken(user.getUsername(), user.getRole()));
                    }
                    return Mono.empty();
                });
    }
}
