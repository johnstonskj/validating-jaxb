package org.johnstonscode.samples.jaxb;

/**
 * This class represents an error that occurred during validation, where that
 * validation may take place during deserialization or during post-construction
 * consistency checking.
 * 
 * @author simonjo
 *
 */
public interface ValidationError {

    /**
     * The severity of this particular error.
     *
     */
    public enum Severity {
        WARNING,
        ERROR,
        FATAL
    }

    /**
     * Return the severity of this particular error.
     * 
     * @return the severity of this particular error
     */
    Severity getSeverity();
    
    /**
     * Return any message associated with this particular error.
     * 
     * @return any message associated with this particular error
     */
    String getMessage();
    
    /**
     * Return the location of this particular error.
     * 
     * @return the location of this particular error
     */
    Location getLocation();
    
    /**
     * Return the cause, if any, of this particular error.
     * 
     * @return the cause, or <code>null</code>, of this particular error
     */
    Throwable getCause();
}
