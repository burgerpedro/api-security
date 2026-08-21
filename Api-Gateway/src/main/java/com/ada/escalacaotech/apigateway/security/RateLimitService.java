package com.ada.escalacaotech.apigateway.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Suporte à funcionalidade de Rate Limiting desenvolvida com TDD.
 * Os buckets são reutilizados para o mesmo identificador
 * e independentes para identificadores diferentes.
 */
@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey) {
        return buckets.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String ip) {

        Bandwidth limit = Bandwidth.builder()
                .capacity(5)
                .refillGreedy(5, Duration.ofMinutes(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    //TDD
    public Bucket resolveBucketForUser(String username) {
        return resolveBucket("USER:" + username);
    }

}
