package meety.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AdminAccessDeniedException extends RuntimeException {
    public AdminAccessDeniedException(Long groupId, Long userId) {
        super("User " + userId + " has no admin privileges for group " + groupId);
    }

    public AdminAccessDeniedException(String message) {
        super(message);
    }
}