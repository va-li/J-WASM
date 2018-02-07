package constants;

/**
 * Thrown when a value is out of the bounds defined in the WebAssembly specificaiton or by this Interpreter's specifics.
 */
public class SpecificationValueBoundaryViolationException extends RuntimeException {
    public SpecificationValueBoundaryViolationException() {
    }

    public SpecificationValueBoundaryViolationException(String message) {
        super(message);
    }

    public SpecificationValueBoundaryViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}
