package com.spartangoldengym.social.service;

import com.spartangoldengym.common.exception.ResourceNotFoundException;
import com.spartangoldengym.social.dto.CreateGroupRequest;
import com.spartangoldengym.social.dto.GroupResponse;
import com.spartangoldengym.social.entity.TrainingGroup;
import com.spartangoldengym.social.repository.TrainingGroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);

    private final TrainingGroupRepository groupRepository;
    private final NeptuneRelationshipService neptuneService;

    public GroupService(TrainingGroupRepository groupRepository,
                        NeptuneRelationshipService neptuneService) {
        this.groupRepository = groupRepository;
        this.neptuneService = neptuneService;
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        TrainingGroup group = new TrainingGroup();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(request.getCreatedBy());

        List<UUID> members = new ArrayList<>();
        members.add(request.getCreatedBy());
        if (request.getMemberIds() != null) {
            for (UUID memberId : request.getMemberIds()) {
                if (!members.contains(memberId)) {
                    members.add(memberId);
                }
            }
        }
        group.setMemberIds(members);

        group = groupRepository.save(group);

        // Store group and membership edges in Neptune
        neptuneService.createGroup(group.getId(), group.getName());
        for (UUID memberId : members) {
            neptuneService.addMembership(memberId, group.getId());
        }

        log.info("Created training group id={} name={} members={}",
                group.getId(), group.getName(), members.size());
        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listGroups(UUID userId) {
        List<TrainingGroup> groups;
        if (userId != null) {
            groups = groupRepository.findByMemberId(userId);
        } else {
            groups = groupRepository.findAll();
        }
        return groups.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId) {
        TrainingGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingGroup", "id=" + groupId));
        return toResponse(group);
    }

    GroupResponse toResponse(TrainingGroup group) {
        GroupResponse r = new GroupResponse();
        r.setId(group.getId());
        r.setName(group.getName());
        r.setDescription(group.getDescription());
        r.setCreatedBy(group.getCreatedBy());
        r.setMemberIds(group.getMemberIds());
        r.setCreatedAt(group.getCreatedAt());
        return r;
    }
}
