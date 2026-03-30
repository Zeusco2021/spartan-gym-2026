package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.RankingEntry;
import com.spartangoldengym.social.dto.RankingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOps;

    private RankingService rankingService;

    private UUID user1;
    private UUID user2;
    private UUID user3;

    @BeforeEach
    void setUp() {
        rankingService = new RankingService(redisTemplate);
        user1 = UUID.randomUUID();
        user2 = UUID.randomUUID();
        user3 = UUID.randomUUID();
    }

    @Test
    void updateScore_addsToRedisSortedSet() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        rankingService.updateScore("strength", user1, 150.0);

        verify(zSetOps).add("ranking:strength", user1.toString(), 150.0);
    }

    @Test
    void incrementScore_incrementsInRedisSortedSet() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        rankingService.incrementScore("endurance", user1, 10.0);

        verify(zSetOps).incrementScore("ranking:endurance", user1.toString(), 10.0);
    }

    @Test
    void getRanking_returnsEntriesOrderedByScoreDescending() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        // Simulate Redis returning entries in descending score order
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(mockTuple(user1.toString(), 300.0));
        tuples.add(mockTuple(user2.toString(), 200.0));
        tuples.add(mockTuple(user3.toString(), 100.0));

        when(zSetOps.reverseRangeWithScores("ranking:strength", 0, 49)).thenReturn(tuples);

        RankingResponse response = rankingService.getRanking("strength", null);

        assertEquals("strength", response.getCategory());
        List<RankingEntry> entries = response.getEntries();
        assertEquals(3, entries.size());
        assertEquals(1, entries.get(0).getRank());
        assertEquals(user1, entries.get(0).getUserId());
        assertEquals(300.0, entries.get(0).getScore());
        assertEquals(2, entries.get(1).getRank());
        assertEquals(3, entries.get(2).getRank());
    }

    @Test
    void getRanking_withCustomTopN_limitsResults() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(mockTuple(user1.toString(), 300.0));

        when(zSetOps.reverseRangeWithScores("ranking:strength", 0, 0)).thenReturn(tuples);

        RankingResponse response = rankingService.getRanking("strength", 1L);

        assertEquals(1, response.getEntries().size());
    }

    @Test
    void getRanking_emptyCategory_returnsEmptyEntries() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.reverseRangeWithScores("ranking:nutrition", 0, 49)).thenReturn(null);

        RankingResponse response = rankingService.getRanking("nutrition", null);

        assertEquals("nutrition", response.getCategory());
        assertTrue(response.getEntries().isEmpty());
    }

    @Test
    void getValidCategories_returnsFourCategories() {
        Set<String> categories = rankingService.getValidCategories();

        assertEquals(4, categories.size());
        assertTrue(categories.contains("strength"));
        assertTrue(categories.contains("endurance"));
        assertTrue(categories.contains("consistency"));
        assertTrue(categories.contains("nutrition"));
    }

    // --- Helpers ---

    @SuppressWarnings("unchecked")
    private ZSetOperations.TypedTuple<String> mockTuple(String value, double score) {
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        when(tuple.getValue()).thenReturn(value);
        when(tuple.getScore()).thenReturn(score);
        return tuple;
    }
}
