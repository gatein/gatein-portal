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
package org.gatein.portal.web.controllers;

import java.util.Collection;

import javax.inject.Inject;

import juzu.Resource;
import juzu.Response;
import juzu.Route;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.json.JSONArray;
import org.json.JSONObject;

public class Organization {
    @Inject
    private OrganizationService orgService;

    @Resource
    @Route(value = "/organization/allGroupAndMembershipType")
    public Response getAllGroupAndMembershipType() {
        JSONObject result = new JSONObject();

        try {
            Collection<Group> groups = orgService.getGroupHandler().getAllGroups();
            Collection<MembershipType> membershipTypes = orgService.getMembershipTypeHandler().findMembershipTypes();

            JSONArray jsonGroups = new JSONArray();
            for(Group group : groups) {
                JSONObject jsonGroup = new JSONObject();
                jsonGroup.put("id", group.getId());
                jsonGroup.put("name", group.getGroupName());
                jsonGroup.put("label", group.getLabel());

                jsonGroups.put(jsonGroup);
            }

            JSONArray jsonMembershipTypes = new JSONArray();
            for(MembershipType membershipType : membershipTypes) {
                JSONObject jsonMembershipType = new JSONObject();
                jsonMembershipType.put("name", membershipType.getName());

                jsonMembershipTypes.put(jsonMembershipType);
            }

            result.put("groups", jsonGroups);
            result.put("membershipTypes", jsonMembershipTypes);

            return Response.status(200).body(result.toString());

        } catch (Exception ex) {
            return Response.status(500).body("Exception: " + ex.getMessage());
        }
    }
}
