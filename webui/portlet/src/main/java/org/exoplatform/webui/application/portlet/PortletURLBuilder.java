/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.webui.application.portlet;

import javax.portlet.PortletURL;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.portal.url.URLWriter;

/**
 * Created by The eXo Platform SAS Apr 3, 2007
 */
public class PortletURLBuilder extends URLBuilder<UIComponent> {

    /** . */
    private final PortletURL url;

    public static final String CSRF_PROP = "gtn:csrfCheck";

    public PortletURLBuilder(PortletURL url) {
        this.url = url;
    }

    public String createAjaxURL(UIComponent targetComponent, String action, String confirm, String targetBeanId,
            Parameter[] params, boolean csrfCheck) {
        return createURL(true, confirm, targetComponent, action, targetBeanId, params, csrfCheck);
    }

    public String createURL(UIComponent targetComponent, String action, String confirm, String targetBeanId,
            Parameter[] params, boolean csrfCheck) {
        return createURL(false, confirm, targetComponent, action, targetBeanId, params, csrfCheck);
    }

    private String createURL(boolean ajax, String confirm, UIComponent targetComponent, String action, String targetBeanId,
            Parameter[] params, boolean csrfCheck) {
        // Clear URL
        url.getParameterMap().clear();

        //
        url.setProperty("gtn:ajax", Boolean.toString(ajax));
        url.setProperty("gtn:confirm", confirm);

        //
        url.setParameter(UIComponent.UICOMPONENT, targetComponent.getId());

        //
        if (action != null && action.trim().length() > 0) {
            url.setParameter(WebuiRequestContext.ACTION, action);
        }

        //
        if (targetBeanId != null && targetBeanId.trim().length() > 0) {
            url.setParameter(UIComponent.OBJECTID, targetBeanId);
        }

        //
        if (params != null && params.length > 0) {
            for (Parameter param : params) {
                url.setParameter(param.getName(), param.getValue());
            }
        }

        //
        if (removeLocale) {
            url.setProperty("gtn:lang", "");
        } else if (locale != null) {
            url.setProperty("gtn:lang", locale.toString());
        }

        //
        if (csrfCheck) {
            url.setProperty(CSRF_PROP, Boolean.TRUE.toString());
        }

        return URLWriter.toString(url);

    }
}
