package sprest.exception;

/**
 * Exception thrown when sending email fails.
 */
public class EmailException extends RuntimeException {

    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
