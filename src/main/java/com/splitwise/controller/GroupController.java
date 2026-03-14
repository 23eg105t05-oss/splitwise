package com.splitwise.controller;

import com.splitwise.dto.request.GroupRequest;
import com.splitwise.dto.response.ApiResponse;
import com.splitwise.model.Group;
import com.splitwise.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // POST /api/groups
    @PostMapping
    public ResponseEntity<ApiResponse<Group>> createGroup(
            @Valid @RequestBody GroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Group group = groupService.createGroup(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Group created successfully.", group));
    }

    // GET /api/groups
    @GetMapping
    public ResponseEntity<ApiResponse<List<Group>>> getMyGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Group> groups = groupService.getMyGroups(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(groups));
    }

    // GET /api/groups/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Group>> getGroupById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Group group = groupService.getGroupById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(group));
    }

    // POST /api/groups/{id}/members
    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Group>> addMember(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        Group group = groupService.addMember(id, body.get("email"), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Member added successfully.", group));
    }

    // DELETE /api/groups/{id}/members/{userId}
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupService.removeMember(id, userId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Member removed successfully.", null));
    }

    // DELETE /api/groups/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupService.deleteGroup(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Group deleted successfully.", null));
    }
}
