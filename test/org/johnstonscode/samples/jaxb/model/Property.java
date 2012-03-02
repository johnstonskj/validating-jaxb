package org.johnstonscode.samples.jaxb.model;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author simonjo
 *
 */
public class Property {

    private String key;
    private String value;
    
    @XmlAttribute(required=true)
    public String getKey() {
        return this.key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    @XmlAttribute(required=true)
    public String getValue() {
        return this.value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
}
