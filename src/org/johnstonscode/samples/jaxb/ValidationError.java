package org.johnstonscode.samples.jaxb;

/**
 * @author simonjo
 *
 */
public interface ValidationError {

    public enum Severity {
        WARNING,
        ERROR,
        FATAL
    }
    
    Severity getSeverity();
    
    String getMessage();
    
    Location getLocation();
    
    Throwable getCause();
}
