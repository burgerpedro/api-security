package com.ada.escalacaotech.apigateway.security;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitServiceTest {

    @InjectMocks
    RateLimitService rateLimitService;

    @Test
    void shouldCreateBucketDifferentForDifferentUsers() {
        Bucket adminBucket = rateLimitService.resolveBucket("admin");
        Bucket userBucket = rateLimitService.resolveBucket("user");

        assertNotSame(adminBucket, userBucket);
    }

    @Test
    void shouldCreateSameBucketForSameUser() {
        Bucket firstBucket = rateLimitService.resolveBucket("user");
        Bucket secondBucket = rateLimitService.resolveBucket("user");

        assertSame(firstBucket, secondBucket);
    }

    @Test
    void shouldBlockSixthRequest() {

        Bucket bucket = rateLimitService.resolveBucket("user");

        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));

        assertFalse(bucket.tryConsume(1));
    }

    @Test
    void shouldCreateDifferentBucketsForDifferentKeys() {

        Bucket admin = rateLimitService.resolveBucket("USER:admin");
        Bucket user = rateLimitService.resolveBucket("USER:user");

        assertNotSame(admin, user);
    }

    @Test
    void shouldUseDifferentBucketForUserAndIp() {

        Bucket userBucket = rateLimitService.resolveBucket("USER:admin");
        Bucket ipBucket = rateLimitService.resolveBucket("IP:admin");

        assertNotSame(userBucket, ipBucket);
    }

    //TDD
    @Test
    void shouldResolveBucketForAuthenticatedUser() {

        Bucket first = rateLimitService.resolveBucketForUser("admin");
        Bucket second = rateLimitService.resolveBucketForUser("admin");

        assertSame(first, second);
    }
    //TDD
    @Test
    void shouldResolveDifferentBucketsForDifferentUsers() {

        Bucket admin = rateLimitService.resolveBucketForUser("admin");
        Bucket user = rateLimitService.resolveBucketForUser("user");

        assertNotSame(admin, user);
    }

    @Test
    void shouldShareRateLimitBetweenRequestsFromSameUser() {

        Bucket firstRequest =
                rateLimitService.resolveBucketForUser("admin");

        Bucket secondRequest =
                rateLimitService.resolveBucketForUser("admin");

        assertSame(firstRequest, secondRequest);

        assertTrue(firstRequest.tryConsume(1));
        assertTrue(secondRequest.tryConsume(1));
        assertTrue(secondRequest.tryConsume(1));
        assertTrue(secondRequest.tryConsume(1));
        assertTrue(secondRequest.tryConsume(1));

        assertFalse(secondRequest.tryConsume(1));
    }




}
