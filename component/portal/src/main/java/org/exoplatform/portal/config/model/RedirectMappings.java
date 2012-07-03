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
package org.exoplatform.portal.config.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.portal.pom.data.RedirectMappingsData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectMappings extends ModelObject
{

   protected boolean useNodeNameMatching = true;
   
   //TODO: allow for the default node mapping strategy to use to be specified in a configuration file somewhere.
   protected UnknownNodeMapping unresolvedNode = UnknownNodeMapping.NO_REDIRECT;
   protected ArrayList<NodeMap> mappings;
  
   public enum UnknownNodeMapping {
    REDIRECT, NO_REDIRECT, ROOT, COMMON_ANCESTOR_NAME_MATCH
   }
   
   @Override
   public RedirectMappingsData build()
   {
      RedirectMappingsData redirectMappingsData = new RedirectMappingsData(this.storageId, this.storageName);
     
      redirectMappingsData.setUnresolvedNode(unresolvedNode.name());
      redirectMappingsData.setUseNodeNameMatching(useNodeNameMatching);
      
      if (mappings != null)
      {
         redirectMappingsData.getMappings().putAll(getMap());
      }
      
      return redirectMappingsData;
   }

   public Map<String, String> getMap()
   {
      if (mappings != null)
      {
         Map<String, String> map = new HashMap<String, String>();
         for (NodeMap nodeMap: mappings)
         {
            map.put(nodeMap.getOriginNode(), nodeMap.getRedirectNode());
         }
         return map;
      }
      else
      {
         return null;
      }
   }
   
   public void setMap(Map<String, String> map)
   {
      for (String key: map.keySet())
      {
         getMappings().add(new NodeMap(key, map.get(key)));
      }
   }
   
   public boolean isUseNodeNameMatching()
   {
      return useNodeNameMatching;
   }

   public void setUseNodeNameMatching(boolean useNodeNameMatching)
   {
      this.useNodeNameMatching = useNodeNameMatching;
   }

   public UnknownNodeMapping getUnresolvedNode()
   {
      return unresolvedNode;
   }

   public void setUnresolvedNode(UnknownNodeMapping unresolvedNode)
   {
      this.unresolvedNode = unresolvedNode;
   }
   
   public ArrayList<NodeMap> getMappings()
   {
      if (mappings == null)
      {
         mappings = new ArrayList<NodeMap>();
      }
      return mappings;
   }

   public void setMappings(ArrayList<NodeMap> mappings)
   {
      this.mappings = mappings;
   }

   
}

