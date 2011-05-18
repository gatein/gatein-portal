package org.exoplatform.services.organization.idm;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.services.organization.Query;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.CacheStatus;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.eviction.ExpirationAlgorithmConfig;
import org.jboss.cache.eviction.ExpirationConfiguration;

import org.picketlink.idm.api.Group;
import org.picketlink.idm.api.User;

import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
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

/**
 * Provides cache for some data used in integration layer between PicketLink IDM and GateIn
 */
public class IntegrationCache
{
   private static Logger log = Logger.getLogger(IntegrationCache.class.getName());

   private Cache cache;

   public static final String NODE_GTN_GROUP_ID = "NODE_GTN_GROUP_ID";

   public static final String NODE_PLIDM_ROOT_GROUP = "NODE_PLIDM_ROOT_GROUP";

   public static final String NULL_NS_NODE = "GTN_IC_COMMON_NS";

   public static final String USER_QUERY_NODE = "GTN_USER_QUERY_LAZY_LIST";

   public static final String MAIN_ROOT = "NODE_GTN_ORG_SERVICE_INT_CACHE_MAIN_ROOT";

   public static final String NODE_OBJECT_KEY = "object";

   private int expiration = -1;

   private Fqn getRootNode()
   {
      return Fqn.fromString("/" + MAIN_ROOT);
   }

   private Fqn getNamespacedFqn(String ns)
   {
      String namespace = ns != null ? ns : NULL_NS_NODE;
      namespace = namespace.replaceAll("/", "_");
      return Fqn.fromString(getRootNode() + "/" + namespace);
   }

   private Fqn getFqn(String ns, String node, Object o)
   {
      return Fqn.fromString(getNamespacedFqn(ns) + "/" + node + "/" + o);
   }

   private Fqn getFqn(String ns, String node)
   {
      return Fqn.fromString(getNamespacedFqn(ns) + "/" + node);
   }

   public void initialize(InputStream jbossCacheConfiguration)
   {
      CacheFactory factory = new DefaultCacheFactory();

      if (jbossCacheConfiguration == null)
      {
         throw new IllegalArgumentException("JBoss Cache configuration InputStream is null");
      }

      this.cache = factory.createCache(jbossCacheConfiguration);

      this.cache.create();
      this.cache.start();

   }

    public void initialize(Cache cache)
   {
      this.cache = cache;

      CacheStatus status = cache.getCacheStatus();

      if (status.createAllowed())
      {
         this.cache.create();
      }
      if (status.startAllowed())
      {
         this.cache.start();
      }

   }

   Cache getCache()
   {
      return cache;
   }


   public void invalidate(String ns)
   {

      boolean success = cache.getRoot().removeChild(getNamespacedFqn(ns));

      if (log.isLoggable(Level.FINER))
      {
         log.finer(this.toString() + "Invalidating namespace:" + ns + "; success=" + success);
      }
   }

   public void invalidateAll()
   {
      boolean success = cache.getRoot().removeChild(getRootNode());

      if (log.isLoggable(Level.FINER))
      {
         log.finer(this.toString() + "Invalidating whole cache - success=" + success);
      }
   }

   /**
    * Store gatein group id
    * @param ns
    * @param pLIDMId
    * @param id
    */
   void putGtnGroupId(String ns, String pLIDMId, String id)
   {
      Fqn nodeFqn = getFqn(ns, NODE_GTN_GROUP_ID, pLIDMId);

      Node ioNode = getCache().getRoot().addChild(nodeFqn);

      if (ioNode != null)
      {
         ioNode.put(NODE_OBJECT_KEY, id);
         setExpiration(ioNode);

         if (log.isLoggable(Level.FINER))
         {

            log.finer(this.toString() + "GateIn group id cached. PLIDM group id: " + pLIDMId +
               "GateIn group id: " + id + ";namespace=" + ns);
         }
      }
   }

