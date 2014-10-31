package org.exoplatform.services.organization.idm;

import org.exoplatform.services.organization.Query;
import org.gatein.common.NotYetImplemented;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picketlink.idm.api.Group;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.cache.AbstractInfinispanCacheProvider;
import org.picketlink.idm.impl.tree.Fqn;
import org.picketlink.idm.impl.tree.Node;
import org.picketlink.idm.impl.tree.TreeCache;

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
public class IntegrationCache extends AbstractInfinispanCacheProvider {
    private static final Logger log = LoggerFactory.getLogger(IntegrationCache.class);

    public static final String NODE_GTN_GROUP_ID = "NODE_GTN_GROUP_ID";

    public static final String NODE_PLIDM_ROOT_GROUP = "NODE_PLIDM_ROOT_GROUP";

    public static final String USER_QUERY_NODE = "GTN_USER_QUERY_LAZY_LIST";

    public static final String MAIN_ROOT = "NODE_GTN_ORG_SERVICE_INT_CACHE_MAIN_ROOT";

    public static final String NODE_OBJECT_KEY = "object";

    @Override
    protected String getRootNode() {
        return MAIN_ROOT;
    }

    // Not needed for integration cache
    protected TreeCache getCacheFromRegistry(Object registry, String registryName) throws IdentityException {
        throw new NotYetImplemented("getCacheFromRegistry not implemented on IntegrationCache");
    }

    /**
     * Store gatein group id
     *
     * @param ns
     * @param pLIDMId
     * @param id
     */
    void putGtnGroupId(String ns, String pLIDMId, String id) {
        Fqn nodeFqn = getFqn(ns, NODE_GTN_GROUP_ID, pLIDMId);

        Node ioNode = addNode(nodeFqn);

        if (ioNode != null) {
            ioNode.put(NODE_OBJECT_KEY, id);

            if (log.isTraceEnabled()) {

                log.trace(this.toString() + "GateIn group id cached. PLIDM group id: " + pLIDMId + "GateIn group id: " + id
                        + ";namespace=" + ns);
            }
        }
    }

    /**
     * Retrieve gatein group id
     *
     * @param ns
     * @param pLIDMId
     * @return
     */
    String getGtnGroupId(String ns, String pLIDMId) {

        Fqn nodeFqn = getFqn(ns, NODE_GTN_GROUP_ID, pLIDMId);

        Node node = getNode(nodeFqn);

        if (node != null) {
            String id = (String) node.get(NODE_OBJECT_KEY);

            if (log.isTraceEnabled() && id != null) {
                log.trace(this.toString() + "GateIn group id found in cache. PLIDM group id: " + pLIDMId + "GateIn group id: "
                        + id + ";namespace=" + ns);
            }

            return id;
        }

        return null;

    }

    /**
     * Store IDMUserListAccess
     *
     * @param ns
     * @param query
     * @param list
     */
    void putGtnUserLazyPageList(String ns, Query query, IDMUserListAccess list) {
        Fqn nodeFqn = getFqn(ns, USER_QUERY_NODE, getQueryKey(query));

        Node ioNode = addNode(nodeFqn);

        if (ioNode != null) {
            ioNode.put(NODE_OBJECT_KEY, list);

            if (log.isTraceEnabled()) {
                log.trace(this.toString() + "GateIn user query list cached. Query: " + getQueryKey(query) + ";namespace=" + ns);
            }
        }
    }

    /**
     * Retrieve IDMUserListAccess
     *
     * @param ns
     * @param query
     * @return LazyPageList
     */
    IDMUserListAccess getGtnUserLazyPageList(String ns, Query query) {

        Fqn nodeFqn = getFqn(ns, USER_QUERY_NODE, getQueryKey(query));

        Node node = getNode(nodeFqn);

        if (node != null) {
            IDMUserListAccess list = (IDMUserListAccess) node.get(NODE_OBJECT_KEY);

            if (log.isTraceEnabled() && list != null) {
                log.trace(this.toString() + "GateIn user query list found in cache. Query: " + getQueryKey(query)
                        + ";namespace=" + ns);
            }

            return list;
        }

        return null;

    }

    /**
     * Store PLIDM root group
     *
     * @param ns
     * @param rootGroup
     */
    void putRootGroup(String ns, Group rootGroup) {
        Fqn nodeFqn = getFqn(ns, NODE_PLIDM_ROOT_GROUP);

        Node ioNode = addNode(nodeFqn);

        if (ioNode != null) {
            ioNode.put(NODE_OBJECT_KEY, rootGroup);

            if (log.isTraceEnabled()) {

                log.trace(this.toString() + "GateIn root group stored in cache" + ";namespace=" + ns);
            }
        }
    }

    /**
     * Retrieve PLIDM root group
     *
     * @param ns
     * @return
     */
    Group getRootGroup(String ns) {
        Fqn nodeFqn = getFqn(ns, NODE_PLIDM_ROOT_GROUP);

        Node node = getNode(nodeFqn);

        if (node != null) {
            Group rootGroup = (Group) node.get(NODE_OBJECT_KEY);

            if (log.isTraceEnabled() && rootGroup != null) {
                log.trace(this.toString() + "GateIn root group found in cache" + ";namespace=" + ns);
            }

            return rootGroup;
        }

        return null;

    }

    String getQueryKey(Query query) {
        StringBuilder sb = new StringBuilder();
        String SEP = ":::";

        sb.append(query.getEmail()).append(SEP).append(query.getFirstName()).append(SEP).append(query.getLastName())
                .append(SEP).append(query.getUserName()).append(SEP).append(query.getFromLoginDate()).append(SEP)
                .append(query.getToLoginDate()).append(SEP);

        return sb.toString();
    }
}
