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

package org.exoplatform.portal.url;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.URIWriter;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.URLContext;
import org.exoplatform.webui.CSRFTokenUtil;
import org.gatein.common.io.UndeclaredIOException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PortalURLContext implements URLContext {
    private static final char REPLACEMENT_CHAR = '\ufffd';

    /** . */
    private final ControllerContext controllerContext;

    /** . */
    private final SiteKey siteKey;

    /** . */
    private URIWriter writer;

    /** . */
    private StringBuilder buffer;

    public PortalURLContext(ControllerContext controllerContext, SiteKey siteKey) {
        if (controllerContext == null) {
            throw new NullPointerException("No null controller context");
        }

        //
        this.controllerContext = controllerContext;
        this.siteKey = siteKey;
        this.writer = null;
        this.buffer = null;
    }

    public <R, U extends PortalURL<R, U>> String render(U url) {
        try {
            return _render(url);
        } catch (IOException e) {
            throw new UndeclaredIOException(e);
        }
    }

    private <R, U extends PortalURL<R, U>> String _render(U url) throws IOException {
        if (url.getResource() == null) {
            throw new IllegalStateException("No resource set on portal URL");
        }

        //
        if (writer == null) {
            writer = new URIWriter(buffer = new StringBuilder());
        } else {
            buffer.setLength(0);
            writer.reset(buffer);
        }

        //
        HttpServletRequest req = controllerContext.getRequest();
        if (url.getSchemeUse()) {
            buffer.append(req.getScheme());
            buffer.append("://");
        }
        if (url.getAuthorityUse()) {
            buffer.append(req.getServerName());
            int port = req.getServerPort();
            if (port != 80) {
                buffer.append(':').append(port);
            }
        }

        //
        writer.setMimeType(url.getMimeType());

        //
        String confirm = url.getConfirm();
        boolean hasConfirm = confirm != null && confirm.length() > 0;

        //
        boolean ajax = url.getAjax() != null && url.getAjax();
        if (ajax) {
            writer.append("javascript:");
            if (hasConfirm) {
                writer.append("if(confirm('");
                writer.append(confirm.replaceAll("'", "\\\\'"));
                writer.append("'))");
            }
            writer.append("ajaxGet('");
        } else {
            if (hasConfirm) {
                writer.append("javascript:");
                writer.append("if(confirm('");
                writer.append(confirm.replaceAll("'", "\\\\'"));
                writer.append("'))");
                writer.append("window.location=\'");
            }
        }

        //
        Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
        parameters.put(WebAppController.HANDLER_PARAM, "portal");
        parameters.put(PortalRequestHandler.REQUEST_SITE_TYPE, siteKey.getTypeName());
        parameters.put(PortalRequestHandler.REQUEST_SITE_NAME, siteKey.getName());

        //
        String lang = "";
        Locale locale = url.getLocale();
        if (locale != null && locale.getLanguage().length() > 0) {
            lang = I18N.toTagIdentifier(locale);
        }
        parameters.put(PortalRequestHandler.LANG, lang);

        //
        for (QualifiedName parameterName : url.getParameterNames()) {
            String parameterValue = url.getParameterValue(parameterName);
            if (parameterValue != null) {
                parameters.put(parameterName, parameterValue);
            }
        }

        int untrustedStart = buffer.length();

        // Render url via controller
        controllerContext.renderURL(parameters, writer);

        checkUntrustedData(untrustedStart);

        // Now append generic query parameters
        Map<String, String[]> queryParameters = url.getQueryParameters();
        if (queryParameters != null) {
            for (Map.Entry<String, String[]> entry : queryParameters.entrySet()) {
                for (String value : entry.getValue()) {
                    writer.appendQueryParameter(entry.getKey(), value);
                }
            }
        }

        // CSRF token
        if (url.isCSRFCheck()) {
            String token;
            try {
                token = CSRFTokenUtil.getToken();
                writer.appendQueryParameter(CSRFTokenUtil.CSRF_TOKEN, token);
            } catch (Exception e) {
                throw new IllegalStateException("Can't add csrf token to url", e);
            }
        }

        //
        if (ajax) {
            writer.appendQueryParameter("ajaxRequest", "true");
            writer.append("')");
        } else {
            if (hasConfirm) {
                writer.append("\'");
            }
        }

        //
        return buffer.toString();
    }

    private void checkUntrustedData(int start) {
        int len = buffer.length();
        for (int i = start; i < len; i++) {
            if (buffer.charAt(i) == '\'') {
                buffer.setCharAt(i, REPLACEMENT_CHAR);
            }
        }
    }
}
