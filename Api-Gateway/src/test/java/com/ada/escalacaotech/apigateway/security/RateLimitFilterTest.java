package com.ada.escalacaotech.apigateway.security;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private Bucket bucket;

    @Mock
    private WebFilterChain chain;

    @InjectMocks
    private RateLimitFilter rateLimitFilter;

    @Test
    public void shouldUseAuthenticatedUsernameForRateLimit() {

        var request = MockServerHttpRequest
                .get("/api/pedidos")
                .remoteAddress(
                        new java.net.InetSocketAddress("127.0.0.1", 8080)
                )
                .build();

        var exchange = MockServerWebExchange.from(request);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        null,
                        null
                );

        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(rateLimitService.resolveBucketForUser("admin"))
                .thenReturn(bucket);

        when(rateLimitService.resolveBucket(anyString()))
                .thenReturn(bucket);

        when(bucket.tryConsume(1))
                .thenReturn(true);

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        rateLimitFilter.filter(exchange, chain)
                .contextWrite(
                        ReactiveSecurityContextHolder
                                .withSecurityContext(
                                        Mono.just(securityContext)
                                )
                )
                .block();

        verify(rateLimitService)
                .resolveBucketForUser("admin");
    }


    @Test
    void shouldUseIpWhenUserIsNotAuthenticated() {

        var request = MockServerHttpRequest
                .get("/api/pedidos")
                .remoteAddress(
                        new java.net.InetSocketAddress("127.0.0.1", 8080)
                )
                .build();

        var exchange = MockServerWebExchange.from(request);

        when(rateLimitService.resolveBucket("127.0.0.1"))
                .thenReturn(bucket);

        when(bucket.tryConsume(1))
                .thenReturn(true);

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        rateLimitFilter.filter(exchange, chain)
                .block();

        verify(rateLimitService)
                .resolveBucket("127.0.0.1");

        verify(rateLimitService, never())
                .resolveBucketForUser(anyString());
    }

    @Test
    void shouldReturnTooManyRequestsWhenRateLimitIsExceeded() {

        var request = MockServerHttpRequest
                .get("/api/pedidos")
                .remoteAddress(
                        new java.net.InetSocketAddress("127.0.0.1", 8080)
                )
                .build();

        var exchange = MockServerWebExchange.from(request);

        when(rateLimitService.resolveBucket("127.0.0.1"))
                .thenReturn(bucket);

        when(bucket.tryConsume(1))
                .thenReturn(false);

        rateLimitFilter.filter(exchange, chain)
                .block();

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                exchange.getResponse().getStatusCode()
        );

        verify(chain, never()).filter(exchange);
    }
}
