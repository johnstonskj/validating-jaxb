package org.johnstonscode.samples.jaxb;

import javax.xml.bind.ValidationEvent;

/**
 * @author simonjo
 *
 */
public class ValidationErrorImpl implements ValidationError {
    
    private final Severity severity;
    private final String message;
    private final Location location;
    private final Throwable cause;

    public ValidationErrorImpl(final Severity severity, final String message, final Throwable cause) {
        this.severity = severity;
        this.message = message;
        this.location = new LocationImpl(Location.UNKNOWN, Location.UNKNOWN);
        this.cause = cause;
    }

    public ValidationErrorImpl(final ValidationEvent event) {
        switch (event.getSeverity()) {
        case ValidationEvent.WARNING:
            this.severity = Severity.WARNING; break;
        case ValidationEvent.ERROR:
            this.severity = Severity.ERROR; break;
        default:
            this.severity = Severity.FATAL;
        }
        this.message = event.getMessage();
        this.cause = event.getLinkedException();
        if (event.getLocator() == null) {
            this.location = new LocationImpl(Location.UNKNOWN, Location.UNKNOWN);
        } else {
            this.location = new LocationImpl(event.getLocator().getLineNumber(), event.getLocator().getColumnNumber());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getLocation() {
        return this.location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

}
