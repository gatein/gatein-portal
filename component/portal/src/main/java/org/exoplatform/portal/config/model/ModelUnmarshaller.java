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

package org.exoplatform.portal.config.model;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.exoplatform.portal.mop.Visibility;
import org.gatein.common.io.IOTools;
import org.gatein.portal.content.Content;
import org.gatein.portal.content.ContentType;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.staxnav.Naming;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ModelUnmarshaller {

    /** . */
    private static final String[] EMPTY_STRINGS = new String[0];

    private static <E extends Enum<E>> StaxNavigator<E> createStaxNav(InputStream in, Class<E> type) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty("javax.xml.stream.isCoalescing", true);
        XMLStreamReader stream = inputFactory.createXMLStreamReader(in);
        StaxNavigator<E> nav = StaxNavigatorFactory.create(new Naming.Enumerated.Simple<E>(type, null), stream);
        nav.setTrimContent(true);
        return nav;
    }

    public static <T> UnmarshalledObject<T> unmarshall(Class<T> type, InputStream in) throws Exception {
        return unmarshall(type, IOTools.getBytes(in));
    }

    public static <T> UnmarshalledObject<T> unmarshall(Class<T> type, byte[] bytes) throws Exception {
        ByteArrayInputStream baos = new ByteArrayInputStream(bytes);

        // Find out version
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(baos);
        Version version = Version.UNKNOWN;
        while (reader.hasNext()) {
            int next = reader.next();
            if (next == XMLStreamReader.START_ELEMENT) {
                QName name = reader.getName();
                String uri = name.getNamespaceURI();
                if (uri != null) {
                    version = Version.forURI(uri);
                }
                break;
            }
        }

        //
        baos.reset();
        T obj;
        if (type == PageNavigation.class && version == Version.V_2_0) {
            obj = type.cast(parseNavigation(baos));
        } else if (type == Page.PageSet.class && version == Version.V_2_0) {
            obj = type.cast(parsePageSet(baos));
        } else if (type == PortalConfig.class && version == Version.V_2_0) {
            obj = type.cast(parseSite(baos));
        } else {

            // Legacy parsing using JIBX shit
            IBindingFactory bfact = BindingDirectory.getFactory(type);
            UnmarshallingContext uctx = (UnmarshallingContext) bfact.createUnmarshallingContext();
            uctx.setDocument(baos, null, "UTF-8", false);
            obj = type.cast(uctx.unmarshalElement());
        }

        //
        return new UnmarshalledObject<T>(version, obj);
    }

    enum NavigationElement {

        navigation, node, name, parent_uri, display_name, icon, start_publication_date, end_publication_date, visibility, page_ref

    }

    /** . */
    private static final QName LANG = new QName("http://www.w3.org/XML/1998/namespace", "lang");

    /** . */
    private static final Pattern RFC1766_PATTERN = Pattern.compile("^([a-zA-Z]{2})(?:-([a-zA-Z]{2}))?$");

    /** . */
    private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat();
        }
    };

    //
    private static PageNavigation parseNavigation(InputStream in) throws XMLStreamException, ParseException {
        StaxNavigator<NavigationElement> nav = createStaxNav(in, NavigationElement.class);

        //
        validate(NavigationElement.navigation == nav.getName());
        PageNavigation navigation = new PageNavigation();
        if (nav.child(NavigationElement.node)) {
            for (StaxNavigator<NavigationElement> portletNav : nav.fork(NavigationElement.node)) {
                PageNode root = parseNode(portletNav);
                NavigationFragment fragment = new NavigationFragment();
                fragment.setParentURI(root.getName());
                fragment.setState(root.getState());
                fragment.setNodes(root.getNodes());
                fragment.setLabels(root.getLabels());
                navigation.addFragment(fragment);
            }
        }

        //
        return navigation;
    }

    //
    private static PageNode parseNode(StaxNavigator<NavigationElement> nav) throws ParseException {
        PageNode node = new PageNode();
        for (NavigationElement child = nav.child();child != null;child = nav.sibling()) {
            switch (child) {
                case name:
                case parent_uri:
                    node.setName(nav.getContent());
                    break;
                case display_name:
                    String lang = nav.getAttribute(LANG);
                    String value = nav.getContent();

                    Locale locale = null;
                    if (lang != null) {
                        Matcher matcher = RFC1766_PATTERN.matcher(lang);
                        if (matcher.matches()) {
                            String langISO = matcher.group(1);
                            String countryISO = matcher.group(2);
                            if (countryISO == null) {
                                locale = new Locale(langISO.toLowerCase());
                            } else {
                                locale = new Locale(langISO.toLowerCase(), countryISO.toLowerCase());
                            }
                        } else {
                            throw new RuntimeException("The attribute xml:lang " + lang
                                    + " does not represent a valid language as defined by RFC 1766");
                        }
                    }
                    I18NString labels = node.getLabels();
                    if (labels == null) {
                        node.setLabels(labels = new I18NString());
                    }
                    labels.add(new LocalizedString(value, locale));
                    break;
                case icon:
                    node.setIcon(nav.getContent());
                    break;
                case start_publication_date:
                    node.setStartPublicationDate(dateFormat.get().parse(nav.getContent()));
                    break;
                case end_publication_date:
                    node.setEndPublicationDate(dateFormat.get().parse(nav.getContent()));
                    break;
                case visibility:
                    node.setVisibility(Visibility.valueOf(nav.getContent()));
                    break;
                case page_ref:
                    node.setPageReference(nav.getContent());
                    break;
                case node:
                    PageNode childNode = parseNode(nav.fork());
                    node.getChildren().add(childNode);
                    break;
            }
        }
        return node;
    }

    enum PageElement {

        page_set, page, name, display_name, description, access_permission, edit_permission

    }

    private static Page.PageSet parsePageSet(InputStream in) throws XMLStreamException, ParseException {

        StaxNavigator<PageElement> nav = createStaxNav(in, PageElement.class);

        //
        return parsePageSet(nav);
    }

    public static void validate(boolean b) {
        if (!b) {
            throw new RuntimeException("Parse exception");
        }
    }

    static Page.PageSet parsePageSet(StaxNavigator<PageElement> nav) {
        Page.PageSet set = new Page.PageSet();
        validate(PageElement.page_set == nav.getName());
        if (nav.child(PageElement.page)) {
            for (StaxNavigator<PageElement> pageNav : nav.fork(PageElement.page)) {
                validate(pageNav.child(PageElement.name));
                String name = pageNav.getContent();
                String[] accessPermission = pageNav.sibling(PageElement.access_permission) ? new String[]{pageNav.getContent()} : EMPTY_STRINGS;
                String[] editPermission = pageNav.sibling(PageElement.edit_permission) ? new String[]{pageNav.getContent()} : EMPTY_STRINGS;
                Page page = new Page();
                page.setName(name);
                page.setAccessPermissions(accessPermission);
                page.setEditPermissions(editPermission);
                set.getPages().add(page);
                String layout = parseLayout(pageNav.fork(new Naming.Enumerated.Simple<LayoutElement>(LayoutElement.class, null)), page.getChildren());
                page.setFactoryId(layout);
            }
        }
        return set;
    }

    enum SiteElement {

        site, name, display_name, description, access_permission, edit_permission, locale, properties, entry

    }

    private static PortalConfig parseSite(InputStream in) throws XMLStreamException, ParseException {

        StaxNavigator<SiteElement> nav = createStaxNav(in, SiteElement.class);

        validate(SiteElement.site == nav.getName());
        validate(nav.next(SiteElement.name));
        String name = nav.getContent();
        String displayName = nav.sibling(SiteElement.display_name) ? nav.getContent() : null;
        String description = nav.sibling(SiteElement.description) ? nav.getContent() : null;
        String[] accessPermission = nav.sibling(SiteElement.access_permission) ? new String[]{nav.getContent()} : EMPTY_STRINGS;
        String[] editPermission = nav.sibling(SiteElement.edit_permission) ? new String[]{nav.getContent()} : EMPTY_STRINGS;
        String locale = nav.sibling(SiteElement.locale) ? nav.getContent() : null;
        Properties properties = null;
        if (nav.sibling(SiteElement.properties)) {
            StaxNavigator<SiteElement> f = nav.fork();
            if (f.child(SiteElement.entry)) {
                properties = new Properties();
                do {
                    String key = f.getAttribute("key");
                    String value = f.getContent();
                    properties.put(key, value);
                }
                while (f.sibling(SiteElement.entry));
            }
        }

        //
        PortalConfig site = new PortalConfig();
        site.setProperties(properties);
        site.setName(name);
        site.setLabel(displayName);
        site.setDescription(description);
        site.setLocale(locale);
        site.setAccessPermissions(accessPermission);
        site.setEditPermissions(editPermission);

        //
        String layout = parseLayout(nav.fork(new Naming.Enumerated.Simple<LayoutElement>(LayoutElement.class, null)), site.getPortalLayout().getChildren());
        site.getPortalLayout().setFactoryId(layout);

        //
        return site;
    }

    enum LayoutElement {

        zone, name, layout, id, portlet, display_name, description, access_permission, edit_permission,
        show_info_bar, show_application_state, show_application_mode, application_ref, portlet_ref, preferences,
        preference, value, read_only

    }

    private static String parseLayout(StaxNavigator<LayoutElement> pageNav, Collection<ModelObject> zones) {
        String layout;
        if (pageNav.sibling(LayoutElement.layout)) {
            layout = pageNav.getContent();
        } else {
            layout = null;
        }
        while (pageNav.sibling(LayoutElement.zone)) {
            StaxNavigator<String> zoneNav = pageNav.fork(new Naming.Local());
            validate(zoneNav.child("id"));
            String id = zoneNav.getContent();
            Container zone = new Container();
            zone.setStorageName(id);
            for (String p = zoneNav.sibling();p != null;p = zoneNav.sibling()) {
                StaxNavigator<String> windowNav = zoneNav.fork(new Naming.Local());
                validate(windowNav.child("name"));
                String windowName = windowNav.getContent();
                String windowTitle = windowNav.sibling("display-name") ? windowNav.getContent() : null;
                String windowDescription = windowNav.sibling("description") ? windowNav.getContent() : null;
                String windowAccessPermission = windowNav.sibling("access-permission") ? windowNav.getContent() : null;
                String windowEditPermission = windowNav.sibling("edit-permission") ? windowNav.getContent() : null;
                String windowShowInfoBar = windowNav.sibling("show-info-bar") ? windowNav.getContent() : null;
                String windowShowApplicationState = windowNav.sibling("show-application-state") ? windowNav.getContent() : null;
                String windowShowApplicationMode = windowNav.sibling("show-application-mode") ? windowNav.getContent() : null;
                Application<?> application;
                ContentType<?> provider = ContentType.forTag(p);
                if (provider == null) {
                    throw new UnsupportedOperationException("Unregistered content type " + p);
                } else {
                    application = createApplication(windowNav, provider);
                }
                application.setStorageName(windowName);
                application.setTitle(windowTitle);
                application.setDescription(windowDescription);
                application.setAccessPermission(windowAccessPermission);
                application.setShowInfoBar("true".equals(windowShowInfoBar));
                application.setShowApplicationMode("true".equals(windowShowApplicationMode));
                application.setShowApplicationState("true".equals(windowShowApplicationState));
                zone.getChildren().add(application);
            }
            zones.add(zone);
        }
        return layout;
    }

    private static  <S extends Serializable> Application<S> createApplication(StaxNavigator<String> xml, ContentType<S> provider) {
        Content<S> content = provider.readState(xml);
        Application<S> application = new Application<S>(provider.getApplicationType());
        TransientApplicationState<S> state = new TransientApplicationState<S>(content.id, content.state);
        application.setState(state);
        return application;
    }
}
