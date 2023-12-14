package sprest.exception;

/**
 * Exception thrown when invalid ID or other key was referenced while processing request
 */
public class InvalidReferenceException extends RuntimeException {

	private static final long serialVersionUID = 6682809580889342919L;

	public InvalidReferenceException(String msg) {
		super(msg);
	}
}
