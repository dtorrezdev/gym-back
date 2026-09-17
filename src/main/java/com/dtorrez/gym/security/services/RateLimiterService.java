package com.dtorrez.gym.security.services;

import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

@Service
public class RateLimiterService {

    @Value("${bucket4j.limit}")
    private Long limitPeticiones;

    @Value("${bucket4j.duration.minutes}")
    private Long duracion;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    // private static final Logger log =
    // LogManager.getLogger(RateLimiterService.class);

    public Bucket resolveBucket(String key) {
        // log.info("****dtn limitPeticiones={}, duracion={}",limitPeticiones,duracion);
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(limitPeticiones,
                    Refill.greedy(limitPeticiones, Duration.ofMinutes(duracion)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }
}
