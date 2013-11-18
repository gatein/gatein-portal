/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop.customization;

import java.io.Serializable;

import org.exoplatform.portal.mop.AbstractMopServiceTest;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.customization.CustomizationContext;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.layout.Element;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestCustomizationService extends AbstractMopServiceTest {

    /** . */
    private PageService pageService;

    /** . */
    private CustomizationService customizationService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //
        this.customizationService = context.getCustomizationService();
        this.pageService = context.getPageService();
    }

    public void testPage() {

        PageData page = createPage(createSite(SiteType.PORTAL, "test_layout_page"), "page", new PageState.Builder().build());
        NodeData<ElementState>[] nodes = createElements(page, Element.portlet("app/foo", new Portlet().setValue("weather", "marseille")).title("foo"));

        //
        CustomizationContext<Portlet> customizationContext = customizationService.loadCustomization(nodes[0].id);
        Serializable customizationState = customizationContext.getState();
        assertEquals("application/portlet", customizationContext.getContentType().getValue());
        assertEquals("app/foo", customizationContext.getContentId());
        Portlet state = assertInstanceOf(customizationState, Portlet.class);
        Preference weather = state.getPreference("weather");
        assertEquals("marseille", weather.getValue());

        // Save
        customizationContext.setState(new Portlet().setValue("weather", "paris"));
        customizationService.saveCustomization(customizationContext);

        //

    }
}
