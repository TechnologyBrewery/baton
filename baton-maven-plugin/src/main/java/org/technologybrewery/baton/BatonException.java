package org.technologybrewery.baton;

/**
 * Baton-specific exception.
 */
public class BatonException extends RuntimeException {

    /**
     * {@inheritDoc}
     */
    public BatonException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public BatonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public BatonException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public BatonException(Throwable cause) {
        super(cause);
    }

}
