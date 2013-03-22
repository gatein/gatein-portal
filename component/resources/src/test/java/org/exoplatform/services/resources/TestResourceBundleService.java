package org.exoplatform.services.resources;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "bundles/resource-bundle-configuration.xml") })
public class TestResourceBundleService extends AbstractKernelTest {

    private static String test_res = "bundles.portal.test";

    private ResourceBundleService service_;

    private LocaleConfigService lservice_;

    private Locale localeBK;

    public TestResourceBundleService(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        // Backup the default locale of JVM to be able to restored in tearDown()
        localeBK = Locale.getDefault();

        PortalContainer manager = PortalContainer.getInstance();
        service_ = (ResourceBundleService) manager.getComponentInstanceOfType(ResourceBundleService.class);
        lservice_ = (LocaleConfigService) manager.getComponentInstanceOfType(LocaleConfigService.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        // Restore the default locale of JVM from backup
        Locale.setDefault(localeBK);
    }

    public void testDefaultLocale() {
        assertEquals(Locale.FRENCH, lservice_.getDefaultLocaleConfig().getLocale());

        // Set up default locale of JVM is different than PORTAL
        // Make sure that default locale of PORTAL will be used instead of JVM locale
        Locale.setDefault(Locale.ENGLISH);

        // Portal resource bundle
        ResourceBundle res = service_.getResourceBundle(test_res, Locale.GERMAN);
        assertEquals("French", res.getString("language"));

        res = service_.getResourceBundle(test_res, new Locale("vi"));
        assertEquals("TiengViet", res.getString("language"));
        assertEquals("base_vi.xml", res.getString("base_vi"));

        // Portlet resource bundle
        res = service_.getResourceBundle("bundles.portlet.test", Locale.GERMAN);
        assertEquals("French", res.getString("language"));
    }

    public void testXMLResourceBunble() {
        ResourceBundle res = service_.getResourceBundle(test_res, new Locale("vi"));
        assertEquals("base_vi.xml", res.getString("base_vi"));
    }
    
    public void testCachingPortletBundle() {
        String oldValue = PropertyManager.getProperty(PropertyManager.DEVELOPING);
        try {
            PropertyManager.setProperty(PropertyManager.DEVELOPING, "true");
            assertTrue(PropertyManager.isDevelopping());
            ResourceBundle res = service_.getResourceBundle("bundles.portlet.test", Locale.ENGLISH);
            assertNotNull(res);
            assertTrue(res != service_.getResourceBundle("bundles.portlet.test", Locale.ENGLISH));

            PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
            assertFalse(PropertyManager.isDevelopping());
            res = service_.getResourceBundle("bundles.portlet.test", Locale.ENGLISH);
            assertNotNull(res);
            assertTrue(res == service_.getResourceBundle("bundles.portlet.test", Locale.ENGLISH));
        } finally {
            PropertyManager.setProperty(PropertyManager.DEVELOPING, oldValue);
        }
    }

    public void testPortalFallbackLocale() {
        assertFallback("portal");
    }

    public void testPortletFallbackLocale() {
        assertFallback("portlet");
    }

    private void assertFallback(String app) {
        String baseName = "bundles." + app + ".base";
        ResourceBundle res = service_.getResourceBundle(baseName, Locale.ENGLISH);
        assertEquals("English", res.getString("language"));

        res = service_.getResourceBundle(baseName, Locale.FRENCH);
        assertEquals("Default", res.getString("language"));

        res = service_.getResourceBundle(baseName, Locale.GERMAN);
        assertEquals("Default", res.getString("language"));

        // Test locale with no base file
        baseName = "bundles." + app + ".no-base";
        res = service_.getResourceBundle(baseName, Locale.ENGLISH);
        assertEquals("English", res.getString("language"));

        res = service_.getResourceBundle(baseName, new Locale("vi"));
        assertEquals("TiengViet", res.getString("language"));
        assertEquals("base_vi.properties", res.getString("base_vi"));

        res = service_.getResourceBundle(baseName, Locale.GERMAN);
        assertEquals("English", res.getString("language"));
        assertEquals("base_en.properties", res.getString("base_en"));
    }

    public void testResourceBundleServiceUpdate() throws Exception {
        // -------getResourceBundle have loaded from property file to database--------
        String bundle = "portal.locale";

        String PROPERTIES = "language=en";

        String PROPERTIES_FR = "language=fr\n" +
                               "property=property";

        String PROPERTIES_EN_UPDATE = "language=en\n" +
	                                  "property=en-property";
        // //------------create ressource bundle in database------------------
        createResourceBundle(bundle, PROPERTIES, Locale.ENGLISH);
        createResourceBundle(bundle, PROPERTIES_FR, Locale.FRENCH);

        ResourceBundle res = service_.getResourceBundle(bundle, Locale.ENGLISH);
        assertEquals("en", res.getString("language"));
        assertEquals("property", res.getString("property"));

        res = service_.getResourceBundle(bundle, Locale.FRENCH);
        assertEquals("fr", res.getString("language"));

        // --------- Update a databseRes resource bundle in database ----------------
        createResourceBundle(bundle, PROPERTIES_EN_UPDATE, Locale.ENGLISH);
        res = service_.getResourceBundle(bundle, Locale.ENGLISH);
        assertEquals("en-property", res.getString("property"));
 
        ResourceBundleData data = service_.getResourceBundleData(bundle + "_en");
        service_.removeResourceBundleData(data.getId());

        assertNull(service_.getResourceBundleData(bundle + "_en"));
    }

    private void createResourceBundle(String name, String datas, Locale locale) throws Exception {
        ResourceBundleData data = service_.createResourceBundleDataInstance();
        data.setName(name);
        data.setData(datas);
        data.setLanguage(locale.getLanguage());
        if (locale.getCountry().trim().length() != 0) {
            data.setCountry(locale.getCountry());
        }
        service_.saveResourceBundle(data);
    }
}
