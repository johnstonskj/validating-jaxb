package org.johnstonscode.samples.jaxb;

/**
 * This is the simple interface that returns to the client the location of
 * a parsed object from the XML.
 * 
 * @author simonjo
 *
 */
public interface Location {

    public static final int UNKNOWN = -1;
    
    /**
     * Returns the line number (1-based) where the object exists in the 
     * XML; specifically it is the location of the start of the element.
     * 
     * @return the XML start element location
     */
    public int getLineNumber();
    
    /**
     * Returns the column number (1-based) where the object exists in the 
     * XML; specifically it is the location of the start of the element.
     * 
     * @return the XML start element location
     */
    public int getColumnNumber();

}