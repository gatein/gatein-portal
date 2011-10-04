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

import org.jboss.modules.ModuleIdentifier;
import org.jboss.msc.service.ServiceName;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInExtensionConfiguration
{
   private Set<ModuleIdentifier> extModules;

   private ModuleIdentifier earModule = ModuleIdentifier.create("deployment.gatein.ear");

   private final List<ServiceName> childWars = new LinkedList<ServiceName>();

   private final List<ServiceName> childSubUnits = new LinkedList<ServiceName>();

   public GateInExtensionConfiguration()
   {
      Set<ModuleIdentifier> set = new LinkedHashSet<ModuleIdentifier>();
      set.add(ModuleIdentifier.create("deployment.gatein-sample-extension.ear"));
      set.add(ModuleIdentifier.create("deployment.gatein-sample-portal.ear"));
      set.add(ModuleIdentifier.create("deployment.gatein-sample-skin.war"));
      //set.add(ModuleIdentifier.create("deployment.gatein-wsrp-integration.ear"));
      setGateInExtModules(Collections.unmodifiableSet(set));
   }

   public Set<ModuleIdentifier> getGateInExtModules()
   {
      return extModules;
   }

   public void setGateInExtModules(Set<ModuleIdentifier> modules)
   {
      this.extModules = modules;
   }

   public ModuleIdentifier getGateInEarModule()
   {
      return earModule;
   }

   public void setGateInEarModule(ModuleIdentifier modules)
   {
      this.earModule = modules;
   }

   public List<ServiceName> getChildWars()
   {
      return childWars;
   }

   public List<ServiceName> getChildSubUnits()
   {
      return childSubUnits;
   }

   public List<String> getChildSubUnitComponentPrefixes()
   {
      LinkedList<String> ret = new LinkedList<String>();
      for (ServiceName name : childSubUnits)
      {
         ret.add(name.getCanonicalName() + ".component.");
      }
      return ret;
   }
}
