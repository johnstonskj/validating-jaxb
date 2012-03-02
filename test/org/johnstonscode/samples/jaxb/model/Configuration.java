package org.johnstonscode.samples.jaxb.model;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author simonjo
 *
 */
@XmlRootElement(name="Configuration", namespace="http://example.org/xmlns/configuration")
public class Configuration {

    private Collection<Property> properties;
    
    @XmlElement(name="property", namespace="http://example.org/xmlns/configuration")
    public Collection<Property> getProperties() {
        return this.properties;
    }

    public void setProperties(Collection<Property> properties) {
        this.properties = properties;
    }
    
}
