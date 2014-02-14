/*
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
package org.exoplatform.web.application.javascript;

import static org.exoplatform.web.application.javascript.JavascriptConfigParser.SCRIPT_RESOURCE_DESCRIPTORS_ATTR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;

import org.gatein.portal.controller.resource.script.ScriptResource;
import org.gatein.portal.controller.resource.script.StaticScriptResource;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptTask {

    private List<ScriptResourceDescriptor> descriptors = new ArrayList<ScriptResourceDescriptor>();
    private List<StaticScriptResource> staticScriptResources = new ArrayList<StaticScriptResource>();

    public List<ScriptResourceDescriptor> execute(JavascriptConfigService service, ServletContext scontext) {
        List<ScriptResourceDescriptor> finishedDescriptors = this.descriptors;
        /* make sure finishedDescriptors won't change or leak */
        this.descriptors = null;
        finishedDescriptors = Collections.unmodifiableList(finishedDescriptors);
        scontext.setAttribute(SCRIPT_RESOURCE_DESCRIPTORS_ATTR, finishedDescriptors);
        for (ScriptResourceDescriptor desc : finishedDescriptors) {
            String contextPath = null;
            if (desc.modules.size() > 0) {
                contextPath = desc.modules.get(0).getContextPath();
            }

            ScriptResource resource = service.scripts.addResource(desc.id, desc.fetchMode, desc.alias, desc.group, contextPath, desc.nativeAmd);
            if (resource != null) {
                for (Javascript module : desc.modules) {
                    module.addModuleTo(resource);
                }
                for (Locale locale : desc.getSupportedLocales()) {
                    resource.addSupportedLocale(locale);
                }
                for (DependencyDescriptor dependency : desc.dependencies) {
                    resource.addDependency(dependency.getResourceId(), dependency.getAlias(), dependency.getPluginResource());
                }
            }
        }

        for (StaticScriptResource staticScriptResource : staticScriptResources) {
            service.addStaticScriptResource(staticScriptResource);
        }

        return finishedDescriptors;
    }

    public void addDescriptor(ScriptResourceDescriptor desc) {
        if (descriptors == null) {
            throw new IllegalStateException("Cannot modify this task after execute() has been called.");
        }
        descriptors.add(desc);
    }

    public void addDescriptors(Collection<ScriptResourceDescriptor> descs) {
        if (descriptors == null) {
            throw new IllegalStateException("Cannot modify this task after execute() has been called.");
        }
        descriptors.addAll(descs);
    }

    /**
     * @param r
     */
    public void addStaticScriptResource(StaticScriptResource r) {
        this.staticScriptResources.add(r);
    }

    /**
     * Returns an umodifiable wrapper of {@link #descriptors}. For testing purposes.
     *
     * @return
     */
    public List<ScriptResourceDescriptor> getDescriptors() {
        return Collections.unmodifiableList(descriptors);
    }

}
