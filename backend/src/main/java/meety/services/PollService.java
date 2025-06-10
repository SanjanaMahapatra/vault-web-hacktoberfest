package meety.services;

import meety.dtos.PollRequestDto;
import meety.dtos.PollResponseDto;
import meety.exceptions.UnauthorizedException;
import meety.exceptions.notfound.GroupNotFoundException;
import meety.exceptions.notfound.NotMemberException;
import meety.exceptions.notfound.PollNotFoundException;
import meety.models.*;
import meety.repositories.GroupMemberRepository;
import meety.repositories.GroupRepository;
import meety.repositories.PollRepository;
import meety.repositories.PollVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    public Poll createPoll(Group group, User author, PollRequestDto pollDto) {
        if (groupMemberRepository.findByGroupAndUser(group, author).isEmpty()) {
            throw new NotMemberException(group.getId(), author.getId());
        }
        Poll poll = Poll.builder()
                .group(group)
                .author(author)
                .question(pollDto.getQuestion())
                .deadline(pollDto.getDeadline())
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

    public List<Poll> getPollsByGroup(Long groupId, User currentUser) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group with id " + groupId + " not found"));
        if (groupMemberRepository.findByGroupAndUser(group, currentUser).isEmpty()) {
            throw new NotMemberException(group.getId(), currentUser.getId());
        }

        return pollRepository.findByGroupId(groupId);
    }

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

    public Poll updatePoll(Long groupId, Long pollId, User user, PollRequestDto pollDto) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new PollNotFoundException(pollId));

        if (!poll.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Poll does not belong to group");
        }

        if (!poll.getAuthor().getId().equals(user.getId())) {
            throw new UnauthorizedException("Only the author can edit the poll");
        }

        poll.setQuestion(pollDto.getQuestion());
        poll.setDeadline(pollDto.getDeadline());
        poll.setAnonymous(pollDto.isAnonymous());

        poll.getOptions().clear();

        List<PollOption> newOptions = pollDto.getOptions().stream()
                .map(optionText -> PollOption.builder()
                        .poll(poll)
                        .text(optionText)
                        .build())
                .toList();

        poll.getOptions().addAll(newOptions);

        return pollRepository.save(poll);
    }


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
