package meety.repositories;

import meety.models.Poll;
import meety.models.PollVote;
import meety.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByOption_PollAndUser(Poll poll, User user);
}
