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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PageNodeContainer;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.tree.diff.Adapters;
import org.exoplatform.portal.tree.diff.ListAdapter;
import org.exoplatform.portal.tree.diff.ListChangeIterator;
import org.exoplatform.portal.tree.diff.ListChangeType;
import org.exoplatform.portal.tree.diff.ListDiff;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NavigationFragmentImporter
{

   private static final ListAdapter<PageNodeContainer, String> PAGE_NODE_CONTAINER_ADAPTER = new ListAdapter<PageNodeContainer, String>()
   {
      public int size(PageNodeContainer list)
      {
         List<PageNode> nodes = list.getNodes();
         if (nodes == null)
         {
            return 0;
         }
         else
         {
            return nodes.size();
         }
      }

      public Iterator<String> iterator(PageNodeContainer list, boolean reverse)
      {
         List<PageNode> nodes = list.getNodes();
         if (nodes == null)
         {
            return Collections.<String>emptyList().iterator();
         }
         else {
            String[] names = new String[nodes.size()];
            int index = 0;
            for (PageNode child : nodes)
            {
               names[index++] = child.getName();
            }
            return Adapters.<String>list().iterator(names, reverse);
         }
      }
   };

   private static final ListAdapter<NodeContext<?>, String> NODE_ADAPTER = new ListAdapter<NodeContext<?>, String>()
   {
      public int size(NodeContext<?> list)
      {
         return list.getNodeCount();
      }

      public Iterator<String> iterator(NodeContext<?> list, boolean reverse)
      {
         int size = list.getNodeCount();
         String[] names = new String[size];
         int index = 0;
         for (NodeContext<?> child = list.getFirst();child != null;child = child.getNext())
         {
            names[index++] = child.getName();
         }
         return Adapters.<String>list().iterator(names, reverse);
      }
   };

   /** . */
   private final String[] path;
   
   /** . */
   private final NavigationService navigationService;
   
   /** . */
   private final SiteKey navigationKey;

   /** . */
   private final Locale portalLocale;

   /** . */
   private final DescriptionService descriptionService;

   /** . */
   private final PageNodeContainer src;

   /** . */
   private final ImportConfig config;

   public NavigationFragmentImporter(
      String[] path,
      NavigationService navigationService,
      SiteKey navigationKey,
      Locale portalLocale,
      DescriptionService descriptionService,
      PageNodeContainer src,
      ImportConfig config)
   {
      this.path = path;
      this.navigationService = navigationService;
      this.navigationKey = navigationKey;
      this.portalLocale = portalLocale;
      this.descriptionService = descriptionService;
      this.src = src;
      this.config = config;
   }

   public ImportConfig getConfig()
   {
      return config;
   }

   public NodeContext<?> perform()
   {
      NavigationContext navigationCtx = navigationService.loadNavigation(navigationKey);

      //
      if (navigationCtx != null)
      {
         NodeContext root = navigationService.loadNode(NodeModel.SELF_MODEL, navigationCtx, GenericScope.branchShape(path), null);

         //
         NodeContext from = root;
         for (String name : path)
         {
            NodeContext a = from.get(name);
            if (a != null)
            {
               from = a;
            }
            else
            {
               from = from.add(null, name);
            }
         }

         // Collect labels
         Map<NodeContext<?>, Map<Locale, Described.State>> labelMap = new HashMap<NodeContext<?>, Map<Locale, Described.State>>();

         // Perform save
         perform(src, from, labelMap);

         // Save the node
         navigationService.saveNode(root, null);

         //
         for (Map.Entry<NodeContext<?>, Map<Locale, Described.State>> entry : labelMap.entrySet())
         {
            String id = entry.getKey().getId();
            descriptionService.setDescriptions(id, entry.getValue());
         }

         //
         return from;
      }
      else
      {
         return null;
      }
   }
   
   private void perform(PageNodeContainer src, final NodeContext<?> dst, final Map<NodeContext<?>, Map<Locale, Described.State>> labelMap)
   {
      navigationService.rebaseNode(dst, Scope.CHILDREN, null);

      //
      ListDiff<PageNodeContainer, NodeContext<?>, String> diff = new ListDiff<PageNodeContainer,NodeContext<?>, String>(
         PAGE_NODE_CONTAINER_ADAPTER,
         NODE_ADAPTER
         );

      //
      List<PageNode> srcChildren = src.getNodes();
      ListChangeIterator<PageNodeContainer, NodeContext<?>, String> it = diff.iterator(src, dst);

      class Change
      {
         final ListChangeType type;
         final String name;
         final int index1;
         final int index2;

         Change(ListChangeType type, String name, int index1, int index2)
         {
            this.type = type;
            this.name = name;
            this.index1 = index1;
            this.index2 = index2;
         }
      }

      // Buffer the changes in a list
      LinkedList<Change> foo = new LinkedList<Change>();
      while (it.hasNext())
      {
         ListChangeType type = it.next();
         foo.add(new Change(type, it.getElement(), it.getIndex1(), it.getIndex2()));
      }

      // The last encountered child
      NodeContext<?> previousChild = null;

      // Replay the changes and apply them
      for (Change change : foo)
      {
         PageNode srcChild = src.getNode(change.name);
         NodeContext<?> dstChild = dst.get(change.name);

         //
         switch (change.type)
         {
            case SAME:
               // Perform recursively
               perform(srcChild, dstChild, labelMap);

               //
               if (config.updatedSame)
               {
                  dstChild.setState(srcChild.getState());
               }

               //
               previousChild = dstChild;
               break;
            case REMOVE:
               if (dst.getNode(change.name) != null)
               {
               }
               else
               {
                  if (config.createMissing)
                  {
                     previousChild = add(srcChild, previousChild, dst, labelMap);
                  }
               }
               break;
            case ADD:
               if (src.getNode(change.name) != null)
               {
                  if (config.updatedSame)
                  {
                     dstChild.setState(srcChild.getState());
                  }
                  previousChild = dstChild;
               }
               else
               {
                  if (config.destroyOrphan)
                  {
                     dstChild.removeNode();
                  }
                  else
                  {
                     previousChild = dstChild;
                  }
               }
               break;
         }
      }
   }

   private NodeContext<?> add(
      PageNode target,
      NodeContext<?> previous,
      NodeContext<?> parent,
      Map<NodeContext<?>, Map<Locale, Described.State>> labelMap)
   {
      I18NString labels = target.getLabels();

      //
      Map<Locale, Described.State> description;
      if (labels.isSimple())
      {
         description = null;
      }
      else if (labels.isEmpty())
      {
         description = null;
      }
      else
      {
         description = new HashMap<Locale, Described.State>();
         for (Map.Entry<Locale, String> entry : labels.getExtended(portalLocale).entrySet())
         {
            description.put(entry.getKey(), new Described.State(entry.getValue(), null));
         }
      }

      //
      String name = target.getName();
      int index;
      if (previous != null)
      {
         index = parent.get((previous).getName()).getIndex() + 1;
      }
      else
      {
         index = 0;
      }
      NodeContext<?> child = parent.add(index, name);
      NodeState state = target.getState();
      child.setState(state);

      //
      if (description != null)
      {
         labelMap.put(child, description);
      }

      // We recurse to create the descendants
      List<PageNode> targetChildren = target.getNodes();
      if (targetChildren != null)
      {
         NodeContext<?> targetPrevious = null;
         for (PageNode targetChild : targetChildren)
         {
            targetPrevious = add(targetChild, targetPrevious, child, labelMap);
         }
      }

      //
      return child;
   }
}
