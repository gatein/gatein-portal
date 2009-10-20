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

package org.exoplatform.services.config;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;

import org.exoplatform.services.database.HibernateService;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Dec 5, 2004
 * @version $Id: ConfigurationServiceImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ConfigurationServiceImpl implements ConfigurationService
{

   private HibernateService hservice_;

   private XStream xstream_;

   public ConfigurationServiceImpl(HibernateService service)
   {
      hservice_ = service;
      xstream_ = new XStream(new XppDriver());
   }

   public Object getServiceConfiguration(Class serviceType) throws Exception
   {
      ConfigurationData impl = (ConfigurationData)hservice_.findOne(ConfigurationData.class, serviceType.getName());
      Object obj = null;
      if (impl == null)
      {
         obj = loadDefaultConfig(serviceType);
         saveServiceConfiguration(serviceType, obj);
      }
      else
      {
         obj = xstream_.fromXML(impl.getData());
      }
      return obj;
   }

   public void saveServiceConfiguration(Class serviceType, Object config) throws Exception
   {
      ConfigurationData configData =
         (ConfigurationData)hservice_.findOne(ConfigurationData.class, serviceType.getName());
      String xml = xstream_.toXML(config);
      if (configData == null)
      {
         configData = new ConfigurationData();
         configData.setServiceType(serviceType.getName());
         configData.setData(xml);
         hservice_.create(configData);
      }
      else
      {
         configData.setData(xml);
         hservice_.update(configData);
      }
   }

   public void removeServiceConfiguration(Class serviceType) throws Exception
   {
      hservice_.remove(serviceType, serviceType.getName());
   }

   @SuppressWarnings("unused")
   private Object loadDefaultConfig(Class serviceType) throws Exception
   {
      // ServiceConfiguration sconf =
      // manager_.getServiceConfiguration(serviceType) ;
      // Iterator i = sconf.values().iterator() ;
      // ObjectParam param = (ObjectParam) i.next() ;
      // return param.getObject() ;
      return null;
   }

}
