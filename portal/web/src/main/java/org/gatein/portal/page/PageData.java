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
package org.gatein.portal.page;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * @author Julien Viet
 */
public class PageData {

    /** . */
    public final String path;

    /** The page render parameters. */
    private Map<QName, String[]> parameters;

    public PageData(String path) {
        this.path = path;
        this.parameters = null;
    }

    public PageData(PageData state) {
        this.path = state.path;
        this.parameters = state.parameters != null ? new HashMap<QName, String[]>(state.parameters) : null;
    }

    public Map<QName, String[]> getParameters() {
        return parameters;
    }

    private void setParameter(QName name, String[] value) {
        if (value.length == 0) {
            if (parameters != null) {
                parameters.remove(name);
            }
        } else {
            if (parameters == null) {
                parameters = new HashMap<QName, String[]>();
            }
            parameters.put(name, value);
        }
    }

    public void apply(Iterable<Map.Entry<QName, String[]>> changes) {
        for (Map.Entry<QName, String[]> change : changes) {
            setParameter(change.getKey(), change.getValue());
        }
    }

    public void setParameters(Map<QName, String[]> parameters) {
        this.parameters = parameters;
    }
}
