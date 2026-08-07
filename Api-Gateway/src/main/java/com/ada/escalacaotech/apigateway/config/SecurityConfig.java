package com.ada.escalacaotech.apigateway.config;

import com.ada.escalacaotech.apigateway.security.JwtAuthenticationFilter;
import com.ada.escalacaotech.apigateway.security.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, RateLimitFilter rateLimitFilter, JwtAuthenticationFilter jwtAuthenticationFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .redirectToHttps(Customizer.withDefaults())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .headers(headers -> headers
                        .hsts(hsts -> hsts
                                .includeSubdomains(true)
                                .maxAge(Duration.ofDays(365))
                        )
                        .frameOptions(frameOptions -> frameOptions.mode(Mode.SAMEORIGIN))
                        .xssProtection(xss -> xss.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "img-src 'self' data:; " +
                                        "font-src 'self'; " +
                                        "connect-src 'self'; " +
                                        "frame-ancestors 'none';"
                        ))
                )
                .addFilterAt(rateLimitFilter, SecurityWebFiltersOrder.FIRST)
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            String body = """
                                    {
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Autenticação necessária"
                                    }
                                    """;

                            DataBuffer buffer = exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(body.getBytes(StandardCharsets.UTF_8));

                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            String body = """
                                    {
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Acesso não autorizado"
                                    }
                                    """;

                            DataBuffer buffer = exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(body.getBytes(StandardCharsets.UTF_8));

                            return exchange.getResponse().writeWith(Mono.just(buffer));
                        })
                )
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/pedidos/admin").hasRole("ADMIN")
                        .pathMatchers("/api/pedidos/**").hasAnyRole("USER", "ADMIN")
                        .anyExchange().permitAll()
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:8443"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}