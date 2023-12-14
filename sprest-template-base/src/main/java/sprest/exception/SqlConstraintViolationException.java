package sprest.exception;

import lombok.Getter;

/**
 * Exception thrown when SQL constraint has been validated
 */
@Getter
public class SqlConstraintViolationException extends RuntimeException {

    public SqlConstraintViolationException(String message) {
        super(message);
    }
}
