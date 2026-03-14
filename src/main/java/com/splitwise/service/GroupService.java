package com.splitwise.service;

import com.splitwise.dto.request.GroupRequest;
import com.splitwise.model.Group;

import java.util.List;

public interface GroupService {
    Group createGroup(GroupRequest request, String creatorEmail);
    List<Group> getMyGroups(String email);
    Group getGroupById(Long groupId, String requestorEmail);
    Group addMember(Long groupId, String memberEmail, String adminEmail);
    void removeMember(Long groupId, Long userId, String adminEmail);
    void deleteGroup(Long groupId, String adminEmail);
}
