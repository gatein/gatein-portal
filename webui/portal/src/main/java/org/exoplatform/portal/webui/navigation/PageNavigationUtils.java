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

package org.exoplatform.portal.webui.navigation;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.webui.application.WebuiRequestContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jun 27, 2007  
 */
public class PageNavigationUtils
{

   public static void removeNode(List<PageNode> list, String uri)
   {
      if (list == null)
         return;
      for (PageNode pageNode : list)
      {
         if (pageNode.getUri().equalsIgnoreCase(uri))
         {
            list.remove(pageNode);
            return;
         }
      }
   }

   /**
    *  This method returns a pair of PageNode, one is the PageNode specified by the uri, 
    * another is its parent. Value return is 2-element array
    * 
    * 1. The element indexed 1 is the page node specified by the uri
    * 
    * 2. The element indexed 0 is its parent
    * 
    * @deprecated Returning 2-element array would makes it difficult to understand, handle the code. 
    * Method searchParentChildPairByUri should be used instead.
    * 
    * @param node
    * @param uri
    * @return
    */
   @Deprecated
   public static PageNode[] searchPageNodesByUri(PageNode node, String uri)
   {
      if (node.getUri().equals(uri))
         return new PageNode[]{null, node};
      if (node.getChildren() == null)
         return null;
      List<PageNode> children = node.getChildren();
      for (PageNode ele : children)
      {
         PageNode[] returnNodes = searchPageNodesByUri(ele, uri);
         if (returnNodes != null)
         {
            if (returnNodes[0] == null)
               returnNodes[0] = node;
            return returnNodes;
         }
      }
      return null;
   }

   @Deprecated
   public static PageNode[] searchPageNodesByUri(PageNavigation nav, String uri)
   {
      if (nav.getNodes() == null)
         return null;
      List<PageNode> nodes = nav.getNodes();
      for (PageNode ele : nodes)
      {
         PageNode[] returnNodes = searchPageNodesByUri(ele, uri);
         if (returnNodes != null)
            return returnNodes;
      }
      return null;
   }
   
   /**
    * This method returns a pair of a node matching the parsed uri and the parent of this node.
    * 
    * @param nav
    * @param uri
    * @return
    */
   public static ParentChildPair searchParentChildPairByUri(PageNavigation nav, String uri)
   {
      List<PageNode> nodes = nav.getNodes();
      
      if(nodes == null)
      {
         return null;
      }
      
      for(PageNode ele : nodes)
      {
         ParentChildPair parentChildPair = searchParentChildPairUnderNode(ele, uri);
         if(parentChildPair != null)
         {
            return parentChildPair;
         }
      }
      
      return null;
   }
   
   //TODO: Split the uri and use optimzed method <code>searchParentChildPairByPath</code>
   public static ParentChildPair searchParentChildPairUnderNode(PageNode rootNode, String uri)
   {
      if(uri.equals(rootNode.getUri()))
      {
         return new ParentChildPair(null, rootNode);
      }
      
      List<PageNode> nodes = rootNode.getNodes();
      if(nodes == null)
      {
         return null;
      }
      
      for(PageNode node : nodes)
      {
         ParentChildPair parentChildPair = searchParentChildPairUnderNode(node, uri);
         if(parentChildPair != null)
         {
            if(parentChildPair.getParentNode() == null)
            {
               parentChildPair.setParentNode(rootNode);
            }
            return parentChildPair;
         }
      }
      
      return null;
   }
   
   /**
    * Search a pair of page node (specified by the path) and its parent
    * 
    * @param rootNode
    * @param path
    * @return
    */
   public static ParentChildPair searchParentChildPairByPath(PageNode rootNode, String[] path)
   {
      if(path.length == 0)
      {
         throw new IllegalArgumentException("The input path must have unzero length");
      }
      
      if(!rootNode.getName().equals(path[0]))
      {
         return null;
      }
      else
      {
         if(path.length == 1)
         {
            return new ParentChildPair(null, rootNode);
         }
         
         PageNode tempNode = rootNode;
         
         PageNode parentNode = null;
         PageNode childNode = null;
         for(int i = 1; i< path.length; i++)
         {
            childNode = tempNode.getChild(path[i]);
            if(childNode == null)
            {
               return null;
            }
            else
            {
               parentNode = tempNode;
            }
         }
         
         return new ParentChildPair(parentNode, childNode);
      }
   }
   
