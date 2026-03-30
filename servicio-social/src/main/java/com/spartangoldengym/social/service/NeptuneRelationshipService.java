package com.spartangoldengym.social.service;

import com.spartangoldengym.common.config.NeptuneSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing social relationships in Amazon Neptune graph database.
 * Uses Neptune's HTTP REST API with Gremlin queries.
 */
@Service
public class NeptuneRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(NeptuneRelationshipService.class);

    private final String neptuneEndpoint;
    private final RestTemplate restTemplate;

    public NeptuneRelationshipService(
            @Value("${aws.neptune.endpoint:localhost}") String neptuneHost,
            @Value("${aws.neptune.port:8182}") int neptunePort) {
        this.neptuneEndpoint = "http://" + neptuneHost + ":" + neptunePort + "/gremlin";
        this.restTemplate = new RestTemplate();
    }

    // Visible for testing
    NeptuneRelationshipService(String neptuneEndpoint, RestTemplate restTemplate) {
        this.neptuneEndpoint = neptuneEndpoint;
        this.restTemplate = restTemplate;
    }

    public void addFollow(UUID followerId, UUID followedId) {
        String gremlin = String.format(
                "g.V().has('%s','%s','%s').as('a')"
                + ".V().has('%s','%s','%s').as('b')"
                + ".addE('%s').from('a').to('b')",
                NeptuneSchema.VERTEX_USER, NeptuneSchema.PROP_ID, followerId,
                NeptuneSchema.VERTEX_USER, NeptuneSchema.PROP_ID, followedId,
                NeptuneSchema.EDGE_FOLLOWS);
        executeGremlin(gremlin);
        log.info("Neptune: {} -[FOLLOWS]-> {}", followerId, followedId);
    }

    public void addFriendship(UUID userId1, UUID userId2) {
        String gremlin = String.format(
                "g.V().has('%s','%s','%s').as('a')"
                + ".V().has('%s','%s','%s').as('b')"
                + ".addE('%s').from('a').to('b')",
                NeptuneSchema.VERTEX_USER, NeptuneSchema.PROP_ID, userId1,
                NeptuneSchema.VERTEX_USER, NeptuneSchema.PROP_ID, userId2,
                NeptuneSchema.EDGE_FRIEND_OF);
        executeGremlin(gremlin);
        log.info("Neptune: {} -[FRIEND_OF]-> {}", userId1, userId2);
    }

    public void createGroup(UUID groupId, String groupName) {
        String gremlin = String.format(
                "g.addV('%s').property('%s','%s').property('%s','%s')",
                NeptuneSchema.VERTEX_GROUP, NeptuneSchema.PROP_ID, groupId,
                NeptuneSchema.PROP_NAME, groupName);
        executeGremlin(gremlin);
        log.info("Neptune: Created group vertex id={} name={}", groupId, groupName);
    }

    public void addMembership(UUID userId, UUID groupId) {
        String gremlin = String.format(
                "g.V().has('%s','%s','%s').as('u')"
                + ".V().has('%s','%s','%s').as('g')"
                + ".addE('%s').from('u').to('g')",
                NeptuneSchema.VERTEX_USER, NeptuneSchema.PROP_ID, userId,
                NeptuneSchema.VERTEX_GROUP, NeptuneSchema.PROP_ID, groupId,
                NeptuneSchema.EDGE_MEMBER_OF);
        executeGremlin(gremlin);
        log.info("Neptune: {} -[MEMBER_OF]-> {}", userId, groupId);
    }

    void executeGremlin(String gremlinQuery) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("gremlin", gremlinQuery);
            restTemplate.postForEntity(neptuneEndpoint, body, String.class);
        } catch (Exception e) {
            log.warn("Neptune query failed (non-blocking): {}", e.getMessage());
        }
    }
}
