package meety.exceptions.notfound;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PollNotFoundException extends RuntimeException {
    public PollNotFoundException(Long pollId) {
        super("Poll with ID" + pollId + " not found");
    }
}
