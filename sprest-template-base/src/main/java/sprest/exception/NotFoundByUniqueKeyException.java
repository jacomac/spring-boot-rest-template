package sprest.exception;

/**
 * Exception thrown when entity was not found by a unique key
 */
public class NotFoundByUniqueKeyException extends RuntimeException {

    public NotFoundByUniqueKeyException(String message) {
        super(message);
    }
}
