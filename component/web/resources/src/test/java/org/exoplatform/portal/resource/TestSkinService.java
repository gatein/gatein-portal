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

import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestSkinService extends AbstractSkinServiceTest {
    private static boolean isFirstStartup = true;

    boolean isDevelopingMode() {
        return false;
    }

    @Override
    boolean setUpTestEnvironment() {
        return isFirstStartup;
    }

    Router getRouter() {
        Router router;
        try {
            router = router().add(
                    route("/skins/{gtn:version}/{gtn:resource}{gtn:compress}{gtn:orientation}.css")
                            .with(routeParam("gtn:handler").withValue("skin"))
                            .with(pathParam("gtn:version").matchedBy("[^/]*").preservePath())
                            .with(pathParam("gtn:orientation").matchedBy("-(lt)|-(rt)|").captureGroup(true))
                            .with(pathParam("gtn:compress").matchedBy("-(min)|").captureGroup(true))
                            .with(pathParam("gtn:resource").matchedBy(".+?").preservePath())).build();
            return router;
        } catch (RouterConfigException e) {
            return null;
        }
    }

    @Override
    void touchSetUp() {
        isFirstStartup = false;
    }

    public void testRenderURL() {
        SkinURL skinURL = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin").createURL(controllerCtx);
        assertEquals("/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/FirstPortlet-min-lt.css", skinURL.toString());
        skinURL.setOrientation(Orientation.RT);
        assertEquals("/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/FirstPortlet-min-rt.css", skinURL.toString());
    }

    public void testCompositeSkin() throws NullPointerException, MalformedURLException {
        SkinConfig fSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
        SkinConfig sSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
        assertNotNull(fSkin);
        assertNotNull(sSkin);

        Skin merged = skinService.merge(Arrays.asList(fSkin, sSkin));
        SkinURL url = merged.createURL(controllerCtx);

        url.setOrientation(Orientation.LT);
        assertEquals(".FirstPortlet {foo1 : bar1}\n.SecondPortlet {foo2 : bar2}",
                skinService.getCSS(newControllerContext(getRouter(), url.toString()), true));

        url.setOrientation(Orientation.RT);
        assertEquals(".FirstPortlet {foo1 : bar1}\n.SecondPortlet {foo2 : bar2}",
                skinService.getCSS(newControllerContext(getRouter(), url.toString()), true));
    }

    public void testCache() throws Exception {
        String resource = "/path/to/test/cache.css";
        String url = newSimpleSkin(resource).createURL(controllerCtx).toString();

        resResolver.addResource(resource, "foo");
        assertEquals("foo", skinService.getCSS(newControllerContext(getRouter(), url), true));

        resResolver.addResource(resource, "bar");
        assertEquals("foo", skinService.getCSS(newControllerContext(getRouter(), url), true));
    }

    public void testInvalidateCache() throws Exception {
        String resource = "/path/to/test/invalidate/cache.css";
        String url = newSimpleSkin(resource).createURL(controllerCtx).toString();

        resResolver.addResource(resource, "foo");
        assertEquals("foo", skinService.getCSS(newControllerContext(getRouter(), url), true));

        resResolver.addResource(resource, "bar");
        skinService.invalidateCachedSkin(resource);
        assertEquals("bar", skinService.getCSS(newControllerContext(getRouter(), url), true));
    }

    public void testProcessImportCSS() throws Exception {
        String resource = "/process/import/css.css";
        String url = newSimpleSkin(resource).createURL(controllerCtx).toString();

        resResolver.addResource(resource, "@import url(Portlet/Stylesheet.css); aaa;");
        assertEquals(" aaa;", skinService.getCSS(newControllerContext(getRouter(), url), true));
        skinService.invalidateCachedSkin(resource);

        resResolver.addResource(resource, "@import url('/Portlet/Stylesheet.css'); aaa;");
        assertEquals(" aaa;", skinService.getCSS(newControllerContext(getRouter(), url), true));
        skinService.invalidateCachedSkin(resource);

        // parent file import child css file
        resResolver.addResource(resource, "@import url(childCSS/child.css);  background:url(images/foo.gif);");
        String childResource = "/process/import/childCSS/child.css";
        resResolver.addResource(childResource, "background:url(bar.gif);");

        /*
         * Now test merge and process recursively (run in non-dev mode) We have folder /path/to/parent.css /images/foo.gif
         * /childCSS/child.css /bar.gif
         */
        assertEquals("background:url(/process/import/childCSS/bar.gif);  background:url(/process/import/images/foo.gif);",
                skinService.getCSS(newControllerContext(getRouter(), url), true));

        url = newSimpleSkin(childResource).createURL(controllerCtx).toString();
        assertEquals("background:url(/process/import/childCSS/bar.gif);",
                skinService.getCSS(newControllerContext(getRouter(), url), true));
    }

    public void testProcessImportCrossDomainCSS() throws Exception {

        crossDomainCSS("@import url(//example.com/some.css); aaa;");
        crossDomainCSS("@import url('//example.com/some.css'); aaa;");
        crossDomainCSS("@import url(\"//example.com/some.css\"); aaa;");
        crossDomainCSS("@import url(http://example.com/some.css); aaa;");
        crossDomainCSS("@import url('http://example.com/some.css'); aaa;");
        crossDomainCSS("@import url(\"http://example.com/some.css\"); aaa;");
        crossDomainCSS("@import url(https://example.com/some.css); aaa;");
        crossDomainCSS("@import url('https://example.com/some.css'); aaa;");
        crossDomainCSS("@import url(\"https://example.com/some.css\"); aaa;");

        crossDomainCSS("@import '//example.com/some.css'; aaa;");
        crossDomainCSS("@import \"//example.com/some.css\"; aaa;");
        crossDomainCSS("@import 'http://example.com/some.css'; aaa;");
        crossDomainCSS("@import \"http://example.com/some.css\"; aaa;");
        crossDomainCSS("@import 'https://example.com/some.css'; aaa;");
        crossDomainCSS("@import \"https://example.com/some.css\"; aaa;");

    }
    private void crossDomainCSS(String crossDomainCss) throws Exception {
        String masterCssPath = "/process/import/master.css";
        String masterCssUrl = newSimpleSkin(masterCssPath).createURL(controllerCtx).toString();
        resResolver.addResource(masterCssPath, crossDomainCss);
        assertEquals(crossDomainCss, skinService.getCSS(newControllerContext(getRouter(), masterCssUrl), true));
        skinService.invalidateCachedSkin(masterCssPath);
    }



    public void testLastModifiedSince() throws Exception {
        String resource = "/last/modify/since.css";
        SkinURL skinURL = newSimpleSkin(resource).createURL(controllerCtx);

        resResolver.addResource(resource, "foo");

        assertTrue(skinService.getCSS(newControllerContext(getRouter(), skinURL.toString()), true).length() > 0);
        long lastModified = skinService.getLastModified(newControllerContext(getRouter(), skinURL.toString()));
        Thread.sleep(1000);
        assertEquals(lastModified, skinService.getLastModified(newControllerContext(getRouter(), skinURL.toString())));

        skinURL.setOrientation(Orientation.RT);
        Thread.sleep(1000);
        assertTrue(lastModified < skinService.getLastModified(newControllerContext(getRouter(), skinURL.toString())));
    }

    public void testIsExternalUrl() {
        assertFalse(SkinService.isExternalUrl(null));
        assertFalse(SkinService.isExternalUrl(""));
        assertFalse(SkinService.isExternalUrl("/"));
        assertFalse(SkinService.isExternalUrl("/foo"));
        assertFalse(SkinService.isExternalUrl("/foo/bar/baz"));
        assertFalse(SkinService.isExternalUrl("foo/bar/baz"));
        assertFalse(SkinService.isExternalUrl("foo-bar-baz"));
        assertFalse(SkinService.isExternalUrl("http"));
        assertFalse(SkinService.isExternalUrl("https"));

        assertTrue(SkinService.isExternalUrl("//"));
        assertTrue(SkinService.isExternalUrl("//foo"));
        assertTrue(SkinService.isExternalUrl("//foo/bar/baz"));
        assertTrue(SkinService.isExternalUrl("http:"));
        assertTrue(SkinService.isExternalUrl("http://foo"));
        assertTrue(SkinService.isExternalUrl("http://foo/bar/baz"));
        assertTrue(SkinService.isExternalUrl("https:"));
        assertTrue(SkinService.isExternalUrl("https://foo"));
        assertTrue(SkinService.isExternalUrl("https://foo/bar/baz"));
    }
}
