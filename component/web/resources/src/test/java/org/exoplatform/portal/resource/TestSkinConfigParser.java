package org.exoplatform.portal.resource;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.portal.resource.config.tasks.SkinConfigTask;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class TestSkinConfigParser extends TestCase {
    public void testResources1_0() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_0.xml");
    }

    public void testResources1_0WithSkinModule() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_0-with-skin-module.xml");
    }

    public void testResources1_1() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_1.xml");
    }

    public void testResources1_2() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_2.xml");
    }

    public void testResources1_3() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_3.xml");
    }

    public void testResources1_4() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_4.xml");
    }

    public void testResources1_4_1() throws IOException, SAXException {
        assertDescriptorCanBeLoaded("org/exoplatform/portal/resource/gatein-resources-1_4_1.xml");
    }

    private void assertDescriptorCanBeLoaded(String descriptorPath) throws IOException, SAXException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(descriptorPath);
        assertNotNull("The " + descriptorPath + " can not be found", url);
        Document document = GateInResourcesSchemaValidator.validate(url);
        List<SkinConfigTask> tasks = SkinConfigParser.parse(document);
        assertNotNull("There are no tasks", tasks);
        assertEquals(8, tasks.size());
    }
}
