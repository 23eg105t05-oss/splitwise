package com.splitwise.service.impl;

import com.splitwise.dto.request.GroupRequest;
import com.splitwise.exception.BadRequestException;
import com.splitwise.exception.ResourceNotFoundException;
import com.splitwise.exception.UnauthorizedException;
import com.splitwise.model.Group;
import com.splitwise.model.GroupMember;
import com.splitwise.model.User;
import com.splitwise.repository.GroupMemberRepository;
import com.splitwise.repository.GroupRepository;
import com.splitwise.repository.UserRepository;
import com.splitwise.service.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository,
                            GroupMemberRepository groupMemberRepository,
                            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Group createGroup(GroupRequest request, String creatorEmail) {
        User creator = getUser(creatorEmail);

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCurrency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "INR");
        group.setCreatedBy(creator);
        group = groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(creator);
        member.setRole(GroupMember.Role.ADMIN);
        groupMemberRepository.save(member);

        return group;
    }

    @Override
    public List<Group> getMyGroups(String email) {
        User user = getUser(email);
        return groupRepository.findActiveGroupsByUserId(user.getId());
    }

    @Override
    public Group getGroupById(Long groupId, String requestorEmail) {
        Group group = findActiveGroup(groupId);
        User user = getUser(requestorEmail);
        assertMember(groupId, user.getId());
        return group;
    }

    @Override
    @Transactional
    public Group addMember(Long groupId, String memberEmail, String adminEmail) {
        Group group = findActiveGroup(groupId);
        User admin = getUser(adminEmail);
        assertAdmin(groupId, admin.getId());

        User newMember = userRepository.findByEmail(memberEmail)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + memberEmail));

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newMember.getId())) {
            throw new BadRequestException("User is already a member of this group.");
        }

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(newMember);
        member.setRole(GroupMember.Role.MEMBER);
        groupMemberRepository.save(member);

        return group;
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long userId, String adminEmail) {
        findActiveGroup(groupId);
        User admin = getUser(adminEmail);
        assertAdmin(groupId, admin.getId());

        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Member not found in group."));
        groupMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId, String adminEmail) {
        Group group = findActiveGroup(groupId);
        User admin = getUser(adminEmail);
        assertAdmin(groupId, admin.getId());
        group.setActive(false);
        groupRepository.save(group);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private Group findActiveGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        if (!group.isActive()) throw new ResourceNotFoundException("Group not found with id: " + groupId);
        return group;
    }

    private void assertMember(Long groupId, Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new UnauthorizedException("Access denied. You are not a member of this group.");
        }
    }

    private void assertAdmin(Long groupId, Long userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .orElseThrow(() -> new UnauthorizedException("Access denied."));
        if (member.getRole() != GroupMember.Role.ADMIN) {
            throw new UnauthorizedException("Access denied. Admins only.");
        }
    }
}
