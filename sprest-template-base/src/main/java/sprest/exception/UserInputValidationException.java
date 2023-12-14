package sprest.exception;

/**
 * Exception thrown when user input is invalid.
 *
 */
public class UserInputValidationException extends RuntimeException {

	private static final long serialVersionUID = 7142299769723428702L;

	public UserInputValidationException(String message) {
		super(message);
	}

    public UserInputValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
