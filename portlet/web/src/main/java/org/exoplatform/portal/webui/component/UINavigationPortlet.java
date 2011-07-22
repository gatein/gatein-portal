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

package org.exoplatform.portal.webui.component;

import java.util.Collection;

import javax.portlet.MimeResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.navigation.UIPortalNavigation;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.json.JSONArray;
import org.json.JSONObject;

@ComponentConfigs({
   @ComponentConfig(lifecycle = UIApplicationLifecycle.class),
   @ComponentConfig(type = UIPortalNavigation.class, id = "UIHorizontalNavigation", events = @EventConfig(listeners = UIPortalNavigation.SelectNodeActionListener.class))})
public class UINavigationPortlet extends UIPortletApplication
{
   public static final int DEFAULT_LEVEL = 2;
   
   public UINavigationPortlet() throws Exception
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();
      String template = prefers.getValue("template", "app:/groovy/portal/webui/component/UIPortalNavigation.gtmpl");

      UIPortalNavigation portalNavigation = addChild(UIPortalNavigation.class, "UIHorizontalNavigation", null);
      portalNavigation.setUseAjax(isUseAjax());
      portalNavigation.setShowUserNavigation(Boolean.valueOf(prefers.getValue("showUserNavigation", "true")));
      portalNavigation.setTemplate(template);

      portalNavigation.setCssClassName(prefers.getValue("CSSClassName", ""));
      
      int level = DEFAULT_LEVEL; 
      try 
      {
         level = Integer.valueOf(prefers.getValue("level", String.valueOf(DEFAULT_LEVEL)));       
      }
      catch (Exception ex) 
      {
         log.warn("Preference for navigation level can only be integer");
      }

      if (level <= 0)
      {
         portalNavigation.setScope(Scope.ALL);                     
      }
      else
      {
         portalNavigation.setScope(GenericScope.treeShape(level));
      }
   }

   @Override
   public void serveResource(WebuiRequestContext context) throws Exception
   {
      super.serveResource(context);
      
      ResourceRequest req = context.getRequest();
      String nodeURI = req.getResourceID();
            
      JSONArray jsChilds = getChildrenAsJSON(nodeURI);
      if (jsChilds == null)
      {
         return;
      }
      
      MimeResponse res = context.getResponse(); 
      res.setContentType("text/json");
      res.getWriter().write(jsChilds.toString());
   }      
   

   public JSONArray getChildrenAsJSON(String nodeURI) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();          
      UIPortalNavigation uiPortalNavigation = getChild(UIPortalNavigation.class);
      
      Collection<UserNode> childs = null;      
      UserNode userNode = uiPortalNavigation.resolvePath(nodeURI);
      if (userNode != null)
      {
         childs = userNode.getChildren();
      }
      
      JSONArray jsChilds = new JSONArray();
      if (childs == null)
      {
         return null;
      }                  
      MimeResponse res = context.getResponse();
      for (UserNode child : childs)
      {
         jsChilds.put(toJSON(child, res));
      }
      return jsChilds;
   }

   private JSONObject toJSON(UserNode node, MimeResponse res) throws Exception
   {
      JSONObject json = new JSONObject();
      String nodeId = node.getId();
      
      json.put("label", node.getEncodedResolvedLabel());      
      json.put("hasChild", node.getChildrenCount() > 0);            
      
      UserNode selectedNode = Util.getUIPortal().getNavPath();
      json.put("isSelected", nodeId.equals(selectedNode.getId()));
      json.put("icon", node.getIcon());      
      
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(res.encodeURL(node.getURI()));
      json.put("getNodeURL", rsURL.toString());            
      
      String actionLink;
      if (node.getPageRef() == null)
      {
         actionLink = null;
      } 
      else if (isUseAjax())
      {
         actionLink = event("SelectNode", nodeId);
      } 
      else
      {
         actionLink = Util.getPortalRequestContext().getPortalURI() + node.getURI();
      }
      json.put("actionLink", actionLink);
      
      JSONArray childs = new JSONArray();
      for (UserNode child : node.getChildren())
      {
         childs.put(toJSON(child, res));
      }      
      json.put("childs", childs);
      return json;
   }
   
   public boolean isUseAjax()
   {
      PortletRequestContext context = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
      PortletRequest prequest = context.getRequest();
      PortletPreferences prefers = prequest.getPreferences();
      return Boolean.valueOf(prefers.getValue("useAJAX", "true"));
   }
}