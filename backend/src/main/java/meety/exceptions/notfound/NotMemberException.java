package meety.exceptions.notfound;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotMemberException extends RuntimeException {
    public NotMemberException(Long groupId, Long userId) {
        super("User " + userId + " is not a member of group " + groupId);
    }
}