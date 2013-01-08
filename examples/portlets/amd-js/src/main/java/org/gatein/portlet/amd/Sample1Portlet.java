package org.gatein.portlet.amd;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;


/**
 * Show the use of AMD module in GateIn portal
 *
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class Sample1Portlet extends GenericPortlet {

    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/sample1/view.jsp");
        prd.include(request, response);
    }
}
