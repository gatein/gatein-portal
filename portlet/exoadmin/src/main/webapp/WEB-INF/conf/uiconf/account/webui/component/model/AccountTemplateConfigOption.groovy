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

import java.util.List;
import java.util.ArrayList;
import org.exoplatform.account.webui.component.model.UIAccountTemplateConfigOption ;
import org.exoplatform.organization.webui.component.UIUserMembershipSelector;
import org.exoplatform.webui.core.model.SelectItemCategory;

List options = new ArrayList();

  SelectItemCategory guest = new SelectItemCategory("DefaultAccount");
  guest.addSelectItemOption(
    new UIAccountTemplateConfigOption("", "DefaultAccount", "Description for Guest Account", "DefaultAccount").
    addMembership(new UIUserMembershipSelector.Membership("exo","/guest","member"))
  );
  options.add(guest);

  SelectItemCategory community = new SelectItemCategory("CommunityAccount");
  community.addSelectItemOption(
    new UIAccountTemplateConfigOption("", "CommunityAccount", "Description for User Account", "CommunityAccount").                         
    addMembership(new UIUserMembershipSelector.Membership("community","/user","member")).
    addMembership(new UIUserMembershipSelector.Membership("community","/portal/community","member"))
  );
  options.add(community);
  
  SelectItemCategory company = new SelectItemCategory("CompanyAccount");
  company.addSelectItemOption(
    new UIAccountTemplateConfigOption("", "CompanyAccount", "Description for Company Account", "CompanyAccount").                         
    addMembership(new UIUserMembershipSelector.Membership("company","/user","member")).
    addMembership(new UIUserMembershipSelector.Membership("company","/portal/company","member"))
  );
  options.add(company);
  
  SelectItemCategory admin = new SelectItemCategory("AdminAccount");
  admin.addSelectItemOption(
    new UIAccountTemplateConfigOption("", "AdminAccount", "Description for Admin Account", "AdminAccount").
    addMembership(new UIUserMembershipSelector.Membership("exo","/user","member")).
    addMembership(new UIUserMembershipSelector.Membership("exoadmin","/admin","member"))
  );
  options.add(admin);

return options ;
