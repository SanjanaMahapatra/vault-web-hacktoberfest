package vaultWeb.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vaultWeb.dtos.PollRequestDto;
import vaultWeb.dtos.PollResponseDto;
import vaultWeb.exceptions.UnauthorizedException;
import vaultWeb.exceptions.notfound.GroupNotFoundException;
import vaultWeb.exceptions.notfound.NotMemberException;
import vaultWeb.exceptions.notfound.PollNotFoundException;
import vaultWeb.models.*;
import vaultWeb.repositories.GroupMemberRepository;
import vaultWeb.repositories.GroupRepository;
import vaultWeb.repositories.PollRepository;
import vaultWeb.repositories.PollVoteRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Service class responsible for managing polls within groups.
 * <p>
 * Provides functionalities to create, update, delete, retrieve, and vote on polls.
 * It also converts Poll entities to PollResponseDto objects for API responses.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PollService {


    private final PollRepository pollRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PollVoteRepository pollVoteRepository;

    /**
     * Creates a new poll in the specified group by the given author.
     *
     * @param group   the group in which the poll will be created
     * @param author  the user who creates the poll
     * @param pollDto the data transfer object containing poll details
     * @return the created Poll entity
     * @throws NotMemberException if the author is not a member of the group
     */
    public Poll createPoll(Group group, User author, PollRequestDto pollDto) {
        if (groupMemberRepository.findByGroupAndUser(group, author).isEmpty()) {
            throw new NotMemberException(group.getId(), author.getId());
        }

        Instant deadlineInstant = pollDto.getDeadline() != null
                ? pollDto.getDeadline().toInstant()
                : null;

        Poll poll = Poll.builder()
                .group(group)
                .author(author)
                .question(pollDto.getQuestion())
                .deadline(deadlineInstant)
                .isAnonymous(pollDto.isAnonymous())
                .build();

        List<PollOption> options = pollDto.getOptions().stream()
                .map(optionText -> PollOption.builder()
                        .poll(poll)
                        .text(optionText)
                        .build())
                .collect(Collectors.toList());

        poll.setOptions(options);

        return pollRepository.save(poll);
    }

    /**
     * Retrieves all polls for a given group.
     *
     * @param groupId     the ID of the group
     * @param currentUser the current user requesting the polls
     * @return a list of polls in the group
     * @throws GroupNotFoundException if the group does not exist
     * @throws NotMemberException     if the user is not a member of the group
     */
    public List<Poll> getPollsByGroup(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with id " + groupId + " not found"));
        if (groupMemberRepository.findByGroupAndUser(group, currentUser).isEmpty()) {
            throw new NotMemberException(group.getId(), currentUser.getId());
        }

        return pollRepository.findByGroupId(groupId);
    }

    /**
     * Converts a Poll entity to a PollResponseDto suitable for API responses.
     *
     * @param poll the Poll entity to convert
     * @return a PollResponseDto representing the poll and its options
     */
    public PollResponseDto toResponseDto(Poll poll) {
        List<PollResponseDto.OptionResultDto> options = poll.getOptions().stream()
                .map(option -> {
                    List<String> voters = poll.isAnonymous()
                            ? List.of()
                            : option.getVotes() == null
                            ? List.of()
                            : option.getVotes().stream()
                            .map(vote -> vote.getUser().getUsername())
                            .collect(Collectors.toList());

                    int voteCount = option.getVotes() != null ? option.getVotes().size() : 0;

                    return new PollResponseDto.OptionResultDto(
                            option.getId(),
                            option.getText(),
                            voteCount,
                            voters
                    );
                })
                .collect(Collectors.toList());

        return new PollResponseDto(
                poll.getId(),
                poll.getQuestion(),
                poll.isAnonymous(),
                options
        );
    }

    /**
     * Allows a user to vote for a specific option in a poll within a group.
     *
     * @param groupId  the ID of the group containing the poll
     * @param pollId   the ID of the poll
     * @param optionId the ID of the option to vote for
     * @param user     the user casting the vote
     * @throws GroupNotFoundException if the group does not exist
     * @throws NotMemberException     if the user is not a member of the group
     * @throws PollNotFoundException  if the poll does not exist
     * @throws RuntimeException       if the user has already voted or if the option/poll is invalid
     */
    public void vote(Long groupId, Long pollId, Long optionId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with id " + groupId + " not found"));

        if (groupMemberRepository.findByGroupAndUser(group, user).isEmpty()) {
            throw new NotMemberException(group.getId(), user.getId());
        }

        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));

        if (!poll.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Poll does not belong to group");
        }

        PollOption option = poll.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("PollOption not found in poll"));

        if (pollVoteRepository.existsByOption_PollAndUser(poll, user)) {
            throw new RuntimeException("User has already voted in this poll");
        }

        PollVote vote = PollVote.builder()
                .option(option)
                .user(user)
                .build();

        if (option.getVotes() == null) {
            option.setVotes(new ArrayList<>());
        }
        option.getVotes().add(vote);

        pollVoteRepository.save(vote);
    }

    /**
     * Updates an existing poll authored by a user.
     *
     * @param groupId the ID of the group containing the poll
     * @param pollId  the ID of the poll to update
     * @param user    the author of the poll
     * @param pollDto the new poll data
     * @return the updated Poll entity
     * @throws PollNotFoundException if the poll does not exist
     * @throws UnauthorizedException if the user is not the author
     */
    public Poll updatePoll(Long groupId, Long pollId, User user, PollRequestDto pollDto) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));

        if (!poll.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Poll does not belong to group");
        }

        if (!poll.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the author can edit the poll");
        }

        Instant deadlineInstant = pollDto.getDeadline() != null
                ? pollDto.getDeadline().toInstant()
                : null;

        poll.setQuestion(pollDto.getQuestion());
        poll.setDeadline(deadlineInstant);
        poll.setAnonymous(pollDto.isAnonymous());

        poll.getOptions().clear();

        List<PollOption> newOptions = pollDto.getOptions().stream()
                .map(optionText -> PollOption.builder()
                        .poll(poll)
                        .text(optionText)
                        .build())
                .collect(Collectors.toList());

        poll.getOptions().addAll(newOptions);

        return pollRepository.save(poll);
    }

    /**
     * Deletes a poll authored by a user.
     *
     * @param groupId the ID of the group containing the poll
     * @param pollId  the ID of the poll to delete
     * @param user    the author of the poll
     * @throws PollNotFoundException if the poll does not exist
     * @throws UnauthorizedException if the user is not the author
     */
    public void deletePoll(Long groupId, Long pollId, User user) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));

        if (!poll.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Poll does not belong to group");
        }

        if (!poll.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the author can delete the poll");
        }

        pollRepository.delete(poll);
    }
}