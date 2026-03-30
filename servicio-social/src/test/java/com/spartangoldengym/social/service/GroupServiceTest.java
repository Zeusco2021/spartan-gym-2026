package com.spartangoldengym.social.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.CreateGroupRequest;
import com.spartangoldengym.social.dto.GroupResponse;
import com.spartangoldengym.social.entity.TrainingGroup;
import com.spartangoldengym.social.repository.TrainingGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private TrainingGroupRepository groupRepository;
    @Mock
    private NeptuneRelationshipService neptuneService;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupService(groupRepository, neptuneService);
    }

    @Test
    void createGroup_savesGroupAndCreatesNeptuneRelationships() {
        UUID creatorId = UUID.randomUUID();
        UUID member1 = UUID.randomUUID();
        UUID member2 = UUID.randomUUID();

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Morning Warriors");
        request.setDescription("Early morning training group");
        request.setCreatedBy(creatorId);
        request.setMemberIds(Arrays.asList(member1, member2));

        when(groupRepository.save(any(TrainingGroup.class))).thenAnswer(inv -> {
            TrainingGroup g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            g.setCreatedAt(Instant.now());
            return g;
        });

        GroupResponse response = groupService.createGroup(request);

        assertNotNull(response.getId());
        assertEquals("Morning Warriors", response.getName());
        assertEquals("Early morning training group", response.getDescription());
        assertEquals(creatorId, response.getCreatedBy());
        assertEquals(3, response.getMemberIds().size());
        assertTrue(response.getMemberIds().contains(creatorId));
        assertTrue(response.getMemberIds().contains(member1));
        assertTrue(response.getMemberIds().contains(member2));

        // Verify Neptune calls
        verify(neptuneService).createGroup(any(UUID.class), eq("Morning Warriors"));
        verify(neptuneService, times(3)).addMembership(any(UUID.class), any(UUID.class));
    }

    @Test
    void createGroup_creatorAlwaysIncludedAsMember() {
        UUID creatorId = UUID.randomUUID();

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Solo Group");
        request.setCreatedBy(creatorId);
        request.setMemberIds(null);

        when(groupRepository.save(any(TrainingGroup.class))).thenAnswer(inv -> {
            TrainingGroup g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            g.setCreatedAt(Instant.now());
            return g;
        });

        GroupResponse response = groupService.createGroup(request);

        assertEquals(1, response.getMemberIds().size());
        assertTrue(response.getMemberIds().contains(creatorId));
    }

    @Test
    void createGroup_noDuplicateCreatorInMembers() {
        UUID creatorId = UUID.randomUUID();

        CreateGroupRequest request = new CreateGroupRequest();
        request.setName("Test Group");
        request.setCreatedBy(creatorId);
        request.setMemberIds(Arrays.asList(creatorId, UUID.randomUUID()));

        when(groupRepository.save(any(TrainingGroup.class))).thenAnswer(inv -> {
            TrainingGroup g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            g.setCreatedAt(Instant.now());
            return g;
        });

        GroupResponse response = groupService.createGroup(request);

        long creatorCount = response.getMemberIds().stream()
                .filter(id -> id.equals(creatorId)).count();
        assertEquals(1, creatorCount);
    }

    @Test
    void listGroups_withUserId_returnsUserGroups() {
        UUID userId = UUID.randomUUID();
        TrainingGroup group = makeGroup(userId);
        when(groupRepository.findByMemberId(userId)).thenReturn(Collections.singletonList(group));

        List<GroupResponse> result = groupService.listGroups(userId);

        assertEquals(1, result.size());
        assertEquals("Test Group", result.get(0).getName());
    }

    @Test
    void listGroups_withoutUserId_returnsAllGroups() {
        TrainingGroup group = makeGroup(UUID.randomUUID());
        when(groupRepository.findAll()).thenReturn(Collections.singletonList(group));

        List<GroupResponse> result = groupService.listGroups(null);

        assertEquals(1, result.size());
    }

    @Test
    void getGroup_notFound_throwsException() {
        UUID groupId = UUID.randomUUID();
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroup(groupId));
    }

    private TrainingGroup makeGroup(UUID creatorId) {
        TrainingGroup g = new TrainingGroup();
        g.setId(UUID.randomUUID());
        g.setName("Test Group");
        g.setDescription("A test group");
        g.setCreatedBy(creatorId);
        g.setMemberIds(Arrays.asList(creatorId, UUID.randomUUID()));
        g.setCreatedAt(Instant.now());
        return g;
    }
}
