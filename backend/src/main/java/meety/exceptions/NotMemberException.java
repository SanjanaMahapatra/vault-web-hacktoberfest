package meety.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotMemberException extends RuntimeException {
    public NotMemberException(Long groupId, Long userId) {
        super("User " + userId + " is not a member of group " + groupId);
    }
}