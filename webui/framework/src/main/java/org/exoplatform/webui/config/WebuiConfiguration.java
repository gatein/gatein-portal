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

package org.exoplatform.webui.config;

import java.util.ArrayList;

import org.exoplatform.webui.config.metadata.ComponentMetaData;

/**
 * Created by The eXo Platform SAS May 4, 2006
 */
public class WebuiConfiguration {

    private ArrayList<String> annotationClasses;

    private ArrayList<ComponentMetaData> components;

    private Application application;

    public ArrayList<String> getAnnotationClasses() {
        return annotationClasses;
    }

    public ArrayList<ComponentMetaData> getComponents() {
        return components;
    }

    public Application getApplication() {
        return application;
    }

}
