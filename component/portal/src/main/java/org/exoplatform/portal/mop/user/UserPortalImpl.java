/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeChangeListener;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.exoplatform.services.organization.Group;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserPortalImpl implements UserPortal {

    public static final Comparator<UserNavigation> USER_NAVIGATION_COMPARATOR = new Comparator<UserNavigation>() {
        public int compare(UserNavigation nav1, UserNavigation nav2) {
            /* Because we cannot tell how expensive getPriority() is
             * we better store the priorities into local variables */
            int priority1 = nav1.getPriority();
            int priority2 = nav2.getPriority();
            if (priority1 == priority2) {
                /* A fix for GTNPORTAL-3322. The original comparator has not imposed
                 * a total ordering as required by the Comparator interface: "The implementor
                 * must ensure that sgn(compare(x, y)) == -sgn(compare(y, x)) for all x and y."
                 * This was not the case when comparing two nodes both having UNDEFINED_PRIORITY. */
                return 0;
            }
            else if (priority1 == PageNavigation.UNDEFINED_PRIORITY) {
                return 1;
            } else if (priority2 == PageNavigation.UNDEFINED_PRIORITY) {
                return -1;
            }
            else {
                return priority1 - priority2;
            }
        }
    };

    /** . */
    final UserPortalConfigService service;

    /** . */
    private final PortalConfig portal;

    /** . */
    final UserPortalContext context;

    /** . */
    final String userName;

    /** . */
    private List<UserNavigation> navigations;

    /** . */
    private final String portalName;

    /** . */
    private final Locale portalLocale;

    public UserPortalImpl(UserPortalConfigService service, String portalName, PortalConfig portal, String userName,
            UserPortalContext context) {
        if (context == null) {
            throw new NullPointerException("No null context argument allowed");
        }

        //
        String locale = portal.getLocale();

        //
        this.portalLocale = locale != null ? new Locale(locale) : null;
        this.service = service;
        this.portalName = portalName;
        this.portal = portal;
        this.userName = userName;
        this.context = context;
        this.navigations = null;
    }

    public Locale getLocale() {
        return portalLocale;
    }

    /**
     * Returns an immutable sorted list of the valid navigations related to the user.
     *
     * @return the navigations
     * @throws UserPortalException any user portal exception
     */
    public List<UserNavigation> getNavigations() throws UserPortalException, NavigationServiceException {
        if (navigations == null) {
            List<UserNavigation> navigations = new ArrayList<UserNavigation>(userName == null ? 1 : 10);
            NavigationContext portalNav = service.getNavigationService().loadNavigation(
                    new SiteKey(SiteType.PORTAL, portalName));
            if (portalNav != null && portalNav.getState() != null) {
                navigations.add(new UserNavigation(this, portalNav, service.getUserACL().hasEditPermission(portal)));
            }
            //
            if (userName != null) {
                // Add user nav if any
                NavigationContext userNavigation = service.getNavigationService().loadNavigation(SiteKey.user(userName));
                if (userNavigation != null && userNavigation.getState() != null) {
                    navigations.add(new UserNavigation(this, userNavigation, true));
                }

                // Add group navigations
                if (service.getUserACL().getSuperUser().equals(userName)) {
                    List<NavigationContext> navCtxs = service.getNavigationService().loadNavigations(SiteType.GROUP);
                    for (NavigationContext navCtx : navCtxs) {
                        if (!navCtx.getKey().getName().equals(service.getUserACL().getGuestsGroup())) {
                            navigations.add(new UserNavigation(this, navCtx, true));
                        }
                    }
                } else {
                    Collection<?> groups;
                    try {
                        groups = service.getOrganizationService().getGroupHandler().findGroupsOfUser(userName);
                    } catch (Exception e) {
                        throw new UserPortalException("Could not retrieve groups", e);
                    }

                    //
                    for (Object group : groups) {
                        Group m = (Group) group;
                        String groupId = m.getId().trim();
                        if (!groupId.equals(service.getUserACL().getGuestsGroup())) {
                            NavigationContext groupNavigation = service.getNavigationService().loadNavigation(
                                    SiteKey.group(groupId));
                            if (groupNavigation != null && groupNavigation.getState() != null) {
                                navigations.add(new UserNavigation(this, groupNavigation, service.getUserACL()
                                        .hasEditPermissionOnNavigation(groupNavigation.getKey())));
                            }
                        }
                    }
                }

                // Sort the list finally
                Collections.sort(navigations, USER_NAVIGATION_COMPARATOR);
            }

            //
            this.navigations = Collections.unmodifiableList(navigations);
        }
        return navigations;
    }

    public UserNavigation getNavigation(SiteKey key) throws NullPointerException, UserPortalException,
            NavigationServiceException {
        if (key == null) {
            throw new NullPointerException("No null key accepted");
        }
        for (UserNavigation navigation : getNavigations()) {
            if (navigation.getKey().equals(key)) {
                return navigation;
            }
        }
        return null;
    }

    public void refresh() {
        navigations = null;
    }

    public UserNode getNode(UserNavigation userNavigation, Scope scope, UserNodeFilterConfig filterConfig,
            NodeChangeListener<UserNode> listener) throws NullPointerException, UserPortalException, NavigationServiceException {
        UserNodeContext context = new UserNodeContext(userNavigation, filterConfig);
        NodeContext<UserNode> nodeContext = service.getNavigationService().loadNode(context, userNavigation.navigation, scope,
                new UserNodeListener(listener));
        if (nodeContext != null) {
            return nodeContext.getNode().filter();
        } else {
            return null;
        }
    }

    public void updateNode(UserNode node, Scope scope, NodeChangeListener<UserNode> listener) throws NullPointerException,
            IllegalArgumentException, UserPortalException, NavigationServiceException {
        if (node == null) {
            throw new NullPointerException("No null node accepted");
        }
        service.getNavigationService().updateNode(node.context, scope, new UserNodeListener(listener));
        node.filter();
    }

    public void rebaseNode(UserNode node, Scope scope, NodeChangeListener<UserNode> listener) throws NullPointerException,
            IllegalArgumentException, UserPortalException, NavigationServiceException {
        if (node == null) {
            throw new NullPointerException("No null node accepted");
        }
        service.getNavigationService().rebaseNode(node.context, scope, new UserNodeListener(listener));
        node.filter();
    }

    public void saveNode(UserNode node, NodeChangeListener<UserNode> listener) throws NullPointerException,
            UserPortalException, NavigationServiceException {
        if (node == null) {
            throw new NullPointerException("No null node accepted");
        }
        service.getNavigationService().saveNode(node.context, new UserNodeListener(listener));
        node.filter();
    }

    /**
     * Note : the scope implementation is not stateless but we don't care in this case.
     */
    private class MatchingScope extends GenericScope.Branch.Visitor implements Scope {
        final UserNavigation userNavigation;
        final UserNodeFilterConfig filterConfig;
        final String[] match;
        int score;
        String id;
        UserNode userNode;

        MatchingScope(UserNavigation userNavigation, UserNodeFilterConfig filterConfig, String[] match) {
            this.userNavigation = userNavigation;
            this.filterConfig = filterConfig;
            this.match = match;
        }

        public Visitor get() {
            return this;
        }

        @Override
        protected int getSize() {
            return match.length;
        }

        @Override
        protected String getName(int index) {
            return match[index];
        }

        @Override
        protected Visitor getFederated() {
            return Scope.CHILDREN.get();
        }

        void resolve() throws NavigationServiceException {
            UserNodeContext context = new UserNodeContext(userNavigation, filterConfig);
            NodeContext<UserNode> nodeContext = service.getNavigationService().loadNode(context, userNavigation.navigation,
                    this, null);
            if (score > 0) {
                userNode = nodeContext.getNode().filter().find(id);
            }
        }

        public VisitMode enter(int depth, String id, String name, NodeState state) {
            VisitMode vm = super.enter(depth, id, name, state);
            if (depth == 0) {
                score = 0;
                MatchingScope.this.id = null;
            } else {
                if (vm == VisitMode.ALL_CHILDREN) {
                    MatchingScope.this.id = id;
                    score++;
                }
            }
            return vm;
        }
    }

    public UserNode getDefaultPath(UserNodeFilterConfig filterConfig) throws UserPortalException, NavigationServiceException {
        for (UserNavigation userNavigation : getNavigations()) {
            UserNode node = getDefaultPath(userNavigation, filterConfig);
            if (node != null) {
                return node;
            }
        }

        //
        return null;
    }

    public UserNode getDefaultPath(UserNavigation userNavigation, UserNodeFilterConfig filterConfig)
            throws UserPortalException, NavigationServiceException {
        NavigationContext navigation = userNavigation.navigation;
        if (navigation.getState() != null) {
            UserNodeContext context = new UserNodeContext(userNavigation, null);
            NodeContext<UserNode> nodeContext = service.getNavigationService().loadNode(context, navigation, Scope.CHILDREN,
                    null);
            if (nodeContext != null) {
                UserNode root = nodeContext.getNode();

                //
                if (filterConfig == null) {
                    filterConfig = UserNodeFilterConfig.builder().build();
                }
                UserNodeFilter filter = new UserNodeFilter(userNavigation.portal, filterConfig);

                // Filter node by node
                for (UserNode node : root.getChildren()) {
                    if (node.context.accept(filter)) {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    public UserNode resolvePath(UserNodeFilterConfig filterConfig, String path) throws NullPointerException,
            UserPortalException, NavigationServiceException {
        if (path == null) {
            throw new NullPointerException("No null path accepted");
        }

        // Parse path
        String[] segments = Utils.parsePath(path);

        // Find the first navigation available or return null
        if (segments == null) {
            return getDefaultPath(filterConfig);
        }

        // Create a filter as we need one for the path
        if (filterConfig == null) {
            filterConfig = UserNodeFilterConfig.builder().build();
        } else {
            filterConfig = UserNodeFilterConfig.builder(filterConfig).build();
        }

        // Restrict the filter with path
        filterConfig.path = segments;

        // Get navigations
        List<UserNavigation> navigations = getNavigations();

        //
        MatchingScope best = null;
        for (UserNavigation navigation : navigations) {
            MatchingScope scope = new MatchingScope(navigation, filterConfig, segments);
            scope.resolve();
            if (scope.score == segments.length) {
                best = scope;
                break;
            } else {
                if (best == null) {
                    best = scope;
                } else {
                    if (scope.score > best.score) {
                        best = scope;
                    }
                }
            }
        }

        //
        if (best != null && best.score > 0) {
            UserNode ret = best.userNode;
            if (ret != null) {
                ret.owner.filterConfig.path = null;
            }
            return ret;
        } else {
            return getDefaultPath(null);
        }
    }

    public UserNode resolvePath(UserNavigation navigation, UserNodeFilterConfig filterConfig, String path)
            throws NullPointerException, UserPortalException, NavigationServiceException {
        if (navigation == null) {
            throw new NullPointerException("No null navigation accepted");
        }
        if (path == null) {
            throw new NullPointerException("No null path accepted");
        }

        //
        String[] segments = Utils.parsePath(path);

        //
        if (segments == null) {
            return null;
        }

        // Create a filter as we need one for the path
        if (filterConfig == null) {
            filterConfig = UserNodeFilterConfig.builder().build();
        } else {
            filterConfig = UserNodeFilterConfig.builder(filterConfig).build();
        }

        // Restrict the filter with the path
        filterConfig.path = segments;

        //
        MatchingScope scope = new MatchingScope(navigation, filterConfig, segments);
        scope.resolve();

        //
        if (scope.score > 0) {
            UserNode ret = scope.userNode;
            if (ret != null) {
                ret.owner.filterConfig.path = null;
            }
            return ret;
        } else {
            return null;
        }
    }
}
