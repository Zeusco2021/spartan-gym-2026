package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.RankingEntry;
import com.spartangoldengym.social.dto.RankingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RankingService {

    private static final Logger log = LoggerFactory.getLogger(RankingService.class);
    private static final String RANKING_KEY_PREFIX = "ranking:";
    private static final Set<String> VALID_CATEGORIES = new HashSet<>(
            Arrays.asList("strength", "endurance", "consistency", "nutrition"));
    private static final long DEFAULT_TOP_N = 50;

    private final StringRedisTemplate redisTemplate;

    public RankingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateScore(String category, UUID userId, double score) {
        String key = RANKING_KEY_PREFIX + category;
        redisTemplate.opsForZSet().add(key, userId.toString(), score);
        log.debug("Updated ranking category={} userId={} score={}", category, userId, score);
    }

    public void incrementScore(String category, UUID userId, double delta) {
        String key = RANKING_KEY_PREFIX + category;
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), delta);
        log.debug("Incremented ranking category={} userId={} delta={}", category, userId, delta);
    }

    public RankingResponse getRanking(String category, Long topN) {
        long limit = (topN != null && topN > 0) ? topN : DEFAULT_TOP_N;
        String key = RANKING_KEY_PREFIX + category;

        // Reverse range: highest score first
        Set<ZSetOperations.TypedTuple<String>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        List<RankingEntry> entries = new ArrayList<>();
        int rank = 1;
        if (tuples != null) {
            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                UUID userId = UUID.fromString(tuple.getValue());
                double score = tuple.getScore() != null ? tuple.getScore() : 0.0;
                entries.add(new RankingEntry(rank++, userId, score));
            }
        }

        RankingResponse response = new RankingResponse();
        response.setCategory(category);
        response.setEntries(entries);
        return response;
    }

    public Set<String> getValidCategories() {
        return VALID_CATEGORIES;
    }
}
