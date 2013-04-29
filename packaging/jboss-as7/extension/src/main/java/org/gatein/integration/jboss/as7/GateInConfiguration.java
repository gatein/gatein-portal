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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.gatein.integration.jboss.as7.deployment.GateInEarKey;
import org.gatein.integration.jboss.as7.deployment.GateInExtKey;
import org.gatein.integration.jboss.as7.deployment.PortletWarKey;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;
import org.jboss.msc.service.ServiceName;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInConfiguration {
    private static final String DEPLOYMENT_SUFFIX = "deployment.";

    private Set<ModuleIdentifier> extModules = new HashSet<ModuleIdentifier>();

    private Set<ModuleDependency> portletWarDependencies = new LinkedHashSet<ModuleDependency>();

    private ModuleIdentifier earModule;

    private final List<ServiceName> childWars = new ArrayList<ServiceName>();

    private final List<ServiceName> childSubUnits = new ArrayList<ServiceName>();

    final ModuleLoader moduleLoader = Module.getBootModuleLoader();

    GateInConfiguration() {
    }

    public synchronized void addDeploymentArchive(String archive, boolean main) {
        String moduleId = DEPLOYMENT_SUFFIX + archive;
        extModules.add(ModuleIdentifier.create(moduleId));
        if (main)
            earModule = ModuleIdentifier.create(moduleId);
    }

    public synchronized void addPortletWarDependency(String dependency, boolean importSvcs) {
        String[] parts = parseNameSlotPair(dependency);
        portletWarDependencies.add(new ModuleDependency(moduleLoader, ModuleIdentifier.create(parts[0], parts[1]), false,
                false, importSvcs, true));
    }

    public synchronized Set<ModuleIdentifier> getGateInExtModules() {
        return Collections.unmodifiableSet(new HashSet<ModuleIdentifier>(extModules));
    }

    /**
     * Returns a list of portal extensions deployment archives' file names.
     *
     * @return list of file names
     */
    public synchronized Set<String> getGateInExtNames() {
        Set<String> ret = new HashSet<String>();
        for (ModuleIdentifier module : extModules) {
            ret.add(module.getName().substring(DEPLOYMENT_SUFFIX.length()));
        }
        return Collections.unmodifiableSet(ret);
    }

    public synchronized Set<ModuleDependency> getPortletWarDependencies() {
        return Collections.unmodifiableSet(new LinkedHashSet<ModuleDependency>(portletWarDependencies));
    }

    public ModuleIdentifier getGateInEarModule() {
        return earModule;
    }

    /**
     * Returns a file name of the gatein.ear or equivalent main portal archive.
     *
     * @return deployment archive file name
     */
    public synchronized String getGateInEarName() {
        return earModule != null ? earModule.getName().substring(DEPLOYMENT_SUFFIX.length()) : null;
    }

    /**
     * Clear all deployment archives info set via {@link #addDeploymentArchive(String,boolean)}
     */
    public synchronized void clearDeploymentArchives() {
        earModule = null;
        extModules.clear();
    }

    public synchronized List<ServiceName> getChildWars() {
        return Collections.unmodifiableList(new ArrayList(childWars));
    }

    public synchronized void addChildWar(ServiceName deploymentServiceName) {
        childWars.add(deploymentServiceName);
    }

    public synchronized List<ServiceName> getChildSubUnits() {
        return Collections.unmodifiableList(new ArrayList(childSubUnits));
    }

    public synchronized List<String> getChildSubUnitComponentPrefixes() {
        LinkedList<String> ret = new LinkedList<String>();
        for (ServiceName name : childSubUnits) {
            ret.add(name.getCanonicalName() + ".component.");
        }
        return ret;
    }

    public synchronized void addChildSubUnit(ServiceName serviceName) {
        childSubUnits.add(serviceName);
    }

    private static String[] parseNameSlotPair(String name) {
        String[] parts = name.split(":");
        if (parts.length == 2)
            return parts;
        return new String[] { parts[0], null };
    }

    public static boolean isGateInArchive(DeploymentUnit du) {
        return du.hasAttachment(GateInEarKey.KEY) || du.hasAttachment(GateInExtKey.KEY);
    }

    public static boolean isGateInArchiveOrSubDeployment(DeploymentUnit du) {
        boolean ret = du.hasAttachment(GateInEarKey.KEY) || du.hasAttachment(GateInExtKey.KEY);
        if (ret) {
            return true;
        }
        if (du.getParent() != null) {
            return isGateInArchive(du.getParent());
        }
        return false;
    }

    public static boolean isPortletArchive(DeploymentUnit du) {
        return du.hasAttachment(PortletWarKey.INSTANCE);
    }

    public static boolean isNonGateInPortletArchive(DeploymentUnit du) {
        return !isGateInArchive(du) && isPortletArchive(du);
    }

    public static boolean isGateInOrPortletArchive(DeploymentUnit du) {
        return isGateInArchive(du) || isPortletArchive(du);
    }
}
