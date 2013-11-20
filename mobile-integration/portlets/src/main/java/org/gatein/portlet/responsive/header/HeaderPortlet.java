/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.header;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class HeaderPortlet extends NodePortlet {

    private static final Logger log = LoggerFactory.getLogger(HeaderPortlet.class);
    private final int DEFAULT_NODE_LEVEL = 2;
    public static final String NODE_LEVEL_PREFERENCE = "level";

    HeaderBean headerBean;

    public HeaderPortlet() {
        headerBean = new HeaderBean(DEFAULT_NODE_LEVEL);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        headerBean.setNodeLevel(getNodeLevel(request));
        request.setAttribute("headerbean", headerBean);
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/header.jsp");
        prd.include(request, response);
    }

    @Override
    protected int getNodeLevel(PortletRequest request) {
        int nodeLevel = DEFAULT_NODE_LEVEL;
        try {
            nodeLevel = Integer.valueOf(request.getPreferences().getValue(NODE_LEVEL_PREFERENCE,
                    String.valueOf(DEFAULT_NODE_LEVEL)));
        } catch (NumberFormatException nfe) {
            log.warn("Preference for Node level can only be an integer. Received invalid value of : "
                    + request.getPreferences().getValue(NODE_LEVEL_PREFERENCE, null) + ". Using default value: "
                    + DEFAULT_NODE_LEVEL);
        }

        if (nodeLevel < 2) {
            nodeLevel = 2;
            log.warn("Preference for Node level must be greater than 1. Current value of "
                    + request.getPreferences().getValue(NODE_LEVEL_PREFERENCE, null) + " is invalid. Using default value of "
                    + DEFAULT_NODE_LEVEL);
        }
        return nodeLevel;
    }
}
