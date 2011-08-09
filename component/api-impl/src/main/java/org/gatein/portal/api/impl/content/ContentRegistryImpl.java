/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.portal.api.impl.content;

import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.gatein.api.content.Category;
import org.gatein.api.content.Content;
import org.gatein.api.content.ContentRegistry;
import org.gatein.api.content.Gadget;
import org.gatein.api.content.Portlet;
import org.gatein.api.content.WSRP;
import org.gatein.api.id.Id;
import org.gatein.api.util.IterableCollection;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.portal.PortalImpl;
import org.gatein.portal.api.impl.util.AdaptedIterableCollection;
import org.gatein.portal.api.impl.util.AdaptedIterableIdentifiableCollection;
import org.gatein.portal.api.impl.util.AggregatedIterableIdentifiableCollection;

import java.util.List;
import java.util.Set;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ContentRegistryImpl implements ContentRegistry
{
   private final GateInImpl gateIn;
   private final PortalImpl portal;

   public ContentRegistryImpl(GateInImpl gateIn, PortalImpl portal)
   {
      this.gateIn = gateIn;
      this.portal = portal;
   }

   public Category getOrCreateCategory(String name)
   {
      return getOrCreateCategory(name, true);
   }

   public Category getCategory(String name)
   {
      return getOrCreateCategory(name, false);
   }

   public void deleteCategory(String name)
   {
      try
      {
         final ApplicationCategory category = new ApplicationCategory();
         category.setName(name);
         gateIn.begin();
         gateIn.getRegistryService().remove(category);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableCollection<String> getCategoryNames()
   {
      try
      {
         gateIn.begin();
         final List<ApplicationCategory> categories = gateIn.getRegistryService().getApplicationCategories();
         return new AdaptedIterableCollection<ApplicationCategory, String>(categories.size(), categories.iterator())
         {
            public String adapt(ApplicationCategory old)
            {
               return old.getName();
            }

            public boolean contains(String s)
            {
               return doesCategoryExist(s);
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableIdentifiableCollection<Category> getAllCategories()
   {
      try
      {
         gateIn.begin();
         final List<ApplicationCategory> categories = gateIn.getRegistryService().getApplicationCategories();
         return new AdaptedIterableIdentifiableCollection<ApplicationCategory, Category>(categories.size(), categories.iterator())
         {
            public Category adapt(ApplicationCategory old)
            {
               return new CategoryImpl(old, ContentRegistryImpl.this, gateIn);
            }

            public boolean contains(Id<Category> t)
            {
               return doesCategoryExist(t.toString());
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   private Category getOrCreateCategory(String name, boolean forceCreate)
   {
      try
      {
         gateIn.begin();
         ApplicationRegistryService registryService = gateIn.getRegistryService();
         ApplicationCategory applicationCategory = registryService.getApplicationCategory(name);
         if (applicationCategory == null)
         {
            if (forceCreate)
            {
               applicationCategory = new ApplicationCategory();
               applicationCategory.setName(name);
               applicationCategory.setDisplayName(name);
               applicationCategory.setDescription(name);
               registryService.save(applicationCategory);
            }
            else
            {
               return null;
            }
         }

         return new CategoryImpl(applicationCategory, this, gateIn);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   private boolean doesCategoryExist(String name)
   {
      try
      {
         gateIn.begin();
         return gateIn.getRegistryService().getApplicationCategory(name) != null;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public <T extends Content> T get(Id<T> id)
   {
      Class<T> type = id.getIdentifiableType();
      Object result;
      Object regitryItem;
      try
      {
         gateIn.begin();
         if (Portlet.class.equals(type) || WSRP.class.equals(type))
         {
            regitryItem = getPortletFrom(id);
         }
         else if (Gadget.class.equals(type))
         {
            regitryItem = gateIn.getGadgetService().getGadget(id.toString());
         }
         else
         {
            throw new IllegalArgumentException("Unknown Content type: " + type.getCanonicalName());
         }

         result = newContentFrom(id, regitryItem, type);
         return type.cast(result);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   private <T extends Content> Object getPortletFrom(Id<T> id) throws PortletInvokerException
   {
      PortletContext portletContext;
      Object regitryItem;
      if (!id.knowsComponent(GateInImpl.INVOKER_COMPONENT))
      {
         portletContext = PortletContext.createPortletContext(id.getComponent(GateInImpl.APPLICATION_COMPONENT), id.getComponent(GateInImpl.PORTLET_COMPONENT));
         regitryItem = gateIn.getPortletInvoker().getFederatedInvoker("local").getPortletInvoker().getPortlet(portletContext);
      }
      else
      {
         portletContext = PortletContext.createPortletContext(id.toString());
         regitryItem = gateIn.getPortletInvoker().getPortlet(portletContext);
      }
      return regitryItem;
   }

   private <T extends Content> Content newContentFrom(Id id, Object registryItem, Class<T> wanted)
   {
      Object result;
      if (Portlet.class.equals(wanted))
      {
         result = new PortletImpl((Id<Portlet>)id, (org.gatein.pc.api.Portlet)registryItem, gateIn);
      }
      else if (Gadget.class.equals(wanted))
      {
         result = new GadgetImpl((Id<Gadget>)id, (org.exoplatform.application.gadget.Gadget)registryItem, gateIn);
      }
      else if (WSRP.class.equals(wanted))
      {
         result = new WSRPImpl((Id<WSRP>)id, (org.gatein.pc.api.Portlet)registryItem, gateIn);
      }
      else
      {
         throw new IllegalArgumentException("Unknown Content type: " + wanted.getCanonicalName());
      }

      return wanted.cast(result);
   }

   public Gadget createGadget(String gadget, String source)
   {
      try
      {
         gateIn.begin();
         final GadgetRegistryService gadgetService = gateIn.getGadgetService();
         org.exoplatform.application.gadget.Gadget original = gadgetService.getGadget(gadget);
         if (original == null)
         {
            original = new org.exoplatform.application.gadget.Gadget();
            original.setName(gadget);
            original.setUrl("http://www.gatein.org"); // todo: fix me
            gadgetService.saveGadget(original);
         }

         SourceStorage sourceStorage = gateIn.getSourceStorage();
         Source originalSource = sourceStorage.getSource(original);
         originalSource.setTextContent(source);
         sourceStorage.saveSource(original, originalSource);

         return (Gadget)newContentFrom(gateIn.gadgetId(gadget), original, Gadget.class);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableIdentifiableCollection<Content> getAll()
   {
      try
      {
         gateIn.begin();
         IterableIdentifiableCollection<? extends Content> localPortlets = getLocalPortlets();
         IterableIdentifiableCollection<? extends Content> gadgets = getGadgets();
         IterableIdentifiableCollection<? extends Content> remotePortlets = getRemotePortlets();

         final AggregatedIterableIdentifiableCollection result = new AggregatedIterableIdentifiableCollection();
         result.addCollection(localPortlets);
         result.addCollection(remotePortlets);
         result.addCollection(gadgets);
         return result;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableIdentifiableCollection<Portlet> getLocalPortlets()
   {
      try
      {
         gateIn.begin();
         final FederatingPortletInvoker portletInvoker = gateIn.getPortletInvoker();
         final Set<org.gatein.pc.api.Portlet> initialLocalPortlets = portletInvoker.getLocalPortlets();
         return new AdaptedIterableIdentifiableCollection<org.gatein.pc.api.Portlet, Portlet>(initialLocalPortlets.size(), initialLocalPortlets.iterator())
         {
            public Portlet adapt(org.gatein.pc.api.Portlet old)
            {
               return (Portlet)newContentFrom(GateInImpl.parsePortletId(old.getContext().getId()), old, Portlet.class);
            }

            public boolean contains(Id<Portlet> t)
            {
               try
               {
                  return getPortletFrom(t) != null;
               }
               catch (PortletInvokerException e)
               {
                  return false;
               }
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableIdentifiableCollection<WSRP> getRemotePortlets()
   {
      try
      {
         gateIn.begin();
         final FederatingPortletInvoker portletInvoker = gateIn.getPortletInvoker();
         final Set<org.gatein.pc.api.Portlet> initialRemotePortlets = portletInvoker.getRemotePortlets();
         return new AdaptedIterableIdentifiableCollection<org.gatein.pc.api.Portlet, WSRP>(initialRemotePortlets.size(), initialRemotePortlets.iterator())
         {
            public WSRP adapt(org.gatein.pc.api.Portlet old)
            {
               return (WSRP)newContentFrom(GateInImpl.parseWSRPPortletId(old.getContext().getId()), old, WSRP.class);
            }

            public boolean contains(Id<WSRP> id)
            {
               try
               {
                  return getPortletFrom(id) != null;
               }
               catch (PortletInvokerException e)
               {
                  return false;
               }
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public IterableIdentifiableCollection<Gadget> getGadgets()
   {
      try
      {
         gateIn.begin();
         final List<org.exoplatform.application.gadget.Gadget> initialGadgets = gateIn.getGadgetService().getAllGadgets();
         return new AdaptedIterableIdentifiableCollection<org.exoplatform.application.gadget.Gadget, Gadget>(initialGadgets.size(), initialGadgets.iterator())
         {
            public Gadget adapt(org.exoplatform.application.gadget.Gadget old)
            {
               return (Gadget)newContentFrom(gateIn.gadgetId(old.getName()), old, Gadget.class);
            }

            public boolean contains(Id<Gadget> id)
            {
               try
               {
                  return gateIn.getGadgetService().getGadget(id.toString()) != null;
               }
               catch (Exception e)
               {
                  return false;
               }
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public int size()
   {
      // todo: optimize
      return getAll().size();
   }

   public <U extends Content> boolean contains(Id<U> id)
   {
      // todo: optimize
      return get(id) != null;
   }
}
