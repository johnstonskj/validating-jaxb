package org.johnstonscode.samples.jaxb;

/**
 * This is the basic {@link Location} implementation.
 *
 */
class LocationImpl implements Location {
    
    private final int line;
    private final int column;
    
    LocationImpl(final int line, final int column) {
        this.line = line;
        this.column = column;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        return this.line;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber() {
        return this.column;
    }
}