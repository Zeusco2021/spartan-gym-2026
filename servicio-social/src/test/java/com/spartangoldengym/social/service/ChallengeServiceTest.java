package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.ChallengeResponse;
import com.spartangoldengym.social.dto.CreateChallengeRequest;
import com.spartangoldengym.social.entity.Challenge;
import com.spartangoldengym.social.repository.ChallengeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeService challengeService;

    private UUID userId;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        now = Instant.now();
    }

    @Test
    void createChallenge_persistsAllFields() {
        CreateChallengeRequest request = makeRequest("weekly", "strength");

        ArgumentCaptor<Challenge> captor = ArgumentCaptor.forClass(Challenge.class);
        when(challengeRepository.save(captor.capture())).thenAnswer(inv -> {
            Challenge c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            c.setCreatedAt(Instant.now());
            return c;
        });

        ChallengeResponse response = challengeService.createChallenge(request);

        assertNotNull(response.getId());
        assertEquals("Bench Press Challenge", response.getName());
        assertEquals("weekly", response.getType());
        assertEquals("strength", response.getCategory());
        assertEquals("weight_lifted_kg", response.getMetricName());
        assertEquals(500.0, response.getTargetValue());
        assertEquals(userId, response.getCreatedBy());
    }

    @Test
    void listChallenges_noFilters_returnsAll() {
        List<Challenge> challenges = Arrays.asList(
                makeChallenge("weekly", "strength"),
                makeChallenge("monthly", "endurance"));
        when(challengeRepository.findAll()).thenReturn(challenges);

        List<ChallengeResponse> result = challengeService.listChallenges(null, null);

        assertEquals(2, result.size());
        verify(challengeRepository).findAll();
    }

    @Test
    void listChallenges_filterByType_returnsFiltered() {
        List<Challenge> challenges = Collections.singletonList(makeChallenge("weekly", "strength"));
        when(challengeRepository.findByType("weekly")).thenReturn(challenges);

        List<ChallengeResponse> result = challengeService.listChallenges("weekly", null);

        assertEquals(1, result.size());
        assertEquals("weekly", result.get(0).getType());
    }

    @Test
    void listChallenges_filterByCategory_returnsFiltered() {
        List<Challenge> challenges = Collections.singletonList(makeChallenge("monthly", "endurance"));
        when(challengeRepository.findByCategory("endurance")).thenReturn(challenges);

        List<ChallengeResponse> result = challengeService.listChallenges(null, "endurance");

        assertEquals(1, result.size());
        assertEquals("endurance", result.get(0).getCategory());
    }

    @Test
    void listChallenges_filterByTypeAndCategory_returnsFiltered() {
        List<Challenge> challenges = Collections.singletonList(makeChallenge("weekly", "strength"));
        when(challengeRepository.findByTypeAndCategory("weekly", "strength")).thenReturn(challenges);

        List<ChallengeResponse> result = challengeService.listChallenges("weekly", "strength");

        assertEquals(1, result.size());
        verify(challengeRepository).findByTypeAndCategory("weekly", "strength");
    }

    // --- Helpers ---

    private CreateChallengeRequest makeRequest(String type, String category) {
        CreateChallengeRequest req = new CreateChallengeRequest();
        req.setName("Bench Press Challenge");
        req.setDescription("Lift 500kg total in bench press");
        req.setType(type);
        req.setCategory(category);
        req.setMetricName("weight_lifted_kg");
        req.setTargetValue(500.0);
        req.setBadgeName("Iron Chest");
        req.setCreatedBy(userId);
        req.setStartsAt(now);
        req.setEndsAt(now.plus(7, ChronoUnit.DAYS));
        return req;
    }

    private Challenge makeChallenge(String type, String category) {
        Challenge c = new Challenge();
        c.setId(UUID.randomUUID());
        c.setName("Test Challenge");
        c.setType(type);
        c.setCategory(category);
        c.setMetricName("workouts_completed");
        c.setTargetValue(10.0);
        c.setCreatedBy(userId);
        c.setStartsAt(now);
        c.setEndsAt(now.plus(7, ChronoUnit.DAYS));
        c.setCreatedAt(now);
        return c;
    }
}
