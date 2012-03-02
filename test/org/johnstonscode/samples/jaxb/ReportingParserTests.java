package org.johnstonscode.samples.jaxb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Assert;

import org.johnstonscode.samples.jaxb.model.Configuration;
import org.junit.Test;

/**
 * @author simonjo
 *
 */
public class ReportingParserTests {
    
    @SuppressWarnings("boxing")
    private Configuration runParserTest(final String source, final int expectedErrors) throws ParserConfigurationException, IOException {
        final ByteArrayInputStream input = new ByteArrayInputStream(source.getBytes());
        final ReportingParser<Configuration> parser = new ReportingParser<Configuration>();
        final Configuration root = parser.parse(input, Configuration.class);
        
        if (!parser.getEvents().isEmpty()) {
            System.out.println("Errors:");
            for (final ValidationError event : parser.getEvents()) {
                System.out.println(String.format(
                        "    %s (%d:%d) %s [%s]",
                        event.getSeverity().toString(),
                        event.getLocation().getLineNumber(),
                        event.getLocation().getColumnNumber(),
                        event.getMessage(),
                        event.getCause()));
            }
        } else {
            Assert.assertNotNull(root);
        }
        Assert.assertEquals(expectedErrors, parser.getEvents().size());

        System.out.println("Objects:");
        for (final Entry<Object, LocationImpl> entry : parser.getLocationMap().entrySet()) {
            System.out.println(String.format(
                    "    %s (%d:%d)",
                    entry.getKey().getClass().getName(),
                    entry.getValue().getLineNumber(),
                    entry.getValue().getColumnNumber()));
        }
        return root;
    }
    
    @Test
    public void testConfigurationWithoutNamespace() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration></Configuration>", 2);
    }

    @Test
    public void testOKConfiguration() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration xmlns=\"http://example.org/xmlns/configuration\"></Configuration>", 0);
    }

    @Test
    public void testOKConfigurationNs() throws ParserConfigurationException, IOException {
        runParserTest(
                "<config:Configuration xmlns:config=\"http://example.org/xmlns/configuration\"></config:Configuration>", 0);
    }

    @Test
    public void testBadConfiguration() throws ParserConfigurationException, IOException {
        runParserTest(
                "<configuration xmlns=\"http://example.org/xmlns/configuration\"></configuration>", 2);
    }

    @Test
    public void testBadContent() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration xmlns=\"http://example.org/xmlns/configuration\">" +
                "<keyValuePair/>" +
                "</Configuration>", 1);
    }

    @Test
    public void testGoodishProperty() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration xmlns=\"http://example.org/xmlns/configuration\">" +
                "<property key=\"name\" />" +
                "<property value=\"a value\" />" +
                "</Configuration>", 0);
    }

    @Test
    public void testGoodProperties() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration xmlns=\"http://example.org/xmlns/configuration\">\n" +
        		"  <property key=\"name\" value=\"a value\"></property>\n" +
                "  <property key=\"name2\" value=\"a value\"></property>\n" +
                "  <property key=\"name3\" value=\"a value\"/>\n" +
        		"</Configuration>", 0);
    }

    @Test
    public void testBadProperties() throws ParserConfigurationException, IOException {
        runParserTest(
                "<Configuration xmlns=\"http://example.org/xmlns/configuration\">\n" +
                "  <property key=\"name\" value=\"a value\"></property>\n" +
                "  <keyValue key=\"name2\" value=\"a value\"></keyValue>\n" +
                "  <pair key=\"name3\" value=\"a value\"></pair>\n" +
                "</Configuration>", 2);
    }
}
