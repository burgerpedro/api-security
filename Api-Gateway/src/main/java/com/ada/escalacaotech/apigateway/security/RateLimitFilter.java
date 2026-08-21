package com.ada.escalacaotech.apigateway.security;

import io.github.bucket4j.Bucket;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
/**
 * Funcionalidade desenvolvida utilizando TDD (Red → Green → Refactor).
 * O rate limiting utiliza o usuário autenticado como identificador
 * quando disponível e o endereço IP como fallback.
 * Cenários protegidos por testes:
 * - Reutilização do bucket para o mesmo usuário;
 * - Buckets independentes para usuários diferentes;
 * - Fallback para IP sem autenticação;
 * - Retorno HTTP 429 quando o limite é excedido.
 */
@Component
public class RateLimitFilter implements WebFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    //TDD REFACTOR
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             WebFilterChain chain) {

        var remoteAddress = exchange.getRequest().getRemoteAddress();

        String ip = remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : null;

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .map(rateLimitService::resolveBucketForUser)
                .switchIfEmpty(
                        Mono.fromSupplier(
                                () -> rateLimitService.resolveBucket(ip)
                        )
                )
                .flatMap(bucket ->
                        processBucket(bucket, exchange, chain)
                );
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

        String body = """
            {
              "status":429,
              "error":"Too Many Requests",
              "message":"Rate limit exceeded"
            }
            """;

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse()
                .writeWith(Mono.just(buffer));
    }

    private Mono<Void> processBucket(
            Bucket bucket,
            ServerWebExchange exchange,
            WebFilterChain chain) {

        if (!bucket.tryConsume(1)) {
            return tooManyRequests(exchange);
        }

        return chain.filter(exchange);
    }
}