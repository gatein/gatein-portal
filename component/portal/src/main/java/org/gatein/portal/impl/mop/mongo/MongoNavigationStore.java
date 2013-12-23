/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.impl.mop.mongo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.portal.mop.hierarchy.NodeData;
import org.gatein.portal.mop.navigation.NavigationData;
import org.gatein.portal.mop.navigation.NavigationState;
import org.gatein.portal.mop.navigation.NavigationStore;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author Julien Viet
 */
public class MongoNavigationStore implements NavigationStore {

    /** . */
    private final MongoStore store;

    /** . */
    private final MongoDescriptionStore descriptionStore;
    
    /** . */
    private final MongoSecurityStore securityStore;

    public MongoNavigationStore(MongoStore store, MongoDescriptionStore descriptionStore, MongoSecurityStore securityStore) {
        this.store = store;
        this.descriptionStore = descriptionStore;
        this.securityStore = securityStore;
    }

    private DBCollection getNavigations() {
        return store.getDB().getCollection("navigations");
    }

    private static BasicDBObject getKey(SiteKey siteKey) {
        return getKey(siteKey.getType(), siteKey.getName());
    }

    private static BasicDBObject getKey(SiteType siteType, String siteName) {
        BasicDBObject key = new BasicDBObject();
        if (siteType != null) {
            key.put("site_type", siteType.getName());
        }
        if (siteName != null) {
            key.put("site_name", siteName);
        }
        return key;
    }

    @Override
    public List<NavigationData> loadNavigations(SiteType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NavigationData loadNavigationData(SiteKey navigationKey) {
        BasicDBObject key = getKey(navigationKey);
        DBCollection navigations = getNavigations();
        DBObject doc = navigations.findOne(key);
        if (doc == null) {
            return null;
        } else {
            ObjectId id = (ObjectId) doc.get("_id");
            NavigationState navigationState = new NavigationState((Integer) doc.get("priority"));
            return new NavigationData(navigationKey, navigationState, id.toString());
        }
    }

    @Override
    public void saveNavigation(SiteKey navigationKey, NavigationState navigationState) {
        BasicDBObject key = getKey(navigationKey);
        DBCollection navigations = getNavigations();
        DBObject doc = navigations.findOne(key);
        if (doc == null) {
            doc = toDoc(key, NodeState.INITIAL);
            doc.put("name", "");
            doc.put("children", new ArrayList());
        }
        doc.put("priority", navigationState.getPriority());
        navigations.save(doc);
    }

    @Override
    public boolean destroyNavigation(SiteKey key) {
        DBCollection navigations = getNavigations();
        DBObject doc = navigations.findOne(getKey(key));
        if (doc != null) {
            String id = doc.get("_id").toString();
            destroyNode(id);
            descriptionStore.saveDescriptions(id, null);
            securityStore.savePermission(id, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public NodeData<NodeState> loadNode(String nodeId) {
        DBObject key = new BasicDBObject("_id", new ObjectId(nodeId));
        DBCollection navigations = getNavigations();
        DBObject doc = navigations.findOne(key);
        if (doc == null) {
            return null;
        } else {
            return create(nodeId, doc);
        }
    }

    private NodeData<NodeState> create(String id, DBObject doc) {
        String name = (String) doc.get("name");
        String parentId = (String) doc.get("parent_id");
        String label = (String) doc.get("label");
        String icon = (String) doc.get("icon");
        long startPublicationDate = (Long) doc.get("start_publication_date");
        long endPublicationDate = (Long) doc.get("end_publication_date");
        Visibility visibility = Visibility.valueOf(((String) doc.get("visibility")).toUpperCase());
        List<String> children = (ArrayList<String>) doc.get("children");
        PageKey link;
        DBObject linkDoc = (DBObject) doc.get("link");
        if (linkDoc != null) {
            String siteType = (String) linkDoc.get("site_type");
            String siteName = (String) linkDoc.get("site_name");
            String pageName = (String) linkDoc.get("name");
            link = SiteType.forName(siteType).key(siteName).page(pageName);
        } else {
            link = null;
        }
        NodeState state = new NodeState(label, icon, startPublicationDate, endPublicationDate, visibility, link);
        return new NodeData<NodeState>(parentId, id, name, state, children.toArray(new String[children.size()]));
    }

    private DBObject toDoc(DBObject doc, NodeState nodeState) {
        PageKey key = nodeState.getPageRef();
        DBObject link;
        if (key != null) {
            link = new BasicDBObject();
            link.put("site_type", key.getSite().getTypeName());
            link.put("site_name", key.getSite().getName());
            link.put("name", key.getName());
        } else {
            link = null;
        }
        doc.put("label", nodeState.getLabel());
        doc.put("visibility", nodeState.getVisibility().name().toLowerCase());
        doc.put("start_publication_date", nodeState.getStartPublicationTime());
        doc.put("end_publication_date", nodeState.getEndPublicationTime());
        doc.put("icon", nodeState.getIcon());
        doc.put("link", link);
        return doc;
    }

    @Override
    public NodeData<NodeState>[] createNode(String parentId, String previousId, String name, NodeState nodeState) {
        DBObject doc = toDoc(new BasicDBObject(), nodeState);
        doc.put("parent_id", parentId);
        doc.put("name", name);
        doc.put("children", new ArrayList<String>());
        DBCollection navigations = getNavigations();
        navigations.save(doc);
        String id = doc.get("_id").toString();
        DBObject parentDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(parentId)));
        List<String> children = (List<String>) parentDoc.get("children");
        int index = previousId != null ? children.indexOf(previousId) + 1 : 0;
        children.add(index, id);
        navigations.update(new BasicDBObject("_id", new ObjectId(parentId)), new BasicDBObject("$set", new BasicDBObject("children", children)));
        return new NodeData[]{
                create(parentId, parentDoc),
                create(id, doc)
        };
    }

    @Override
    public NodeData<NodeState> destroyNode(String targetId) {
        DBCollection navigations = getNavigations();
        DBObject found = navigations.findOne(new BasicDBObject("_id", new ObjectId(targetId)));
        LinkedList<ObjectId> descendants = getDescendants(navigations, found, new LinkedList<ObjectId>());
        navigations.remove(new BasicDBObject("_id", new BasicDBObject("$in", descendants)));
        String parentId = (String) found.get("parent_id");
        if (parentId != null) {
            DBObject parentDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(parentId)));
            List<String> children = (List<String>) parentDoc.get("children");
            children.remove(targetId);
            securityStore.savePermission(targetId, null);
            navigations.update(new BasicDBObject("_id", new ObjectId(parentId)), new BasicDBObject("$set", new BasicDBObject("children", children)));
            return create(parentId, parentDoc);
        } else {
            return null;
        }
    }

