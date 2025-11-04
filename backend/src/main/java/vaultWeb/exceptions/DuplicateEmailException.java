package vaultWeb.exceptions;

public class DuplicateEmailException extends RuntimeException {

    /**
     * Constructs a new DuplicateEmailException with a custom message.
     *
     * @param message the detail message explaining the exception
     */

    public DuplicateEmailException(String message) {
        super(message);
    }
}
