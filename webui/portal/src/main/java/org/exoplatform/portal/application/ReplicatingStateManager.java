/*
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

package org.exoplatform.portal.application;

import org.exoplatform.commons.utils.LazyList;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.webui.Util;
import org.exoplatform.webui.application.ConfigurationManager;
import org.exoplatform.webui.application.StateManager;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.application.replication.SerializationContext;
import org.exoplatform.webui.application.replication.api.annotations.Serialized;
import org.exoplatform.webui.application.replication.api.factory.CreateException;
import org.exoplatform.webui.application.replication.api.factory.ObjectFactory;
import org.exoplatform.webui.application.replication.model.FieldModel;
import org.exoplatform.webui.application.replication.model.TypeDomain;
import org.exoplatform.webui.application.replication.model.metadata.DomainMetaData;
import org.exoplatform.webui.application.replication.serial.ObjectWriter;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * The basis is either {@link org.exoplatform.webui.core.UIPortletApplication} or
 * {@link org.exoplatform.portal.webui.workspace.UIPortalApplication}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ReplicatingStateManager extends StateManager
{

   @Override
   public UIApplication restoreUIRootComponent(WebuiRequestContext context) throws Exception
   {
      context.setStateManager(this);

      //
      WebuiApplication app = (WebuiApplication)context.getApplication();

      //
      HttpSession session = getSession(context);
      String key = getKey(context);

      //
      UIApplication uiapp = (UIApplication)session.getAttribute("bilto_" + key);

      // Looks like some necessary hacking
      if (context instanceof PortalRequestContext)
      {
         PortalRequestContext portalRC = (PortalRequestContext)context;
         UserPortalConfig config = getUserPortalConfig(portalRC);
         if (config == null)
         {
            HttpServletResponse response = portalRC.getResponse();
            response.sendRedirect(portalRC.getRequest().getContextPath() + "/portal-unavailable.jsp");
            portalRC.setResponseComplete(true);
            return null;
         }
         portalRC.setAttribute(UserPortalConfig.class, config);
//         SessionManagerContainer pcontainer = (SessionManagerContainer)app.getApplicationServiceContainer();
//         pcontainer.createSessionContainer(context.getSessionId(), uiapp.getOwner());
      }

      //
      if (uiapp == null)
      {
         ConfigurationManager cmanager = app.getConfigurationManager();
         String uirootClass = cmanager.getApplication().getUIRootComponent();
         Class<? extends UIApplication> type = (Class<UIApplication>) Thread.currentThread().getContextClassLoader().loadClass(uirootClass);
         uiapp = app.createUIComponent(type, null, null, context);
      }

      //
      return uiapp;
   }

   @Override
   public void storeUIRootComponent(final WebuiRequestContext context) throws Exception
   {
      UIApplication uiapp = context.getUIApplication();

      //
      HttpSession session = getSession(context);

      //
      Class<? extends UIApplication> appClass = uiapp.getClass();
      if (appClass.getAnnotation(Serialized.class) != null)
      {
         try
         {
            DomainMetaData domainMetaData = new DomainMetaData();
/*
            domainMetaData.addClassType(PageList.class, true);
            domainMetaData.addClassType(LazyPageList.class, true);
            domainMetaData.addClassType(LazyList.class, true);
            domainMetaData.addClassType(LazyList.class.getName() + "$Batch", true);
*/

            //
            SerializationContext serializationContext = (SerializationContext)session.getAttribute("SerializationContext");
            if (serializationContext == null)
            {
               TypeDomain domain = new TypeDomain(domainMetaData, true);
               serializationContext = new SerializationContext(domain);
               session.setAttribute("SerializationContext", serializationContext);
               ObjectFactory<UIComponent> factory = new ObjectFactory<UIComponent>()
               {

                  private <S extends UIComponent> Object getFieldValue(String fieldName, Map<FieldModel<? super S, ?>, ?> state)
                  {
                     for (Map.Entry<FieldModel<? super S, ?>, ?> entry : state.entrySet())
                     {
                        FieldModel<? super S, ?> fieldModel = entry.getKey();
                        if (fieldModel.getOwner().getJavaType() == UIComponent.class && fieldModel.getName().equals(fieldName))
                        {
                           return entry.getValue();
                        }
                     }
                     return null;
                  }

                  @Override
                  public <S extends UIComponent> S create(Class<S> type, Map<FieldModel<? super S, ?>, ?> state) throws CreateException
                  {
                     // Get config id
                     String configId = (String)getFieldValue("configId", state);
                     String id = (String)getFieldValue("id", state);

                     //
                     try
                     {
                        WebuiApplication webuiApp = (WebuiApplication) context.getApplication();
                        ConfigurationManager configMgr = webuiApp.getConfigurationManager();
                        Component config = configMgr.getComponentConfig(type, configId);

                        //
                        S instance;
                        if (config != null)
                        {
                           instance = Util.createObject(type, config.getInitParams());
                           instance.setComponentConfig(id, config);
                        }
                        else
                        {
                           instance = Util.createObject(type, null);
                           instance.setId(id);
                        }

                        // Now set state
                        for (Map.Entry<FieldModel<? super S, ?>, ?> entry : state.entrySet())
                        {
                           FieldModel<? super S, ?> fieldModel = entry.getKey();
                           Object value = entry.getValue();
                           fieldModel.castAndSet(instance, value);
                        }

                        //
                        return instance;
                     }
                     catch (Exception e)
                     {
                        throw new CreateException(e);
                     }
                  }
               };
               serializationContext.addFactory(factory);
            }

            //

            //
            uiapp = serializationContext.clone(uiapp);
            System.out.println("Cloned application");
         }
         catch (Exception e)
         {
            System.out.println("Could not clone application");
            e.printStackTrace();
         }
      }


      //
      String key = getKey(context);
      session.setAttribute("bilto_" + key, uiapp);
   }

   @Override
   public void expire(String sessionId, WebuiApplication app) throws Exception
   {
      // For now do nothing....
   }

   private UserPortalConfig getUserPortalConfig(PortalRequestContext context) throws Exception
   {
      ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService service_ = (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      String remoteUser = context.getRemoteUser();
      String ownerUser = context.getPortalOwner();
      return service_.getUserPortalConfig(ownerUser, remoteUser);
   }

   private String getKey(WebuiRequestContext webuiRC)
   {
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;
         return portletRC.getApplication().getApplicationId() + "/" + portletRC.getWindowId();
      }
      else
      {
         PortalRequestContext portalRC = (PortalRequestContext)webuiRC;
         return "portal";
      }
   }

   private HttpSession getSession(WebuiRequestContext webuiRC)
   {
      if (webuiRC instanceof PortletRequestContext)
      {
         PortletRequestContext portletRC = (PortletRequestContext)webuiRC;
         PortalRequestContext portalRC = (PortalRequestContext) portletRC.getParentAppRequestContext();
         HttpServletRequest req = portalRC.getRequest();
         return req.getSession();
      }
      else
      {
         PortalRequestContext portalRC = (PortalRequestContext)webuiRC;
         HttpServletRequest req = portalRC.getRequest();
         return req.getSession();
      }
   }
}
