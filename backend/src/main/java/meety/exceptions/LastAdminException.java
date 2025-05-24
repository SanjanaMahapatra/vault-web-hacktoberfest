package meety.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LastAdminException extends RuntimeException {
    public LastAdminException(Long groupId) {
        super("Cannot remove the last admin of group " + groupId);
    }
}