    /**
     * Get the list of descendants of the specified doc, including the specified doc.
     *
     * @param doc the node
     */
    private LinkedList<ObjectId> getDescendants(DBCollection navigations, DBObject doc, LinkedList<ObjectId> descendants) {
        String id = doc.get("_id").toString();
        descendants.add(new ObjectId(id));
        List<String> children = (List<String>) doc.get("children");
        if (children.size() > 0) {
            DBCursor cursor = navigations.find(new BasicDBObject("_id", new BasicDBObject("$in", children)));
            for (DBObject childDoc : cursor) {
                String childId = childDoc.get("_id").toString();
                descendants.add(new ObjectId(childId));
                getDescendants(navigations, childDoc, descendants);
            }
        }
        return descendants;
    }

    @Override
    public NodeData<NodeState> updateNode(String targetId, NodeState state) {
        DBObject updateDoc = toDoc(new BasicDBObject(), state);
        DBCollection navigations = getNavigations();
        navigations.update(new BasicDBObject("_id", new ObjectId(targetId)), new BasicDBObject("$set", updateDoc));
        DBObject targetDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(targetId)));
        return create(targetId, targetDoc);
    }

    @Override
    public NodeData<NodeState>[] moveNode(String targetId, String fromId, String toId, String previousId) {
        DBCollection navigations = getNavigations();
        if (fromId.equals(toId)) {
            DBObject parentDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(fromId)));
            List<String> children = (List<String>) parentDoc.get("children");
            children.remove(targetId);
            int index = previousId != null ? children.indexOf(previousId) + 1 : 0;
            children.add(index, targetId);
            navigations.update(new BasicDBObject("_id", new ObjectId(fromId)), new BasicDBObject("$set", new BasicDBObject("children", children)));
            NodeData<NodeState> target = create(targetId, navigations.findOne(new BasicDBObject("_id", new ObjectId(targetId))));
            NodeData<NodeState> parent = create(fromId, navigations.findOne(new BasicDBObject("_id", new ObjectId(fromId))));
            return new NodeData[]{ target, parent, parent };
        } else {
            DBObject fromDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(fromId)));
            DBObject toDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(toId)));
            List<String> fromChildren = (List<String>) fromDoc.get("children");
            List<String> toChildren = (List<String>) toDoc.get("children");
            fromChildren.remove(targetId);
            int toIndex = previousId != null ? toChildren.indexOf(previousId) + 1 : toChildren.size();
            toChildren.add(toIndex, targetId);
            navigations.update(new BasicDBObject("_id", new ObjectId(targetId)), new BasicDBObject("$set", new BasicDBObject("parent_id", toId)));
            navigations.update(new BasicDBObject("_id", new ObjectId(fromId)), new BasicDBObject("$set", new BasicDBObject("children", fromChildren)));
            navigations.update(new BasicDBObject("_id", new ObjectId(toId)), new BasicDBObject("$set", new BasicDBObject("children", toChildren)));
            NodeData<NodeState> target = create(targetId, navigations.findOne(new BasicDBObject("_id", new ObjectId(targetId))));
            NodeData<NodeState> from = create(fromId, navigations.findOne(new BasicDBObject("_id", new ObjectId(fromId))));
            NodeData<NodeState> to = create(toId, navigations.findOne(new BasicDBObject("_id", new ObjectId(toId))));
            return new NodeData[]{ target, from, to };
        }
    }

    @Override
    public NodeData<NodeState>[] renameNode(String targetId, String parentId, String name) {
        DBCollection navigations = getNavigations();
        navigations.update(new BasicDBObject("_id", new ObjectId(targetId)), new BasicDBObject("$set", new BasicDBObject("name", name)));
        DBObject targetDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(targetId)));
        DBObject parentDoc = navigations.findOne(new BasicDBObject("_id", new ObjectId(parentId)));
        NodeData<NodeState> target = create(targetId, targetDoc);
        NodeData<NodeState> parent = create(parentId, parentDoc);
        return new NodeData[]{target, parent};
    }

    @Override
    public void flush() {
    }
}
