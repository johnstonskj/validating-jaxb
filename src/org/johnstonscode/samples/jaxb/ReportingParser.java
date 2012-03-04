package org.johnstonscode.samples.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.johnstonscode.samples.jaxb.ValidationError.Severity;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This is a wrapper around JAX-B but with all the error handling and 
 * location tracking we can posibly muster. It utilizes both JAX-B and
 * SAX simultaneously to hook in different handlers to manage validation
 * errors, capture line numbers and associate parsed objects with 
 * locations. 
 * 
 * Apparently JAX-B doesn't validate as much as you'd 
 * expect, for example just because you have @XmlAttribute(required=true) set 
 * on a field doesn't mean it will generate an error/warning for an XML element 
 * that doesn't specify the attribute. So, even for errors it should actually 
 * be able to detect (basically schema validation errors), it doesn't actually 
 * do what you would expect. However, even if it did we would want to do more 
 * detailed validation, checking cross-references and so on which we can only 
 * do after parsing and after parsing of course we haven't any info any more.
 * 
 * To deal with basic JAX-B validation then we implement our own 
 * <code>ValidationEventHandler</code> that simply records all errors into a
 * list that can be made available to the client. This is a fairly limited
 * set of errors under most implementations, and the implementations are
 * only required to do a best effort at error reporting and potentially
 * only report the first error and stop.
 * 
 * To be able to track which unmarshalled object is found where in the file
 * we use a little trickery as we need to be able to associate the element
 * location data, which is only available in SAX, with the unmarshalled
 * object which is only available in JAX-B. To do this we use a technique
 * available in JAX-B where we can retrieve from the parser the actual SAX
 * handler implemented by JAX-B and wrap it in our own, and feed to by
 * hand into a SAX processor. By doing this we still get the benefit of 
 * the JAX-B unmarshal code but we get to intercept the one call we care
 * about in the SAX <code>ContentHandler</code> interface, 
 * {@link ContentHandler#setDocumentLocator(Locator)}. The wrapper actually
 * has to implement the JAX-B version of <code>ContentHandler</code>,
 * {@link UnmarshallerHandler}, but it also implements another JAX-B interface
 * {@link Listener} which allows us to handle events before and after an
 * object is unmarshalled. So, specifically we use the
 * {@link Listener#beforeUnmarshal(Object, Object)} to associate the
 * current object being unmarshalled with the location data we can 
 * retrieve from the {@link Locator}.
 * 
 * <h3>Issues</h3>
 * 
 * In the  JavaDoc for the <code>handleEvent</code> method it states that the
 * method should return <code>true</code> or <code>false</code>:
 * 
 * <blockquote>
 * <code>true</code> if the JAXB Provider should attempt to continue the current 
 * unmarshal, validate, or marshal operation after handling this warning/error, 
 * <code>false</code> if the provider should terminate the current operation 
 * with the appropriate UnmarshalException, ValidationException, or MarshalException.
 * </blockquote>
 * 
 * @author simonjo
 * 
 * @param <T> the type of the expected root element
 *
 */
public class ReportingParser<T> {
    
    /*
     * We use this to gather errors to return to the client.
     */
    private List<ValidationError> events;
    /*
     * The actual root object to return to the client.
     */
    private T result;
    /*
     * The Map that we use to track the location of objects in the XML.
     */
    private Map<Object, LocationImpl> locationMap = new HashMap<Object, LocationImpl>();
    
    /**
     * Construct a new parser.
     */
    public ReportingParser() {
        // Currently, do nothing.
    }
    
    /**
     * Parse the given file and return the root object unmarshalled
     * by JAX-B. The method will return <code>null</code> on errors 
     * reading from the input or misconfigured SAX/JAX-B.
     * 
     * @param fileName the name of a file to parse
     * @param classOfT the class of the response type
     * 
     * @return the root object unmarshalled by JAX-B, or <code>null</code> 
     *     on error.
     * @throws ParserConfigurationException implies the JAXB configuration is
     *     not valid
     * @throws IOException implies that the parser could not read from the 
     *     input source 
     */
    public T parse(final String fileName, final Class<? super T> classOfT) throws ParserConfigurationException, IOException {
        return parse(new InputSource(fileName), null, classOfT);
    }
    
    /**
     * Parse the given input stream and return the root object unmarshalled
     * by JAX-B. The method will return <code>null</code> on errors 
     * reading from the input or misconfigured SAX/JAX-B.
     * 
     * @param input the input stream to parse
     * @param classOfT the class of the response type
     * 
     * @return the root object unmarshalled by JAX-B, or <code>null</code> 
     *     on error.
     * @throws ParserConfigurationException implies the JAXB configuration is
     *     not valid
     * @throws IOException implies that the parser could not read from the 
     *     input source 
     */
    public T parse(final InputStream input, final Class<? super T> classOfT) throws ParserConfigurationException, IOException {
        return parse(new InputSource(input), null, classOfT);
    }
    
    /**
     * Parse the given reader and return the root object unmarshalled
     * by JAX-B. The method will return <code>null</code> on errors 
     * reading from the input or misconfigured SAX/JAX-B.
     * 
     * @param input the reader stream to parse
     * @param classOfT the class of the response type
     * 
     * @return the root object unmarshalled by JAX-B, or <code>null</code> 
     *     on error.
     * @throws ParserConfigurationException implies the JAXB configuration is
     *     not valid
     * @throws IOException implies that the parser could not read from the 
     *     input source 
     */
    public T parse(final Reader input, final Class<? super T> classOfT) throws ParserConfigurationException, IOException {
        return parse(new InputSource(input), null, classOfT);
    }
    
    /**
     * Parse the given input source and return the root object unmarshalled
     * by JAX-B. The method will return <code>null</code> on errors 
     * reading from the input or misconfigured SAX/JAX-B.
     * 
     * @param input the input source to parse
     * @param schemaPath the path to an XML Schema definition to use for validation
     * @param classOfT the class of the response type
     * 
     * @return the root object unmarshalled by JAX-B, or <code>null</code> 
     *     on error.
     * @throws ParserConfigurationException implies the JAXB configuration is
     *     not valid
     * @throws IOException implies that the parser could not read from the 
     *     input source 
     */
    @SuppressWarnings("unchecked")
    public T parse(final InputSource input, final String schemaPath, final Class<? super T> classOfT) throws ParserConfigurationException, IOException {
        this.result = null;
        this.events = new LinkedList<ValidationError>();
        try {
            
            // Standard JAX-B
            final JAXBContext context = JAXBContext.newInstance(classOfT);
            final Unmarshaller unmarshaller = context.createUnmarshaller();
            // Setup schema validation if required
            if (schemaPath != null) {
                final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                final InputStream schemaIS = ClassLoader.getSystemResourceAsStream(schemaPath);
                final Schema schema = sf.newSchema(new StreamSource(schemaIS));
                unmarshaller.setSchema(schema);
            }
            // Now retrieve the SAX handler that JAX-B uses
            final UnmarshallerHandler unmarshallerHandler = unmarshaller.getUnmarshallerHandler();
            // Wrap it in our own handler
            final DelegatingHandlerImpl actualHandler = new DelegatingHandlerImpl(unmarshallerHandler, this.locationMap);
    
            // Now create and add an error handler
            final ValidationEventHandlerImpl errorHandler = new ValidationEventHandlerImpl(this.events);
            unmarshaller.setEventHandler(errorHandler);
            // Add a listener for before/after unmarshall events
            unmarshaller.setListener(actualHandler);
    
            // Now setup SAX
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            
            // Start the SAX parser but using *our* new handler
            final XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(actualHandler);
            xmlReader.parse(input);

            // Retrieve the result from the handler, note that this is actually
            // the bridge back to JAX-B
            this.result = (T)unmarshallerHandler.getResult();
            
        } catch (UnmarshalException ex) {
            // ignore, these are reported in the validation errors.
        } catch (JAXBException ex) {
            this.events.add(new ValidationErrorImpl(Severity.FATAL, "JAX-B configuration exception", ex));
        } catch (SAXException ex) {
            this.events.add(new ValidationErrorImpl(Severity.FATAL, "IO error reading from InputSource", ex));
        }
        
        return this.result;
    }
    
    /**
     * Return the result of the last parse operation.
     * 
     * @return the root object unmarshalled by JAX-B, or <code>null</code> 
     *     on error.
     */
    public T getResult() {
        return this.result;
    }
    
    /**
     * Return the list of errors recorded during parsing, this will always
     * return a list, but that list may be empty.
     * 
     * @return a list of validation errors
     */
    public List<ValidationError> getEvents() {
        return this.events;
    }
    
    /**
     * Return the map that allows the client to determine the location of
     * a parsed object in the XML; it records the location of the XML
     * start element which was the root of the XML that was unmarshalled
     * into the given object.
     * 
     * @return the map of parsed object to XML location
     */
    public Map<Object, LocationImpl> getLocationMap() {
        return this.locationMap;
    }
}