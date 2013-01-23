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

package org.gatein.portal.impl.mop.ram;

import java.io.IOException;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.gatein.portal.mop.site.SiteType;
import org.json.JSONObject;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RamStore implements ComponentRequestLifecycle {

    /** . */
    final Store store = new Store();

    public RamStore() {
        Store init = store.open();
        String root = init.getRoot();
        init.addChild(root, SiteType.PORTAL.getName(), SiteType.PORTAL);
        init.addChild(root, SiteType.GROUP.getName(), SiteType.GROUP);
        init.addChild(root, SiteType.USER.getName(), SiteType.USER);
        init.merge();
    }

    @Override
    public void startRequest(ExoContainer container) {
        Tx.begin();
    }

    @Override
    public void endRequest(ExoContainer container) {
        Tx.end(true);
    }

    public void dump(Appendable appendable) throws IOException {
        dump("", store.getRoot(), appendable);
    }

    private void dump(String tab, String parent, Appendable appendable) throws IOException {
        Node node = store.getNode(parent);
        appendable.append(tab).append("{\n");
        appendable.append(tab).append("  id      : ").append(JSONObject.quote(parent)).append(",\n");
        appendable.append(tab).append("  name    : ").append(JSONObject.quote(node.getName())).append(",\n");
        appendable.append(tab).append("  state   : ").append(JSONObject.quote(node.getState().toString())).append(",\n");
        List<String> children = store.getChildren(parent);
        if (children.size() > 0) {
            appendable.append(tab).append("  children: [");
            String tab2 = tab + "  ";
            for (int i = 0;i < children.size();i++) {
                appendable.append("\n");
                dump(tab2, children.get(i), appendable);
                if (i < children.size() - 1) {
                    appendable.append(",");
                }
            }
            appendable.append("\n").append(tab).append("  ]\n");
        }
        appendable.append(tab).append("}");
    }
}
