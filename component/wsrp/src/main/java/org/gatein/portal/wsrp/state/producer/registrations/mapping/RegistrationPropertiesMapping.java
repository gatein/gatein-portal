/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.producer.registrations.mapping;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;
import org.exoplatform.commons.utils.Tools;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationPropertiesMapping.NODE_NAME)
public abstract class RegistrationPropertiesMapping
{
   public static final String NODE_NAME = "wsrp:registrationproperties";

   /** Need to ignore JCR properties for now until scoping mechanism exists on @Properties… */
   private static final Set<String> propertiesBlackList = Tools.set("jcr:uuid", "jcr:primaryType");

   @Properties
   public abstract Map<String, String> getProperties();

   public Map<QName, Object> toPropMap()
   {
      Map<QName, Object> properties = Collections.emptyMap();
      Map<String, String> propMap = getProperties();
      if (!propMap.isEmpty())
      {
         properties = new HashMap<QName, Object>(propMap.size());
         for (Map.Entry<String, String> entry : propMap.entrySet())
         {
            String key = entry.getKey();
            // ignore JCR-specific properties
            if (!propertiesBlackList.contains(key))
            {
               properties.put(QName.valueOf(key), entry.getValue());
            }
         }
      }

      return properties;
   }

   public void initFrom(Map<QName, Object> properties)
   {
      if (properties != null)
      {
         Map<String, String> map = getProperties();

         for (Map.Entry<QName, Object> entry : properties.entrySet())
         {
            map.put(entry.getKey().toString(), entry.getValue().toString());
         }
      }
   }
}