   public static PageNode searchPageNodeByUri(PageNode node, String uri)
   {
      if (node.getUri().equals(uri))
         return node;
      if (node.getChildren() == null)
         return null;
      List<PageNode> children = node.getChildren();
      for (PageNode ele : children)
      {
         PageNode returnNode = searchPageNodeByUri(ele, uri);
         if (returnNode != null)
            return returnNode;
      }
      return null;
   }

   public static PageNode searchPageNodeByUri(PageNavigation nav, String uri)
   {
      if (nav.getNodes() == null)
         return null;
      List<PageNode> nodes = nav.getNodes();
      for (PageNode ele : nodes)
      {
         PageNode returnNode = searchPageNodeByUri(ele, uri);
         if (returnNode != null)
            return returnNode;
      }
      return null;
   }

   public static Object searchParentNode(PageNavigation nav, String uri)
   {
      if (nav.getNodes() == null)
         return null;
      int last = uri.lastIndexOf("/");
      String parentUri = "";
      if (last > -1)
         parentUri = uri.substring(0, uri.lastIndexOf("/"));
      for (PageNode ele : nav.getNodes())
      {
         if (ele.getUri().equals(uri))
            return nav;
      }
      if (parentUri.equals(""))
         return null;
      return searchPageNodeByUri(nav, parentUri);
   }

   // Still keep this method to have compatibility with legacy code
   public static PageNavigation filter(PageNavigation nav, String userName) throws Exception
   {
      return filterNavigation(nav, userName, false, false);
   }

   /**
    * 
    * @param nav
    * @param userName
    * @param acceptNonDisplayedNode
    * @param acceptNodeWithoutPage
    * @return
    * @throws Exception
    */
   public static PageNavigation filterNavigation(PageNavigation nav, String userName, boolean acceptNonDisplayedNode, boolean acceptNodeWithoutPage) throws Exception
   {
      PageNavigation filter = nav.clone();
      filter.setNodes(new ArrayList<PageNode>());
      
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService userService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      UserACL userACL = (UserACL)container.getComponentInstanceOfType(UserACL.class);

      for (PageNode node : nav.getNodes())
      {
         PageNode newNode = filterNodeNavigation(node, userName, acceptNonDisplayedNode, acceptNodeWithoutPage, userService, userACL);
         if (newNode != null)
            filter.addNode(newNode);
      }
      return filter;
   }
   
   /**
    * use {@link #filterNavigation(PageNavigation, String, boolean, boolean)}
    * 
    * @param nav
    * @param userName
    * @param acceptNonDisplayedNode
    * @return
    * @throws Exception
    */
   @Deprecated
   public static PageNavigation filterNavigation(PageNavigation nav, String userName, boolean acceptNonDisplayedNode) throws Exception
   {
	   return filterNavigation(nav, userName, acceptNonDisplayedNode, true);
   }
   
   /**
    * Use {@link #filterNodeNavigation(PageNode, String, boolean, boolean, UserPortalConfigService, UserACL)}
    * @param startNode
    * @param userName
    * @param acceptNonDisplayedNode
    * @param userService
    * @param userACL
    * @return
    * @throws Exception
    */
   @Deprecated
   private static PageNode filterNodeNavigation(PageNode startNode, String userName, boolean acceptNonDisplayedNode,
		      UserPortalConfigService userService, UserACL userACL) throws Exception {
	   PageNode cloneStartNode = filterNodeNavigation(startNode, userName, acceptNonDisplayedNode, false, userService, userACL);
	   return cloneStartNode;
   }

