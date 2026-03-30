package com.spartangoldengym.social.service;

import com.spartangoldengym.social.dto.ChallengeResponse;
import com.spartangoldengym.social.dto.CreateChallengeRequest;
import com.spartangoldengym.social.entity.Challenge;
import com.spartangoldengym.social.repository.ChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private final ChallengeRepository challengeRepository;

    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Transactional
    public ChallengeResponse createChallenge(CreateChallengeRequest request) {
        Challenge challenge = new Challenge();
        challenge.setName(request.getName());
        challenge.setDescription(request.getDescription());
        challenge.setType(request.getType());
        challenge.setCategory(request.getCategory());
        challenge.setMetricName(request.getMetricName());
        challenge.setTargetValue(request.getTargetValue());
        challenge.setBadgeName(request.getBadgeName());
        challenge.setCreatedBy(request.getCreatedBy());
        challenge.setStartsAt(request.getStartsAt());
        challenge.setEndsAt(request.getEndsAt());

        challenge = challengeRepository.save(challenge);
        log.info("Created challenge id={} name={} type={} category={}",
                challenge.getId(), challenge.getName(), challenge.getType(), challenge.getCategory());
        return toResponse(challenge);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> listChallenges(String type, String category) {
        List<Challenge> challenges;
        if (type != null && category != null) {
            challenges = challengeRepository.findByTypeAndCategory(type, category);
        } else if (type != null) {
            challenges = challengeRepository.findByType(type);
        } else if (category != null) {
            challenges = challengeRepository.findByCategory(category);
        } else {
            challenges = challengeRepository.findAll();
        }
        return challenges.stream().map(this::toResponse).collect(Collectors.toList());
    }

    ChallengeResponse toResponse(Challenge challenge) {
        ChallengeResponse r = new ChallengeResponse();
        r.setId(challenge.getId());
        r.setName(challenge.getName());
        r.setDescription(challenge.getDescription());
        r.setType(challenge.getType());
        r.setCategory(challenge.getCategory());
        r.setMetricName(challenge.getMetricName());
        r.setTargetValue(challenge.getTargetValue());
        r.setBadgeName(challenge.getBadgeName());
        r.setCreatedBy(challenge.getCreatedBy());
        r.setStartsAt(challenge.getStartsAt());
        r.setEndsAt(challenge.getEndsAt());
        r.setCreatedAt(challenge.getCreatedAt());
        return r;
    }
}
