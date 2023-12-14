package sprest.exception;

public class DuplicateUserException extends RuntimeException {

	public DuplicateUserException(String message) {
		super(message);
	}

	private static final long serialVersionUID = 1L;

}