   /**
    * PageNode won't be processed in following cases:
    * 
    * Case 1: Node 's visibility is SYSTEM and the user is not superuser or he is superuser but acceptNonDisplayNode = false
    * 
    * Case 2: Node 's visibility is not SYSTEM but the node is not display and the acceptNonDisplayedNode = false
    * 
    * Case 3: Node has non null pageReference but the associated Page does not exist and not accept this node is without page
    * 
    * 
    * @param startNode
    * @param userName
    * @param acceptNonDisplayedNode
    * @param acceptNodeWithoutPage
    * @param userService
    * @param userACL
    * @return
    * @throws Exception
    */
   private static PageNode filterNodeNavigation(PageNode startNode, String userName, boolean acceptNonDisplayedNode, boolean acceptNodeWithoutPage,
      UserPortalConfigService userService, UserACL userACL) throws Exception
   {
    
      Visibility nodeVisibility = startNode.getVisibility();
      String pageReference = startNode.getPageReference();

      boolean doNothingCase_1 = nodeVisibility == Visibility.SYSTEM && (!userACL.getSuperUser().equals(userName) || !acceptNonDisplayedNode);
      boolean doNothingCase_2 = nodeVisibility != Visibility.SYSTEM && !startNode.isDisplay() && !acceptNonDisplayedNode;
      boolean doNothingCase_3 = (pageReference != null) && (userService.getPage(pageReference, userName) == null) && !acceptNodeWithoutPage;

      
      
      if (doNothingCase_1 || doNothingCase_2 || doNothingCase_3)
      {
         return null;
      }

      PageNode cloneStartNode = startNode.clone();

      // Check if page reference isn't existing, page reference value of node is setted null too.
      if (pageReference != null && userService.getPage(pageReference) == null)
      {
         cloneStartNode.setPageReference(null);         
      }
      ArrayList<PageNode> filteredChildren = new ArrayList<PageNode>();

      List<PageNode> children = startNode.getChildren();

      if (children != null)
      {
         for (PageNode child : children)
         {
            PageNode filteredChildNode = filterNodeNavigation(child, userName, acceptNonDisplayedNode, acceptNodeWithoutPage, userService, userACL);
            if (filteredChildNode != null)
            {
               filteredChildren.add(filteredChildNode);
            }
         }
      }

      //If are only accepting displayed nodes and If the node has no child and it does not point to any Page, then null is return
      if (!acceptNonDisplayedNode && filteredChildren.size() == 0 && cloneStartNode.getPageReference() == null)
      {
         return null;
      }
      cloneStartNode.setChildren(filteredChildren);
      return cloneStartNode;
   }

   public static PageNode filter(PageNode node, String userName, boolean acceptNonDisplayedNode) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService userService =
         (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      UserACL userACL = (UserACL)container.getComponentInstanceOfType(UserACL.class);

      return filterNodeNavigation(node, userName, acceptNonDisplayedNode, userService, userACL);
   }

   public static void localizePageNavigation(PageNavigation nav, Locale locale, ResourceBundleManager i18nManager)
   {
      if (nav.getOwnerType().equals(PortalConfig.USER_TYPE))
         return;
      String localeLanguage = (locale.getCountry().length() > 0) ? locale.getLanguage() + "_" + locale.getCountry() : locale.getLanguage();      
      ResourceBundle res =
         i18nManager.getNavigationResourceBundle(localeLanguage, nav.getOwnerType(), nav.getOwnerId());
      for (PageNode node : nav.getNodes())
      {
         resolveLabel(res, node);
      }
   }

   private static void resolveLabel(ResourceBundle res, PageNode node)
   {
      node.setResolvedLabel(res);
      if (node.getChildren() == null)
         return;
      for (PageNode childNode : node.getChildren())
      {
         resolveLabel(res, childNode);
      }
   }

   public static PageNavigation findNavigationByID(List<PageNavigation> all_Navigations, int id)
   {
      for (PageNavigation nav : all_Navigations)
      {
         if (nav.getId() == id)
         {
            return nav;
         }
      }
      return null;
   }
   
   public static void sortPageNavigation(List<PageNavigation> navigations)
   {
      Collections.sort(navigations, new PageNavigationComparator());
   }
   
   /**
    * 
    * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
    * @version $Id$
    *
    */
   public static class PageNavigationComparator implements Comparator<PageNavigation>
   {
      public int compare(PageNavigation firstNav, PageNavigation secondNav)
      {
         int firstNavPriority = firstNav.getPriority();
         int secondNavPriority = secondNav.getPriority();

         if (firstNavPriority == secondNavPriority)
         {
            String firstNavId = firstNav.getOwnerId();
            String secondNavId = secondNav.getOwnerId();
            return firstNavId.compareTo(secondNavId);
         }
         else
         {
            if (firstNavPriority < secondNavPriority)
            {
               return -1;
            }
            else
            {
               return 1;
            }
         }
      }
   }   
}
