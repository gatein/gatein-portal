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
package org.gatein.examples;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author Julien Viet
 */
public class ParametersPortlet implements Portlet {

    /** . */
    private PortletConfig config;

    @Override
    public void init(PortletConfig config) throws PortletException {
        this.config = config;
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        HashMap<String, String[]> p = new HashMap<String, String[]>(request.getParameterMap());
        for (Iterator<String> i = p.keySet().iterator();i.hasNext();) {
            if (i.next().startsWith("_")) {
                i.remove();
            }
        }
        String op = request.getParameter("_op");
        if ("add".equals(op)) {
            String name = request.getParameter("_name");
            String value = request.getParameter("_value");
            if (name != null && value != null) {
                String[] values = p.get(name);
                if (values == null) {
                    values = new String[]{value};
                } else {
                    values = Arrays.copyOf(values, values.length +1 );
                    values[values.length - 1] = value;
                }
                p.put(name, values);
            } else {
                throw new PortletException("Invalid request");
            }
        } else if ("rm".equals(op)) {
            String name = request.getParameter("_name");
            if ("foo".equals(name) || "bar".equals(name)) {
                response.removePublicRenderParameter(name);
            }
            p.remove(name);
        } else {
            throw new PortletException("Invalid request");
        }
        for (Map.Entry<String, String[]> parameter : p.entrySet()) {
            response.setRenderParameter(parameter.getKey(), parameter.getValue());
        }
    }

    @Override
    public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        //
        out.append("<h3>Parameters Portlet</h3>");
        out.append("<p>This portlet allows to interract with the render parameter of the portlet. The portlet" +
                "has shares two public render parameters <span class='label label-info'>foo</span> and " +
                "<span class='label label-info'>bar</span></p>");

        //
        if (request.getParameterMap().size() > 0) {
            PortletURL removeURL = response.createActionURL();
            removeURL.setParameters(request.getParameterMap());
            removeURL.setParameter("_op", "rm");
            out.append("<table class='table'>");
            out.append("<thead><tr><th>Name</th><th>Value</th></tr></thead>");
            out.append("<tbody>");
            for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
                removeURL.setParameter("_name", parameter.getKey());
                out.append("<tr><td>");
                out.append(parameter.getKey());
                out.append(" <a href='").append(removeURL.toString()).append("'>&times;</a>");
                out.append("</td><td>");
                out.append(Arrays.asList(parameter.getValue()).toString());
                out.append("</td></tr>");
            }
            out.append("</tbody>");
            out.append("</table>");
        }

        //
        PortletURL addURL = response.createActionURL();
        addURL.setParameters(request.getPrivateParameterMap());
        addURL.setParameter("_op", "add");
        out.append("<form action='").append(addURL.toString()).append("' method='POST' class='well form-inline'>\n");
        out.append("<input type='text' name='_name' class='input-small' placeholder='Name'>\n");
        out.append("<input type='text' name='_value' class='input-small' placeholder='Value'>\n");
        out.append("<button type='submit' class='btn btn-primary'>Add</button>\n");
        out.append("</form>");

        //
        out.close();
    }

    @Override
    public void destroy() {
        config = null;
    }
}
