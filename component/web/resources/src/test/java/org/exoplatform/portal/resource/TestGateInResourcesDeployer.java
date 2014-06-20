/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
    *
    * This is free software; you can redistribute it and/or modify it
    * under the terms of the GNU Lesser General Public License as
    * published by the Free Software Foundation; either version 2.1 of
    * the License, or (at your option) any later version.
    *
    * This software is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    * Lesser General Public License for more details.
    *
    * You should have received a copy of the GNU Lesser General Public
    * License along with this software; if not, write to the Free
    * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
    */

package org.exoplatform.portal.resource;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.exoplatform.component.test.web.WebAppImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.wci.WebApp;
import org.json.JSONObject;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestGateInResourcesDeployer extends AbstractWebResourceTest {
    private static final ControllerContext CONTROLLER_CONTEXT = new MockControllerContext();
    private static final String MODULE_1_SOURCE = "\ndefine('SHARED/gtnDeployerModule1', [], function() {"
            + "\nvar require = eXo.require, requirejs = eXo.require,define = eXo.define;\neXo.define.names=[];\neXo.define.deps=[];"
            + "\nreturn ccc;\n});";
    private static final String MODULE_2_SOURCE = "\ndefine('SHARED/gtnDeployerModule2', [\"SHARED/gtnDeployerModule1\"], function(gtnDeployerModule1) {"
            + "\nvar require = eXo.require, requirejs = eXo.require,define = eXo.define;\neXo.define.names=[\"gtnDeployerModule1\"];\neXo.define.deps=[gtnDeployerModule1];"
            + "\nreturn ddd;\n});";
    private static final String MODULE_2_SOURCE_WITHOUT_MODULE_1 = "\ndefine('SHARED/gtnDeployerModule2', [], function() {"
            + "\nvar require = eXo.require, requirejs = eXo.require,define = eXo.define;\neXo.define.names=[];\neXo.define.deps=[];"
            + "\nreturn ddd;\n});";
    private static final ResourceId M1 = new ResourceId(ResourceScope.SHARED, "gtnDeployerModule1");
    private static final ResourceId M2 = new ResourceId(ResourceScope.SHARED, "gtnDeployerModule2");
    private static final String PREFIX_1 = "/prefix1";
    private static final String PREFIX_2 = "/prefix2";
    private static final String TARGET_PATH_1 = "//mycdn.com/awesome-lib/1.2.3";
    private static ServletContext mockServletContext;
    private GateInResourcesDeployer deployer;

    private static String getTargetPathMapping(JavascriptConfigService javascriptConfigService, String prefix) throws Exception {
        JSONObject jsConfig = javascriptConfigService.getJSConfig(CONTROLLER_CONTEXT, null);
        JSONObject paths = jsConfig.getJSONObject("paths");
        return paths == null || !paths.has(prefix) ? null : paths.getString(prefix);
    }


    @Override
    protected void setUp() throws Exception {
        final PortalContainer portalContainer = getContainer();
        JavascriptConfigService javascriptConfigService = portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
        SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);
        deployer = new GateInResourcesDeployer("portal", skinService, javascriptConfigService);

        if (mockServletContext == null) {
            Map<String, String> resources = new HashMap<String, String>(6);
            resources.put("/js/gtnDeployerModule1.js", "ccc;");
            resources.put("/js/gtnDeployerModule2.js", "ddd;");
            mockServletContext = new MockJSServletContext("GateInResourcesDeployerApp", resources);
        }
    }

    /**
     * A scenario with a repeated add and remove using interdependent apps.
     *
     * @throws Exception
     */
    public void testAddRemoveValid() throws Exception {
        final PortalContainer portalContainer = getContainer();
        JavascriptConfigService javascriptConfigService = portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
        SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);

        /* ensure there is none of the two apps there initially */
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        /* add webApp1 */
        WebApp webApp1 = new WebAppImpl(mockServletContext, Thread.currentThread().getContextClassLoader());
        URL url1 = portalContainer.getPortalClassLoader().getResource("resources-deployment-service/gatein-resources-valid.xml");
        deployer.add(webApp1, url1);

        assertEquals(TARGET_PATH_1, getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertReader(TestGateInResourcesDeployer.MODULE_1_SOURCE, javascriptConfigService.getScript(M1, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));


        /* add webApp2 that depends on webApp1 */
        Map<String, String> resources = new HashMap<String, String>(6);
        resources.put("/js/gtnDeployerModule2.js", "ddd;");
        MockJSServletContext mockServletContext2 = new MockJSServletContext("GateInResourcesDeployerApp2", resources);
        WebApp webApp2 = new WebAppImpl(mockServletContext2, Thread.currentThread().getContextClassLoader());
        URL url2 = portalContainer.getPortalClassLoader().getResource("resources-deployment-service/gatein-resources-valid-dependent.xml");
        deployer.add(webApp2, url2);

        assertEquals(TARGET_PATH_1, getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertReader(TestGateInResourcesDeployer.MODULE_1_SOURCE, javascriptConfigService.getScript(M1, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

        assertEquals("//mycdn.com/other-lib/6.7.8", getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertReader(TestGateInResourcesDeployer.MODULE_2_SOURCE, javascriptConfigService.getScript(M2, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        /* The crucial point: remove webApp1. It must be possible without errors, although there
         * should be a warning in the log about removing a dependency of webApp2 */
        deployer.remove(webApp1);

        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

        assertEquals("//mycdn.com/other-lib/6.7.8", getTargetPathMapping(javascriptConfigService, PREFIX_2));
        /* note that MODULE_2_SOURCE_WITHOUT_MODULE_1 does not contain the deps from webApp1 */
        assertReader(TestGateInResourcesDeployer.MODULE_2_SOURCE_WITHOUT_MODULE_1, javascriptConfigService.getScript(M2, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        /* re-deployment of the webApp1 must be possible and after that, source of M2 must look like before */
        deployer.add(webApp1, url1);

        assertEquals(TARGET_PATH_1, getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertReader(TestGateInResourcesDeployer.MODULE_1_SOURCE, javascriptConfigService.getScript(M1, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

        assertEquals("//mycdn.com/other-lib/6.7.8", getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertReader(TestGateInResourcesDeployer.MODULE_2_SOURCE, javascriptConfigService.getScript(M2, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        /* finally remove both of them and check that there's nothing left from them */
        deployer.remove(webApp2);
        deployer.remove(webApp1);
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

    }

    public void testAddRemoveBrokenGmdDependencies() throws Exception {
        final PortalContainer portalContainer = getContainer();
        JavascriptConfigService javascriptConfigService = portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
        SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);

        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

        WebApp webApp1 = new WebAppImpl(mockServletContext, Thread.currentThread().getContextClassLoader());
        URL url1 = portalContainer.getPortalClassLoader().getResource("resources-deployment-service/gatein-resources-with-broken-gmd-deps.xml");
        log.info("InvalidResourceException comming...");
        deployer.add(webApp1, url1);

        /* none of the three was added */
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));

    }

    public void testAddRemoveBrokenAmdPaths() throws Exception {
        final PortalContainer portalContainer = getContainer();
        JavascriptConfigService javascriptConfigService = portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
        SkinService skinService = portalContainer.getComponentInstanceOfType(SkinService.class);

        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        WebApp webApp1 = new WebAppImpl(mockServletContext, Thread.currentThread().getContextClassLoader());
        URL url1 = portalContainer.getPortalClassLoader().getResource("resources-deployment-service/gatein-resources-valid.xml");
        deployer.add(webApp1, url1);

        assertEquals(TARGET_PATH_1, getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertReader(TestGateInResourcesDeployer.MODULE_1_SOURCE, javascriptConfigService.getScript(M1, null));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        Map<String, String> resources = new HashMap<String, String>(6);
        resources.put("/js/gtnDeployerModule1.js", "ccc;");
        resources.put("/js/gtnDeployerModule2.js", "ddd;");
        MockJSServletContext mockServletContext2 = new MockJSServletContext("GateInResourcesDeployerApp2", resources);
        WebApp webApp2 = new WebAppImpl(mockServletContext2, Thread.currentThread().getContextClassLoader());
        URL url2 = portalContainer.getPortalClassLoader().getResource("resources-deployment-service/gatein-resources-with-broken-amd-paths.xml");
        log.info("DuplicateResourceKeyException comming...");
        deployer.add(webApp2, url2);

        /* nothing changed */
        assertEquals(TARGET_PATH_1, getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertReader(TestGateInResourcesDeployer.MODULE_1_SOURCE, javascriptConfigService.getScript(M1, null));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNotNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

        /* cleanup */
        deployer.remove(webApp1);
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_1));
        assertNull(getTargetPathMapping(javascriptConfigService, PREFIX_2));
        assertNull(javascriptConfigService.getScript(M1, null));
        assertNull(javascriptConfigService.getScript(M2, null));
        assertNull(skinService.getSkin("resources-deployment-service/FirstPortlet", "TestSkin"));
        assertNull(skinService.getSkin("resources-deployment-service/SecondPortlet", "TestSkin"));

    }

}
