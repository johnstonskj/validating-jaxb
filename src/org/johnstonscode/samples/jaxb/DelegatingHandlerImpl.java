package org.johnstonscode.samples.jaxb;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.Unmarshaller.Listener;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class implements the JAX-B {@link UnmarshallerHandler} interface
 * (a specialization of the SAX {@link ContentHandler} interface) as well
 * as the {@link Listener} interface. Together the event handlers
 * {@link UnmarshallerHandler#setDocumentLocator(Locator)} and
 * {@link Listener#beforeUnmarshal(Object, Object)} allow us to build a
 * map from Object (the unmarshalled object) to a location object that
 * tracks the line number and column number where the XML element 
 * started.
 *
 */
class DelegatingHandlerImpl extends Listener implements UnmarshallerHandler {
    /*
     * The original handler, we delegate to it in all cases.
     */
    private final UnmarshallerHandler unmarshallerHandler;
    /*
     * The locator set by SAX. 
     */
    private Locator locator;
    /*
     * The map we build and return to clients.
     */
    private final Map<Object, LocationImpl> locationMap;
    
    public DelegatingHandlerImpl(final UnmarshallerHandler unmarshallerHandler, final Map<Object, LocationImpl> locationMap) {
        this.unmarshallerHandler = unmarshallerHandler;
        this.locationMap = locationMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeUnmarshal(Object target, Object parent) {
        super.beforeUnmarshal(target, parent);
        /*
         * Add this object to the location map using the 
         * locator set by SAX.
         */
        if (target != null && this.locator != null) {
            this.locationMap.put(target, new LocationImpl(this.locator.getLineNumber(), this.locator.getColumnNumber()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        /*
         * Remember this locator.
         */
        this.locator = locator;
        this.unmarshallerHandler.setDocumentLocator(locator);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.unmarshallerHandler.characters(ch, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endDocument() throws SAXException {
        this.unmarshallerHandler.endDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        this.unmarshallerHandler.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        this.unmarshallerHandler.endPrefixMapping(prefix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        this.unmarshallerHandler.ignorableWhitespace(ch, start, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        this.unmarshallerHandler.processingInstruction(target, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        this.unmarshallerHandler.skippedEntity(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startDocument() throws SAXException {
        this.unmarshallerHandler.startDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        this.unmarshallerHandler.startElement(uri, localName, qName, atts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.unmarshallerHandler.startPrefixMapping(prefix, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult() throws JAXBException, IllegalStateException {
        return this.unmarshallerHandler.getResult();
    }
}