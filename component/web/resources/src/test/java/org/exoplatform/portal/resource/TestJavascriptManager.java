/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import java.io.IOException;
import java.net.URL;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.application.javascript.ScriptResources;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMap;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */

public class TestJavascriptManager extends AbstractWebResourceTest {
    private JavascriptManager jsManager;

    private static boolean isFirstStartup = true;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final PortalContainer portalContainer = getContainer();
        JavascriptConfigService jsService = (JavascriptConfigService) portalContainer
                .getComponentInstanceOfType(JavascriptConfigService.class);

        if (isFirstStartup) {
            URL url = portalContainer.getPortalClassLoader().getResource("mockwebapp/gatein-resources.xml");
            ScriptResources scriptResources = new JavascriptConfigParser(new MockServletContext() {
                @Override
                public String getContextPath() {
                    return "mockwebapp";
                }
            }, url.openStream()).parse();
            jsService.add(scriptResources);

            isFirstStartup = false;
        }
        jsManager = new JavascriptManager();
    }

    public void testAddingScriptResources() throws IOException {
        FetchMap<ResourceId> scriptResources = jsManager.getScriptResources();
        assertEquals(0, scriptResources.size());

        jsManager.loadScriptResource(ResourceScope.SHARED, "script1");
        scriptResources = jsManager.getScriptResources();
        assertEquals(1, scriptResources.size());

        // Re-adding the same resource
        jsManager.loadScriptResource(ResourceScope.SHARED, "script1");
        scriptResources = jsManager.getScriptResources();
        assertEquals(1, scriptResources.size());
        assertTrue(scriptResources.containsKey(new ResourceId(ResourceScope.SHARED, "script1")));
    }

    public void testRequireJS() {
        RequireJS require = jsManager.require("SHARED/jquery", "$");
        require.addScripts("$('body').css('color : red');");

        String expected = "window.require([\"SHARED/base\",\"SHARED/jquery\"],function(base,$) {\n$('body').css('color : red');});";
        assertEquals(expected, require.toString());
    }

    public void testNoAlias() {
        RequireJS require = jsManager.require("SHARED/webui");
        require.require("SHARED/jquery", "$");

        // Any module without alias will be pushed to the end of dependency list
        String expected = "window.require([\"SHARED/base\",\"SHARED/jquery\",\"SHARED/webui\"],function(base,$) {\n});";
        assertEquals(expected, require.toString());
    }

    public void testAddOnLoadJavascript() {
        jsManager.require("foo", "bar").addScripts("bar.zoo;");

        String onload = "eXo.core.Browser.init";
        jsManager.addOnLoadJavascript(onload);

        String expected = "window.require([\"SHARED/base\",\"foo\"],function(base,bar) {\n"
                + "bar.zoo;base.Browser.addOnLoadCallback('mid" + Math.abs(onload.hashCode()) + "'," + onload
                + ");base.Browser.onLoad();});";
        assertEquals(expected, jsManager.getJavaScripts());
    }
}
