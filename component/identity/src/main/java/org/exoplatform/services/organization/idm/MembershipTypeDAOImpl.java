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

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.RoleType;

import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MembershipTypeDAOImpl implements MembershipTypeHandler
{

   public static final String MEMBERSHIP_DESCRIPTION = "description";

   public static final String MEMBERSHIP_OWNER = "owner";

   public static final String MEMBERSHIP_CREATE_DATE = "create_date";

   public static final String MEMBERSHIP_MODIFIED_DATE = "modified_date";

   public static final DateFormat dateFormat = DateFormat.getInstance();

   private PicketLinkIDMService service_;

   private PicketLinkIDMOrganizationServiceImpl orgService;

   public MembershipTypeDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService service)
   {
      service_ = service;
      this.orgService = orgService;
   }

   final public MembershipType createMembershipTypeInstance()
   {
      return new MembershipTypeImpl();
   }

   public MembershipType createMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Date now = new Date();
      mt.setCreatedDate(now);
      mt.setModifiedDate(now);

      getIdentitySession().getRoleManager().createRoleType(mt.getName());
      updateMembershipType(mt);

      return mt;
   }

   public MembershipType saveMembershipType(MembershipType mt, boolean broadcast) throws Exception
   {
      Date now = new Date();
      mt.setModifiedDate(now);
      updateMembershipType(mt);
      return mt;
   }

   public MembershipType findMembershipType(String name) throws Exception
   {
      RoleType rt = getIdentitySession().getRoleManager().getRoleType(name);

      MembershipType mt = null;

      if (rt != null)
      {
         mt = new MembershipTypeImpl(name, null, null);
         populateMembershipType(mt);
      }

      return mt;
   }

   public MembershipType removeMembershipType(String name, boolean broadcast) throws Exception
   {
      MembershipType mt = findMembershipType(name);

      if (mt != null)
      {
         getIdentitySession().getRoleManager().removeRoleType(mt.getName());
      }

      return mt;

   }

   public Collection findMembershipTypes() throws Exception
   {

      Collection<RoleType> rts = getIdentitySession().getRoleManager().findRoleTypes();

      List<MembershipType> mts = new LinkedList<MembershipType>();

      for (RoleType rt : rts)
      {
         MembershipType mt = new MembershipTypeImpl(rt.getName(), null, null);
         populateMembershipType(mt);
         mts.add(mt);
      }

      return mts;
   }

   private IdentitySession getIdentitySession() throws Exception
   {
      return service_.getIdentitySession();
   }

   private void updateMembershipType(MembershipType mt) throws Exception
   {

      RoleType rt = getIdentitySession().getRoleManager().getRoleType(mt.getName());

      Map<String, String> props = new HashMap<String, String>();

      props.put(MEMBERSHIP_DESCRIPTION, mt.getDescription());
      props.put(MEMBERSHIP_CREATE_DATE, mt.getCreatedDate() == null ? null : dateFormat.format(mt.getCreatedDate()));
      props
         .put(MEMBERSHIP_MODIFIED_DATE, mt.getModifiedDate() == null ? null : dateFormat.format(mt.getModifiedDate()));
      props.put(MEMBERSHIP_OWNER, mt.getOwner());

      getIdentitySession().getRoleManager().setProperties(rt, props);

      return;

   }

   private void populateMembershipType(MembershipType mt) throws Exception
   {
      RoleType rt = getIdentitySession().getRoleManager().getRoleType(mt.getName());

      Map<String, String> props = getIdentitySession().getRoleManager().getProperties(rt);

      mt.setDescription(props.get(MEMBERSHIP_DESCRIPTION));
      mt.setOwner(props.get(MEMBERSHIP_OWNER));

      String cd = props.get(MEMBERSHIP_CREATE_DATE);
      String md = props.get(MEMBERSHIP_MODIFIED_DATE);

      if (cd != null)
      {
         mt.setCreatedDate(dateFormat.parse(cd));
      }

      if (md != null)
      {
         mt.setModifiedDate(dateFormat.parse(md));
      }

      return;
   }

}
