/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.portal.pom.data;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.portal.config.model.RedirectMappings;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectMappingsData extends ComponentData
{  
   
   protected boolean useNodeNameMatching = true;
   protected String unresolvedNode;
   protected Map<String, String> mappings;
   
   public RedirectMappingsData(String storageId)
   {
      super(storageId, null);
   }
   
   public RedirectMappingsData(String storageId, String storageName)
   {
      super(storageId, storageName);
   }

   public boolean isUseNodeNameMatching()
   {
      return useNodeNameMatching;
   }

   public void setUseNodeNameMatching(boolean useNodeNameMatching)
   {
      this.useNodeNameMatching = useNodeNameMatching;
   }

   public String getUnresolvedNode()
   {
      return unresolvedNode;
   }

   public void setUnresolvedNode(String unresolvedNode)
   {
      this.unresolvedNode = unresolvedNode;
   }

   public Map<String, String> getMappings()
   {
      if (mappings == null)
      {
         mappings = new HashMap<String, String>();
      }
      return mappings;
   }
   
   public RedirectMappings build()
   {
      RedirectMappings redirectMappings = new RedirectMappings();
      
      if (unresolvedNode != null)
      {
         redirectMappings.setUnresolvedNode(RedirectMappings.UnknownNodeMapping.valueOf(unresolvedNode));
      }
      
      redirectMappings.setUseNodeNameMatching(useNodeNameMatching);
      redirectMappings.setMap(mappings);
      return redirectMappings;
   }
}

