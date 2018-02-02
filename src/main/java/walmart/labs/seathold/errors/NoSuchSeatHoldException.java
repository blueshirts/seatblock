package walmart.labs.seathold.errors;

/**
 * Thrown to indicate that a seat hold is not found.
 */
public class NoSuchSeatHoldException extends RuntimeException {
    /**
     * Constructs an NoSuchSeatHoldException with no detail message.
     */
    public NoSuchSeatHoldException() {
        super();
    }

    /**
     * Constructs a NoSuchSeatHoldException with the specified detail message.
     * @param s - the detail message.
     */
    public NoSuchSeatHoldException(String s) {
        super(s);
    }
}
