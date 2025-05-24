package meety.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import meety.dtos.GroupDto;
import meety.models.Group;
import meety.models.User;
import meety.security.annotations.AdminOnly;
import meety.services.GroupService;
import meety.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Group Controller", description = "manages the different Groups a User can join")
public class GroupController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private AuthService authService;

    @GetMapping("")
    public ResponseEntity<List<Group>> getGroups() {
        List<Group> publicGroups = groupService.getPublicGroups();
        return ResponseEntity.ok(publicGroups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<User>> getGroupMembers(@PathVariable Long id) {
        List<User> members = groupService.getMembers(id);
        return ResponseEntity.ok(members);
    }

    @PostMapping("")
    public ResponseEntity<Group> createGroup(@RequestBody GroupDto groupDto) {
        User currentUser = authService.getCurrentUser();
        Group created = groupService.createGroup(groupDto, currentUser);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Group> joinGroup(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        Group updatedGroup = groupService.joinGroup(id, currentUser);
        return ResponseEntity.ok(updatedGroup);
    }

    @AdminOnly
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Long id, @RequestBody GroupDto updatedGroup) {
        return ResponseEntity.ok(groupService.updateGroup(id, updatedGroup));
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Group> leaveGroup(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        Group updatedGroup = groupService.leaveGroup(id, currentUser);
        return ResponseEntity.ok(updatedGroup);
    }

    @AdminOnly
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> removeMemberFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        Group group = groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(group);
    }

}
