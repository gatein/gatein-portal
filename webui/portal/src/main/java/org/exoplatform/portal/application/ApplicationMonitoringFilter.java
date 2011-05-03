package org.exoplatform.portal.application;

import org.exoplatform.container.PortalContainer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;
import java.io.IOException;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 4/5/11
 */
public class ApplicationMonitoringFilter implements ActionFilter, RenderFilter, EventFilter, ResourceFilter
{

   public void init(FilterConfig cfg) throws PortletException
   {
   }

   public void destroy()
   {
   }

   public void doFilter(ActionRequest req, ActionResponse resp, FilterChain chain) throws IOException, PortletException
   {
      ApplicationStatistic stat = get(req);
      if (stat != null)
      {
         long t = -System.currentTimeMillis();
         chain.doFilter(req, resp);
         t += System.currentTimeMillis();
         stat.logTime(t);
      }
      else
      {
         chain.doFilter(req, resp);
      }
   }

   public void doFilter(EventRequest req, EventResponse resp, FilterChain chain) throws IOException, PortletException
   {
      ApplicationStatistic stat = get(req);
      if (stat != null)
      {
         long t = -System.currentTimeMillis();
         chain.doFilter(req, resp);
         t += System.currentTimeMillis();
         stat.logTime(t);
      }
      else
      {
         chain.doFilter(req, resp);
      }
   }

   public void doFilter(RenderRequest req, RenderResponse resp, FilterChain chain) throws IOException, PortletException
   {
      ApplicationStatistic stat = get(req);
      if (stat != null)
      {
         long t = -System.currentTimeMillis();
         chain.doFilter(req, resp);
         t += System.currentTimeMillis();
         stat.logTime(t);
      }
      else
      {
         chain.doFilter(req, resp);
      }
   }

   public void doFilter(ResourceRequest req, ResourceResponse resp, FilterChain chain) throws IOException, PortletException
   {
      ApplicationStatistic stat = get(req);
      if (stat != null)
      {
         long t = -System.currentTimeMillis();
         chain.doFilter(req, resp);
         t += System.currentTimeMillis();
         stat.logTime(t);
      }
      else
      {
         chain.doFilter(req, resp);
      }
   }

   private ApplicationStatistic get(PortletRequest req) throws IOException, PortletException
   {
      PortalContainer container = PortalContainer.getInstance();
      ApplicationStatisticService service = (ApplicationStatisticService)container.getComponentInstance(ApplicationStatisticService.class);
      if (service != null)
      {
         PortletConfig portletConfig = (PortletConfig)req.getAttribute("javax.portlet.config");
         String portletName = portletConfig.getPortletName();
         String phase = (String)req.getAttribute(PortletRequest.LIFECYCLE_PHASE);
         String applicationId = portletName + "/" + phase;
         return service.getApplicationStatistic(applicationId);
      }
      else
      {
         return null;
      }
   }
}
