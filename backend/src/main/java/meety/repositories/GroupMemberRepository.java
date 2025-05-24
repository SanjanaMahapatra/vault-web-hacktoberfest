package meety.repositories;

import meety.models.Group;
import meety.models.GroupMember;
import meety.models.User;
import meety.models.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    List<GroupMember> findAllByGroup(Group group);

    void deleteByGroupAndUser(Group group, User userId);

    long countByGroupAndRole(Group group, Role role);
}