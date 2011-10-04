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
package org.gatein.integration.jboss.as7;

import org.jboss.dmr.ModelNode;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemDescriptions
{
   static final String RESOURCE_NAME = GateInSubsystemDescriptions.class.getPackage().getName() + ".LocalDescriptions";

   public static ModelNode getSubsystemDescription(final Locale locale)
   {
      final ResourceBundle bundle = getResourceBundle(locale);

      final ModelNode node = new ModelNode();

      node.get(DESCRIPTION).set(bundle.getString("gatein.description"));
      node.get(HEAD_COMMENT_ALLOWED).set(true);
      node.get(TAIL_COMMENT_ALLOWED).set(true);
      node.get(NAMESPACE).set(GateInExtension.NAMESPACE);

      node.get(CHILDREN, "deployment-archives", DESCRIPTION).set(bundle.getString("gatein.deployment-archives.description"));
      node.get(CHILDREN, "deployment-archives", MIN_OCCURS).set(0);
      node.get(CHILDREN, "deployment-archives", MAX_OCCURS).set(1);
      node.get(CHILDREN, "deployment-archives", MODEL_DESCRIPTION);

      node.get(CHILDREN, "portlet-war-dependencies", DESCRIPTION).set(bundle.getString("gatein.portlet-war-dependencies.description"));
      node.get(CHILDREN, "portlet-war-dependencies", MIN_OCCURS).set(0);
      node.get(CHILDREN, "portlet-war-dependencies", MAX_OCCURS).set(1);
      node.get(CHILDREN, "portlet-war-dependencies", MODEL_DESCRIPTION);

      return node;
   }

   static ResourceBundle getResourceBundle(Locale locale)
   {
      if (locale == null)
      {
         locale = Locale.getDefault();
      }
      return ResourceBundle.getBundle(RESOURCE_NAME, locale);
   }

   public static ModelNode getSubsystemAddDescription(Locale locale)
   {
      final ResourceBundle bundle = getResourceBundle(locale);
      final ModelNode subsystem = new ModelNode();
      subsystem.get(DESCRIPTION).set(bundle.getString("gatein.add.description"));
      return subsystem;
   }
}
