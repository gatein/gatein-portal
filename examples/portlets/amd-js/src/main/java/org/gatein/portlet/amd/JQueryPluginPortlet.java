package org.gatein.portlet.amd;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


/**
 * Show the use of a jQuery plugin with the built-in jQuery
 *
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class JQueryPluginPortlet extends GenericPortlet {

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jqueryPlugin/view.jsp");
        prd.include(request, response);
    }
}
