package meety.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyMemberException extends RuntimeException {
    public AlreadyMemberException(Long groupId, Long userId) {
        super("User " + userId + " is already a member of group " + groupId);
    }
}