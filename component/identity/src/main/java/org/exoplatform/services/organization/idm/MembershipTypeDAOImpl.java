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

package org.exoplatform.services.organization.idm;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListenerStack;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeEventListener;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.gatein.common.logging.LogLevel;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.RoleType;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class MembershipTypeDAOImpl extends AbstractDAOImpl implements MembershipTypeHandler {

    public static final String MEMBERSHIP_DESCRIPTION = "description";

    public static final String MEMBERSHIP_OWNER = "owner";

    public static final String MEMBERSHIP_CREATE_DATE = "create_date";

    public static final String MEMBERSHIP_MODIFIED_DATE = "modified_date";

    public static final DateFormat dateFormat = DateFormat.getInstance();

    private List listeners_;

    public MembershipTypeDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service) {
        super(orgService, service);
        listeners_ = new ListenerStack(5);
    }

    public void addMembershipTypeEventListener(MembershipTypeEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        listeners_.add(listener);
    }

    public void removeMembershipTypeEventListener(MembershipTypeEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.remove(listener);
    }

    public final MembershipType createMembershipTypeInstance() {
        return new MembershipTypeImpl();
    }

    public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "createMembershipType", new Object[] { "membershipType", mt, "broadcast",
                    broadcast });
        }

        Date now = new Date();
        mt.setCreatedDate(now);
        mt.setModifiedDate(now);

        if (broadcast) {
            preSave(mt, true);
        }

        try {
            getIdentitySession().getRoleManager().createRoleType(mt.getName());
        } catch (Exception e) {
            handleException("Error when creating Membership", e);
        }

        if (broadcast) {
            postSave(mt, true);
        }

        updateMembershipType(mt);

        return mt;
    }

    public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "saveMembershipType", new Object[] { "membershipType", mt, "broadcast",
                    broadcast });
        }
        Date now = new Date();
        mt.setModifiedDate(now);

        if (broadcast) {
            preSave(mt, true);
        }

        updateMembershipType(mt);

        if (broadcast) {
            postSave(mt, true);
        }

        return mt;
    }

    public MembershipType findMembershipType(String name) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipType", new Object[] { "name", name });
        }

        RoleType rt = null;
        try {
            rt = getIdentitySession().getRoleManager().getRoleType(name);
        } catch (Exception e) {
            handleException("Identity error when finding membership type " + name, e);
        }

        MembershipType mt = null;

        if (rt != null) {
            mt = new MembershipTypeImpl(name, null, null);
            populateMembershipType(mt);
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipType", mt);
        }

        return mt;
    }

    public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "removeMembershipType",
                    new Object[] { "name", name, "broadcast", broadcast });
        }

        MembershipType mt = findMembershipType(name);

        if (mt != null) {
            if (broadcast) {
                preDelete(mt);
            }

            try {
                getIdentitySession().getRoleManager().removeRoleType(mt.getName());
            } catch (Exception e) {
                handleException("Error occured when removing membership type", e);
            }

            if (broadcast) {
                postDelete(mt);
            }

        }

        return mt;

    }

    public Collection findMembershipTypes() throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findMembershipTypes", null);
        }

        Collection<RoleType> rts;
        try {
            rts = getIdentitySession().getRoleManager().findRoleTypes();
        } catch (Exception e) {
            handleException("Exception occured when looking for membership types", e);
            rts = new LinkedList<RoleType>();
        }

        List<MembershipType> mts = new LinkedList<MembershipType>();

        for (RoleType rt : rts) {
            MembershipType mt = new MembershipTypeImpl(rt.getName(), null, null);
            populateMembershipType(mt);
            mts.add(mt);
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findMembershipTypes", mts);
        }

        return mts;
    }

    private void updateMembershipType(MembershipType mt) throws Exception {
        RoleType rt;
        try {
            rt = getIdentitySession().getRoleManager().getRoleType(mt.getName());
        } catch (Exception e) {
            handleException("Exception occured when finding role type", e);
            return;
        }

        Map<String, String> props = new HashMap<String, String>();

        props.put(MEMBERSHIP_DESCRIPTION, mt.getDescription());
        props.put(MEMBERSHIP_CREATE_DATE, mt.getCreatedDate() == null ? null : "" + mt.getCreatedDate().getTime());
        props.put(MEMBERSHIP_MODIFIED_DATE, mt.getModifiedDate() == null ? null : "" + mt.getModifiedDate().getTime());
        props.put(MEMBERSHIP_OWNER, mt.getOwner());

        try {
            getIdentitySession().getRoleManager().setProperties(rt, props);
        } catch (Exception e) {
            handleException("Exception when updating membership type", e);
        }

        return;

    }

    private void populateMembershipType(MembershipType mt) throws Exception {
        Map<String, String> props;
        try {
            RoleType rt = getIdentitySession().getRoleManager().getRoleType(mt.getName());
            props = getIdentitySession().getRoleManager().getProperties(rt);
        } catch (Exception e) {
            handleException("Identity error occured when populating membership type", e);
            return;
        }

        mt.setDescription(props.get(MEMBERSHIP_DESCRIPTION));
        mt.setOwner(props.get(MEMBERSHIP_OWNER));

        String cd = props.get(MEMBERSHIP_CREATE_DATE);
        String md = props.get(MEMBERSHIP_MODIFIED_DATE);

        if (cd != null) {
            try {
                long date = Long.parseLong(cd);
                mt.setCreatedDate(new Date(date));
            } catch (NumberFormatException e) {
                try {
                    // For backward compatibility with GateIn 3.0 and EPP 5 Beta
                    mt.setCreatedDate(dateFormat.parse(cd));
                } catch (ParseException e2) {
                    log.error("Cannot parse the membership type creation date for: " + mt.getName());
                }
            }
        }

        if (md != null) {
            try {
                long date = Long.parseLong(md);
                mt.setModifiedDate(new Date(date));
            } catch (NumberFormatException e) {
                // For backward compatibility with GateIn 3.0 and EPP 5 Beta
                try {
                    mt.setModifiedDate(dateFormat.parse(md));
                } catch (ParseException e2) {
                    log.error("Cannot parse the membership type modification date for: " + mt.getName());
                }
            }
        }

        return;
    }

    private void preSave(MembershipType membershipType, boolean isNew) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipTypeEventListener listener = (MembershipTypeEventListener) listeners_.get(i);
            listener.preSave(membershipType, isNew);
        }
    }

    private void postSave(MembershipType membershipType, boolean isNew) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipTypeEventListener listener = (MembershipTypeEventListener) listeners_.get(i);
            listener.postSave(membershipType, isNew);
        }
    }

    private void preDelete(MembershipType membershipType) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipTypeEventListener listener = (MembershipTypeEventListener) listeners_.get(i);
            listener.preDelete(membershipType);
        }
    }

    private void postDelete(MembershipType membershipType) throws Exception {
        for (int i = 0; i < listeners_.size(); i++) {
            MembershipTypeEventListener listener = (MembershipTypeEventListener) listeners_.get(i);
            listener.postDelete(membershipType);
        }
    }

}
