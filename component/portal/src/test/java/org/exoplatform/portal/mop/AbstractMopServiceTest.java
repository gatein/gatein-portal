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

package org.exoplatform.portal.mop;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.hierarchy.NodePersistence;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationPersistence;
import org.gatein.portal.mop.navigation.NavigationServiceImpl;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PagePersistence;
import org.gatein.portal.mop.page.PageServiceImpl;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SitePersistence;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-mop-configuration.xml")
})
public abstract class AbstractMopServiceTest extends AbstractMopTest {

    /** . */
    protected PageServiceImpl pageService;

    /** . */
    protected NavigationServiceImpl navigationService;

    /** . */
    protected DescriptionService descriptionService;

    /** . */
    protected PersistenceContext context;

    protected PersistenceContext createPersistenceContext() {
        return new PersistenceContext.JCR();
    }

    @Override
    protected void setUp() throws Exception {
        context = createPersistenceContext();
        context.setUp();
        navigationService = context.getNavigationService();
        descriptionService = context.getDescriptionService();
        pageService = context.getPageService();
        super.setUp();
    }

    @Override
    protected void begin() {
        super.begin();
        context.begin();
    }

    @Override
    protected void end(boolean save) {
        context.end(save);
        super.end(save);
    }

    protected final NavigationPersistence getNavigationPersistence() {
        return context.getNavigationPersistence();
    }

    protected final SitePersistence getSitePersistence() {
        return context.getSitePersistence();
    }

    protected final PagePersistence getPagePersistence() {
        return context.getPagePersistence();
    }

    protected final SiteData createSite(SiteType type, String siteName) {
        return createSite(type, siteName, new SiteState("fr", "", "", Arrays.<String>asList(), "", Collections.<String, String>emptyMap(), ""));
    }

    protected final SiteData createSite(SiteType type, String siteName, SiteState state) {
        SitePersistence sitePersistence = getSitePersistence();
        SiteKey key = type.key(siteName);
        sitePersistence.saveSite(key, state);
        return sitePersistence.loadSite(key);
    }

    protected final boolean destroySite(SiteType type, String siteName) {
        SitePersistence sitePersistence = getSitePersistence();
        SiteKey key = type.key(siteName);
        return sitePersistence.destroySite(key);
    }

    protected final NodeData createNavigatation(SiteData data) {
        NavigationPersistence navigationPersistence = getNavigationPersistence();
        navigationPersistence.saveNavigation(data.key, new NavigationState(0));
        NavigationData navigation = navigationPersistence.loadNavigationData(data.key);
        return navigationPersistence.loadNode(navigation.rootId);
    }

    protected final NodeData[] createNodeChild(NodeData parent, String... names) {
        NavigationPersistence navigationPersistence = getNavigationPersistence();
        String previous = parent.getLastChild();
        NodeData[] created = new NodeData[names.length];
        for (int i = 0;i < names.length;i++) {
            NodeData<NodeState> child = navigationPersistence.createNode(parent.id, previous, names[i], NodeState.INITIAL)[1];
            created[i] = child;
            previous = created[i].id;
        }
        return created;
    }

    protected final PageData createPage(SiteData data, String name, PageState state) {
        PagePersistence pagePersistence = getPagePersistence();
        PageKey key = data.key.page(name);
        pagePersistence.savePage(key, state);
        return pagePersistence.loadPage(key);
    }

    protected PageData getPage(SiteKey site, String name) {
        return getPage(site.page(name));
    }

    protected PageData getPage(PageKey key) {
        PagePersistence pagePersistence = getPagePersistence();
        return pagePersistence.loadPage(key);
    }

    protected final Map<String, NodeData> createNodes(NodeData parent, Map<String, NodeState> nodes) {
        NavigationPersistence navigationPersistence = getNavigationPersistence();
        String previous = parent.getLastChild();
        LinkedHashMap<String, NodeData> created = new LinkedHashMap<String, NodeData>(nodes.size());
        for (Map.Entry<String, NodeState> node : nodes.entrySet()) {
            NodeData<NodeState> child = navigationPersistence.createNode(parent.id, previous, node.getKey(), node.getValue())[1];
            created.put(node.getKey(), child);
            previous = child.id;
        }
        return created;
    }

    protected final NodeData<ElementState>[] createElements(SiteData site, ElementState.Builder<?>... elements) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        NodeData<ElementState> root = persistence.loadNode(site.layoutId);
        return createElements(root, elements);
    }

    protected final NodeData<ElementState>[] createElements(SiteData site, ElementState... elements) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        NodeData<ElementState> root = persistence.loadNode(site.layoutId);
        return createElements(root, elements);
    }

    protected final NodeData<ElementState>[] createElements(NodeData<ElementState> parent, ElementState.Builder<?>... elements) {
        ElementState[] states = new ElementState[elements.length];
        for (int i = 0;i < elements.length;i++) {
            states[i] = elements[i].build();
        }
        return createElements(parent, states);
    }

    protected final NodeData<ElementState>[] createElements(NodeData<ElementState> parent, ElementState... elements) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        String previous = parent.getLastChild();
        NodeData<ElementState>[] created = new NodeData[elements.length];
        for (int i = 0;i < elements.length;i++) {
            ElementState element = elements[i];
            NodeData<ElementState> child = persistence.createNode(parent.id, previous, UUID.randomUUID().toString(), element)[1];
            created[i] = child;
            previous = child.id;
        }
        return created;
    }

    public final NodeData<ElementState> getElement(SiteData site) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        return persistence.loadNode(site.layoutId);
    }

    public final NodeData<ElementState> getElement(NodeData<ElementState> element) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        return persistence.loadNode(element.id);
    }

    public final NodeData<ElementState> getElement(String parentId) {
        NodePersistence<ElementState> persistence = context.getLayoutPersistence();
        return persistence.loadNode(parentId);
    }

    protected final boolean isSessionModified() {
        return context.isSessionModified();
    }

    protected final void assertSessionNotModified() {
        assertTrue(context.assertSessionNotModified());
    }

    protected final void assertSessionModified() {
        assertTrue(context.assertSessionModified());
    }
}
