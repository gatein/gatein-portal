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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.ui.UIWindow;

import java.util.ArrayList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PortletPreferencesTask
{

   /*

   WindowID:
   - persistenceId : portal#classic:/web/BannerPortlet/testPortletPreferences
   - owner : portal#classic
   - portletApplicationName : web
   - portletName: BannerPortlet
   - uniqueID : testPortletPreferences

   */

   /** . */
   protected final ObjectType<? extends Site> siteType;

   /** . */
   protected final String ownerType;

   /** . */
   protected final String ownerId;

   /** . */
   protected final String applicationName;

   /** . */
   protected final String portletName;

   /** . */
   protected final String instanceName;

   /** . */
   protected final String windowId;

   protected PortletPreferencesTask(String windowId)
   {
      String[] chunks = Mapper.parseWindowId(windowId);
      if (chunks.length < 4 || chunks.length > 5)
      {
         throw new IllegalArgumentException("Invalid window id " + windowId);
      }

      //
      this.ownerType = chunks[0];
      this.siteType = Mapper.parseSiteType(chunks[0]);
      this.ownerId = chunks[1];
      this.applicationName = chunks[2];
      this.portletName = chunks[3];
      this.instanceName = chunks.length > 4 ? chunks[4] : null;
      this.windowId = windowId;
   }

   public static class Save extends PortletPreferencesTask implements POMTask<Void>
   {

      /** . */
      private final PortletPreferences prefs;

      public Save(PortletPreferences prefs)
      {
         super(prefs.getWindowId());

         //
         this.prefs = prefs;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public Void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);

         //
         Customization customization = null;
         if (site != null)
         {
            if (instanceName.startsWith("@"))
            {
               String id = instanceName.substring(1);
               UIWindow window = session.findObjectById(ObjectType.WINDOW, id);

               // Should check it's pointing to same instance though
               customization = window.getCustomization();
            }
            else
            {
               int pos = instanceName.indexOf("#");
               if (pos != -1)
               {
                  String a = instanceName.substring(0, pos);
                  String b = instanceName.substring(pos + 1);
                  Page page = site.getRootPage().getChild("pages").getChild(b);
                  Customization c = page.getCustomizationContext().getCustomization(a);
                  if (c != null)
                  {
                     c.destroy();
                  }
                  customization =
                     page.getCustomizationContext().customize(a, Portlet.CONTENT_TYPE, applicationName + "/" + portletName,
                        new PortletBuilder().build());
               }
               else
               {
                  Customization c = site.getCustomizationContext().getCustomization(instanceName);
                  if (c != null)
                  {
                     c.destroy();
                  }
                  customization =
                     site.getCustomizationContext().customize(instanceName, Portlet.CONTENT_TYPE, applicationName + "/" + portletName,
                        new PortletBuilder().build());
               }
            }
         }

         //
         if (customization != null)
         {
            PortletBuilder builder = new PortletBuilder();
            ArrayList<Preference> list = prefs.getPreferences();
            if (list != null)
            {
               for (Preference pref : list)
               {
                  builder.add(pref.getName(), pref.getValues(), pref.isReadOnly());
               }
            }
            customization.setState(builder.build());
         }
         else
         {
            session.addPortletPreferences(prefs);
         }

         //
         return null;
      }
   }

   public static class Load extends PortletPreferencesTask implements POMTask<PortletPreferences>
   {

      public Load(String windowId)
      {
         super(windowId);
      }

      public Class<PortletPreferences> getValueType()
      {
         return PortletPreferences.class;
      }

      public PortletPreferences run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot load portlet preferences " + windowId
               + " as the corresponding portal " + ownerId + " with type " + siteType + " does not exist");
         }

         //
         if (instanceName != null)
         {
            Customization<Portlet> customization;
            if (instanceName.startsWith("@"))
            {
               String id = instanceName.substring(1);
               UIWindow window = session.findObjectById(ObjectType.WINDOW, id);
               customization = (Customization<Portlet>)window.getCustomization();
            }
            else
            {
               int pos = instanceName.indexOf('#');
               if (pos == -1)
               {
                  customization = (Customization<Portlet>)site.getCustomizationContext().getCustomization(instanceName);
               }
               else
               {
                  String a = instanceName.substring(0, pos);
                  String b = instanceName.substring(pos + 1);
                  Page page = site.getRootPage().getChild("pages").getChild(b);
                  customization = (Customization<Portlet>)page.getCustomizationContext().getCustomization(a);
               }
            }

            //
            if (customization != null)
            {
               Portlet state = customization.getVirtualState();
               if (state != null)
               {
                  ArrayList<Preference> list = new ArrayList<Preference>();
                  for (org.exoplatform.portal.pom.spi.portlet.Preference preference : state)
                  {
                     Preference pref = new Preference();
                     pref.setName(preference.getName());
                     pref.setValues(new ArrayList<String>(preference.getValues()));
                     pref.setReadOnly(preference.isReadOnly());
                     list.add(pref);
                  }
                  PortletPreferences prefs = new PortletPreferences();
                  prefs.setWindowId(windowId);
                  prefs.setPreferences(list);
                  return prefs;
               }
            }
         }

         //
         return null;
      }
   }
}
