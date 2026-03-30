package com.spartangoldengym.social.controller;

import com.spartangoldengym.social.dto.*;
import com.spartangoldengym.social.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final ChallengeService challengeService;
    private final AchievementService achievementService;
    private final RankingService rankingService;
    private final ShareService shareService;
    private final GroupService groupService;
    private final InteractionService interactionService;

    public SocialController(ChallengeService challengeService,
                            AchievementService achievementService,
                            RankingService rankingService,
                            ShareService shareService,
                            GroupService groupService,
                            InteractionService interactionService) {
        this.challengeService = challengeService;
        this.achievementService = achievementService;
        this.rankingService = rankingService;
        this.shareService = shareService;
        this.groupService = groupService;
        this.interactionService = interactionService;
    }

    // --- Challenges ---

    @PostMapping("/challenges")
    public ResponseEntity<ChallengeResponse> createChallenge(
            @Valid @RequestBody CreateChallengeRequest request) {
        ChallengeResponse response = challengeService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/challenges")
    public ResponseEntity<List<ChallengeResponse>> listChallenges(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category) {
        List<ChallengeResponse> challenges = challengeService.listChallenges(type, category);
        return ResponseEntity.ok(challenges);
    }

    // --- Achievements ---

    @GetMapping("/achievements")
    public ResponseEntity<List<AchievementResponse>> getUserAchievements(
            @RequestParam UUID userId) {
        List<AchievementResponse> achievements = achievementService.getUserAchievements(userId);
        return ResponseEntity.ok(achievements);
    }

    @PostMapping("/achievements/complete")
    public ResponseEntity<AchievementResponse> completeChallenge(
            @Valid @RequestBody CompleteChallengeRequest request) {
        AchievementResponse response = achievementService.completeChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- Rankings ---

    @GetMapping("/rankings")
    public ResponseEntity<RankingResponse> getRanking(
            @RequestParam String category,
            @RequestParam(required = false) Long topN) {
        RankingResponse response = rankingService.getRanking(category, topN);
        return ResponseEntity.ok(response);
    }

    // --- Share ---

    @PostMapping("/share")
    public ResponseEntity<ShareResponse> shareAchievement(
            @Valid @RequestBody ShareRequest request) {
        ShareResponse response = shareService.generateShareCard(request);
        return ResponseEntity.ok(response);
    }

    // --- Groups ---

    @PostMapping("/groups")
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupResponse>> listGroups(
            @RequestParam(required = false) UUID userId) {
        List<GroupResponse> groups = groupService.listGroups(userId);
        return ResponseEntity.ok(groups);
    }

    // --- Interactions ---

    @PostMapping("/interactions")
    public ResponseEntity<InteractionResponse> createInteraction(
            @Valid @RequestBody CreateInteractionRequest request) {
        InteractionResponse response = interactionService.createInteraction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
