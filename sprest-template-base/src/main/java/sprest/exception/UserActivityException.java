package sprest.exception;

/**
 * Exception class thrown when invalid UserActivityType is provided on activity tracking request
 */
public class UserActivityException extends RuntimeException {

    public UserActivityException(String type, String group) {
        super(String.format("Invalid activity type %s for %s related activity.", type, group));
    }

    public UserActivityException(String message) {
        super(message);
    }

    public UserActivityException(String message, Throwable cause) {
        super(message, cause);
    }
}
