/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.binding.xml;


import org.staxnav.EnumElement;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Element implements EnumElement<Element>
{
   // Navigation Elements
   UNKNOWN(null),
   NODE_NAVIGATION("node-navigation"),
   PRIORITY("priority"),
   PAGE_NODES("page-nodes"),
   NODE("node"),
   @Deprecated
   URI("uri"),
   PARENT_URI("parent-uri"),
   LABEL("label"),
   START_PUBLICATION_DATE("start-publication-date"),
   END_PUBLICATION_DATE("end-publication-date"),
   VISIBILITY("visibility"),
   PAGE_REFERENCE("page-reference"),

   // Page elements
   PAGE_SET("page-set"),
   PAGE("page"),
   NAME("name"),
   SHOW_MAX_WINDOW("show-max-window"),

   // Portal config elements
   PORTAL_CONFIG("portal-config"),
   PORTAL_NAME("portal-name"),
   LOCALE("locale"),
   SKIN("skin"),
   PROPERTIES("properties"),
   PROPERTIES_ENTRY("entry"),
   PORTAL_LAYOUT("portal-layout"),

   // Common elements
   TITLE("title"),
   DESCRIPTION("description"),
   FACTORY_ID("factory-id"),
   ACCESS_PERMISSIONS("access-permissions"),
   EDIT_PERMISSION("edit-permission"),
   PORTLET_APPLICATION("portlet-application"),
   GADGET_APPLICATION("gadget-application"),
   CONTAINER("container"),
   PAGE_BODY("page-body"),
   APPLICATION_REF("application-ref"),
   PORTLET_REF("portlet-ref"),
   PORTLET("portlet"),
   GADGET_REF("gadget-ref"),
   GADGET("gadget"),
   THEME("theme"),
   SHOW_INFO_BAR("show-info-bar"),
   SHOW_APPLICATION_STATE("show-application-state"),
   SHOW_APPLICATION_MODE("show-application-mode"),
   ICON("icon"),
   WIDTH("width"),
   HEIGHT("height"),
   PREFERENCES("preferences"),
   PREFERENCE("preference"),
   PREFERENCE_VALUE("value"),
   PREFERENCE_READONLY("read-only")
   ;

   private final String name;

   Element(String name)
   {
      this.name = name;
   }

   @Override
   public String getLocalName()
   {
      return name;
   }
}
