package org.exoplatform.commons.config;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/commons/config/configuration.xml") })
public class TestGateInConfigurator extends AbstractKernelTest {
    public void testConfig() {
        assertEquals("bar", System.getProperty("foo"));
    }
}
