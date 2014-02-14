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

package org.gatein.portal.controller.resource.script;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.portal.controller.resource.Resource;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 */
public class BaseScriptResource<R extends Resource<R>> extends Resource<R> {

    /**
     * This is quite closely tied to what is set in {@code controller.xml}.
     * @return
     */
    public static Map<QualifiedName, String> createBaseParameters() {
        Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
        parameters.put(WebAppController.HANDLER_PARAM, ResourceRequestHandler.SCRIPT_HANDLER_NAME);
        parameters.put(ResourceRequestHandler.COMPRESS_QN, "");
        parameters.put(ResourceRequestHandler.VERSION_QN, ResourceRequestHandler.VERSION);
        parameters.put(ResourceRequestHandler.LANG_QN, "");
        return parameters;
    }

    /** . */
    ScriptGraph graph;

    /** . */
    private final Map<QualifiedName, String> parameters;

    /** . */
    private final Map<Locale, Map<QualifiedName, String>> parametersMap;

    /** . */
    private final Map<QualifiedName, String> minParameters;

    /** . */
    private final Map<Locale, Map<QualifiedName, String>> minParametersMap;

    BaseScriptResource(ScriptGraph graph, ResourceId id) {
        super(id);

        //
        Map<QualifiedName, String> parameters = createBaseParameters();
        parameters.put(ResourceRequestHandler.RESOURCE_QN, id.getName());
        parameters.put(ResourceRequestHandler.SCOPE_QN, id.getScope().name());

        //
        Map<QualifiedName, String> minifiedParameters = new HashMap<QualifiedName, String>(parameters);
        minifiedParameters.put(ResourceRequestHandler.COMPRESS_QN, "min");

        //
        this.parameters = parameters;
        this.minParameters = minifiedParameters;
        this.graph = graph;
        this.parametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
        this.minParametersMap = new HashMap<Locale, Map<QualifiedName, String>>();
    }

    public Map<QualifiedName, String> getParameters(boolean minified, Locale locale) {
        Map<Locale, Map<QualifiedName, String>> map = minified ? minParametersMap : parametersMap;
        for (Locale current = locale; current != null; current = I18N.getParent(current)) {
            Map<QualifiedName, String> ret = map.get(locale);
            if (ret != null) {
                return ret;
            }
        }
        return minified ? minParameters : parameters;
    }

    public void addSupportedLocale(Locale locale) {
        if (!parametersMap.containsKey(locale)) {
            Map<QualifiedName, String> localizedParameters = new HashMap<QualifiedName, String>(parameters);
            localizedParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
            parametersMap.put(locale, localizedParameters);
            Map<QualifiedName, String> localizedMinParameters = new HashMap<QualifiedName, String>(minParameters);
            localizedMinParameters.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(locale));
            minParametersMap.put(locale, localizedMinParameters);
        }
    }
}