   /**
    * Retrieve gatein group id
    * @param ns
    * @param pLIDMId
    * @return
    */
   String getGtnGroupId(String ns, String pLIDMId)
   {

      Fqn nodeFqn = getFqn(ns, NODE_GTN_GROUP_ID, pLIDMId);

      Node node = getCache().getRoot().getChild(nodeFqn);

      if (node != null)
      {
         String id = (String)node.get(NODE_OBJECT_KEY);

         if (log.isLoggable(Level.FINER) && id != null)
         {
            log.finer(this.toString() + "GateIn group id found in cache. PLIDM group id: " + pLIDMId +
            "GateIn group id: " + id + ";namespace=" + ns);
         }

         return id;
      }

      return null;

   }

    /**
    * Store IDMUserListAccess
    * @param ns
    * @param query
    * @param list
    */
   void putGtnUserLazyPageList(String ns, Query query, IDMUserListAccess list)
   {
      Fqn nodeFqn = getFqn(ns, USER_QUERY_NODE, getQueryKey(query));

      Node ioNode = getCache().getRoot().addChild(nodeFqn);

      if (ioNode != null)
      {
         ioNode.put(NODE_OBJECT_KEY, list);
         setExpiration(ioNode);

         if (log.isLoggable(Level.FINER))
         {

            log.finer(this.toString() + "GateIn user query list cached. Query: " + getQueryKey(query) + ";namespace=" + ns);
         }
      }
   }

   /**
    * Retrieve IDMUserListAccess
    * @param ns
    * @param query
    * @return LazyPageList
    */
   IDMUserListAccess getGtnUserLazyPageList(String ns, Query query)
   {

      Fqn nodeFqn = getFqn(ns, USER_QUERY_NODE, getQueryKey(query));

      Node node = getCache().getRoot().getChild(nodeFqn);

      if (node != null)
      {
         IDMUserListAccess list = (IDMUserListAccess)node.get(NODE_OBJECT_KEY);

         if (log.isLoggable(Level.FINER) && list != null)
         {
             log.finer(this.toString() + "GateIn user query list found in cache. Query: " + getQueryKey(query) + ";namespace=" + ns);
         }

         return list;
      }

      return null;

   }

   /**
    * Store PLIDM root group
    * @param ns
    * @param rootGroup
    */
   void putRootGroup(String ns, Group rootGroup)
   {
      Fqn nodeFqn = getFqn(ns, NODE_PLIDM_ROOT_GROUP);

      Node ioNode = getCache().getRoot().addChild(nodeFqn);

      if (ioNode != null)
      {
         setExpiration(ioNode);
         ioNode.put(NODE_OBJECT_KEY, rootGroup);

         if (log.isLoggable(Level.FINER))
         {

            log.finer(this.toString() + "GateIn root group stored in cache" + ";namespace=" + ns);
         }
      }
   }

   /**
    * Retrieve PLIDM root group
    * @param ns
    * @return
    */
   Group getRootGroup(String ns)
   {
      Fqn nodeFqn = getFqn(ns, NODE_PLIDM_ROOT_GROUP);

      Node node = getCache().getRoot().getChild(nodeFqn);

      if (node != null)
      {
         Group rootGroup = (Group)node.get(NODE_OBJECT_KEY);

         if (log.isLoggable(Level.FINER) && rootGroup != null)
         {
            log.finer(this.toString() + "GateIn root group found in cache" + ";namespace=" + ns);
         }

         return rootGroup;
      }

      return null;

   }

   public void setExpiration(Node node)
   {
      if (expiration != -1 && expiration > 0)
      {
         Long future = new Long(System.currentTimeMillis() + expiration);
         node.put(ExpirationAlgorithmConfig.EXPIRATION_KEY, future);
      }
   }

   public int getExpiration()
   {
      return expiration;
   }

   public void setExpiration(int expiration)
   {
      this.expiration = expiration;
   }

   String getQueryKey(Query query)
   {
      StringBuilder sb = new StringBuilder();
      String SEP = ":::";

      sb.append(query.getEmail()).append(SEP)
         .append(query.getFirstName()).append(SEP)
         .append(query.getLastName()).append(SEP)
         .append(query.getUserName()).append(SEP)
         .append(query.getFromLoginDate()).append(SEP)
         .append(query.getToLoginDate()).append(SEP);

      return sb.toString();
   }
}
