/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.exoplatform.web.application.javascript;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 * @version $Id$
 *
 */
public class JavascriptUnregisterTask {

    private List<ScriptResourceDescriptor> descriptors;

    public JavascriptUnregisterTask() {
        descriptors = new ArrayList<ScriptResourceDescriptor>();
    }

    public void execute(JavascriptConfigService service, ServletContext scontext) {
        for (ScriptResourceDescriptor desc : descriptors) {
            service.scripts.removeResource(desc.id, scontext.getContextPath());
        }
    }

    public void addDescriptor(ScriptResourceDescriptor desc) {
        descriptors.add(desc);
    }
}
