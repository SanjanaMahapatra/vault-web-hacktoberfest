package meety.controllers;

import jakarta.validation.Valid;
import meety.dtos.PollRequestDto;
import meety.dtos.PollResponseDto;
import meety.exceptions.notfound.GroupNotFoundException;
import meety.models.Group;
import meety.models.Poll;
import meety.models.User;
import meety.services.GroupService;
import meety.services.PollService;
import meety.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId}/polls")
public class PollController {
    @Autowired
    private GroupService groupService;

    @Autowired
    private PollService pollService;

    @Autowired
    private AuthService authService;

    @PostMapping("")
    public ResponseEntity<PollResponseDto> createPoll(
            @PathVariable Long groupId,
            @RequestBody @Valid PollRequestDto pollDto) {
        User currentUser = authService.getCurrentUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with id " + groupId + " not found"));
        Poll poll = pollService.createPoll(group, currentUser, pollDto);

        PollResponseDto responseDto = pollService.toResponseDto(poll);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("")
    public ResponseEntity<List<PollResponseDto>> getPolls(@PathVariable Long groupId) {
        User currentUser = authService.getCurrentUser();
        List<PollResponseDto> polls = pollService.getPollsByGroup(groupId, currentUser)
                .stream()
                .map(pollService::toResponseDto)
                .toList();
        return ResponseEntity.ok(polls);
    }

    @PostMapping("/{pollId}/options/{optionId}/vote")
    public ResponseEntity<Void> vote(
            @PathVariable Long groupId,
            @PathVariable Long pollId,
            @PathVariable Long optionId) {
        User currentUser = authService.getCurrentUser();
        pollService.vote(groupId, pollId, optionId, currentUser);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{pollId}")
    public ResponseEntity<PollResponseDto> updatePoll(
            @PathVariable Long groupId,
            @PathVariable Long pollId,
            @RequestBody @Valid PollRequestDto pollDto) {
        User currentUser = authService.getCurrentUser();
        Poll updatedPoll = pollService.updatePoll(groupId, pollId, currentUser, pollDto);
        return ResponseEntity.ok(pollService.toResponseDto(updatedPoll));
    }

    @DeleteMapping("/{pollId}")
    public ResponseEntity<Void> deletePoll(
            @PathVariable Long groupId,
            @PathVariable Long pollId) {
        User currentUser = authService.getCurrentUser();
        pollService.deletePoll(groupId, pollId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
