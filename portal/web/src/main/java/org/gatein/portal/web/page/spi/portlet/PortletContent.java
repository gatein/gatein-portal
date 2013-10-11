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
package org.gatein.portal.web.page.spi.portlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.portal.web.page.Decoder;
import org.gatein.portal.web.page.Encoder;
import org.gatein.portal.web.page.NodeState;
import org.gatein.portal.web.page.spi.WindowContent;
import org.gatein.portal.web.page.Decoder;

/**
 * @author Julien Viet
 */
class PortletContent extends WindowContent {

    /** . */
    public static final Pattern PORTLET_PATTERN = Pattern.compile("^([^/]+)/([^/]+)$");

    /** . */
    private static final String[] TITLE_KEYS = { MetaInfo.TITLE, MetaInfo.SHORT_TITLE, MetaInfo.DISPLAY_NAME };

    /** . */
    static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();

    /** The window id. */
    public final String id;

    /** The portlet window parameters. */
    public Map<String, String[]> parameters;

    /** The portlet window state. */
    public org.gatein.pc.api.WindowState windowState;

    /** The portlet window state. */
    public org.gatein.pc.api.Mode mode;

    /** The related portlet or null if it cannot be located. */
    public final Portlet portlet;

    PortletContent(NodeState node, Portlet portlet) {
        this.id = node.context.getId();
        this.parameters = null;
        this.windowState = null;
        this.mode = null;
        this.portlet = portlet;
    }

    PortletContent(PortletContent that) {

        Map<String, String[]> parameters;
        if (that.parameters == null) {
            parameters = null;
        } else if (that.parameters.isEmpty()) {
            parameters = NO_PARAMETERS;
        } else {
            parameters = new HashMap<String, String[]>(that.parameters);
            for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                parameter.setValue(parameter.getValue().clone());
            }
        }

        //
        this.id = that.id;
        this.parameters = parameters;
        this.windowState = that.windowState;
        this.mode = that.mode;
        this.portlet = that.portlet;
    }

    @Override
    public Map<String, String[]> computePublicParameters(Map<QName, String[]> parameters) {
        Map<String, String[]> publicParameters = NO_PARAMETERS;
        NavigationInfo info = portlet.getInfo().getNavigation();
        for (Map.Entry<QName, String[]> parameter : parameters.entrySet()) {
            ParameterInfo parameterInfo = info.getPublicParameter(parameter.getKey());
            if (parameterInfo != null) {
                if (publicParameters == NO_PARAMETERS) {
                    publicParameters = new HashMap<String, String[]>();
                }
                publicParameters.put(parameterInfo.getId(), parameter.getValue());
            }
        }
        return publicParameters;

    }

    @Override
    public Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes) {
        LinkedHashMap<QName, String[]> pc = new LinkedHashMap<QName, String[]>();
        NavigationInfo info = portlet.getInfo().getNavigation();
        for (Map.Entry<String, String[]> change : changes.entrySet()) {
            ParameterInfo parameterInfo = info.getPublicParameter(change.getKey());
            if (parameterInfo != null) {
                pc.put(parameterInfo.getName(), change.getValue());
            }
        }
        return pc.entrySet();
    }

    @Override
    public String getParameters() {
        if (parameters != null && parameters.size() > 0) {
            Encoder encoder = new Encoder(parameters);
            return encoder.encode();
        } else {
            return null;
        }
    }

    @Override
    public void setParameters(String s) {
        Decoder decoder = new Decoder(s);
        parameters = decoder.decode().getParameters();
    }

    @Override
    public String getWindowState() {
        if (windowState != null && !windowState.equals(org.gatein.pc.api.WindowState.NORMAL)) {
            return windowState.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setWindowState(String ws) {
        this.windowState = org.gatein.pc.api.WindowState.create(ws);
    }

    @Override
    public String getMode() {
        if (mode != null && !mode.equals(Mode.VIEW)) {
            return mode.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setMode(String m) {
        this.mode = org.gatein.pc.api.Mode.create(m);
    }

    @Override
    public String resolveTitle(Locale locale) {
        PortletInfo info = portlet.getInfo();
        String resolved = resolveMetaValue(info, locale, TITLE_KEYS, 0);
        if (resolved == null) {
            resolved = info.getName();
        }
        return resolved;
    }

    private String resolveMetaValue(PortletInfo info, Locale locale, String[] keys, int index) {
        String resolved = null;
        if (index < keys.length) {
            LocalizedString display = info.getMeta().getMetaValue(keys[index]);
            if (display != null) {
                LocalizedString.Value value = display.getValue(locale, true);
                if (value != null) {
                    resolved = value.getString();
                    if (resolved == null) {
                        resolved = resolveMetaValue(info, locale, keys, index + 1);
                    }
                }
            }
        }
        return resolved;
    }

    @Override
    public WindowContent copy() {
        return new PortletContent(this);
    }
}
