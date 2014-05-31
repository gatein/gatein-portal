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

package org.exoplatform.portal.resource.config.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.exoplatform.portal.resource.SkinConfigParser;
import org.exoplatform.portal.resource.SkinService;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 * Sep 16, 2009
 */
public class ThemeTask implements SkinConfigTask {

    private String styleName;

    private List<String> themeNames;

    public ThemeTask() {
        this.themeNames = new ArrayList<String>();
    }

    private void bindingStyleName(Element element) {
        NodeList nodes = element.getElementsByTagName(SkinConfigParser.STYLE_NAME_TAG);
        if (nodes == null || nodes.getLength() < 1) {
            return;
        }
        String styleName = nodes.item(0).getFirstChild().getNodeValue();
        setStyleName(styleName);
    }

    private void bindingThemeNames(Element element) {
        NodeList nodes = element.getElementsByTagName(SkinConfigParser.THEME_NAME_TAG);
        if (nodes == null) {
            return;
        }
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            addThemeName(nodes.item(i).getFirstChild().getNodeValue());
        }
    }

    public void addThemeName(String _themeName) {
        // TODO: Check duplicated theme name
        this.themeNames.add(_themeName);
    }

    public void setStyleName(String _styleName) {
        this.styleName = _styleName;
    }

    public void binding(Element elemt) {
        bindingStyleName(elemt);
        bindingThemeNames(elemt);
    }

    public void execute(SkinService skinService, ServletContext scontext) {
        if (styleName == null || themeNames.size() < 1) {
            return;
        }
        skinService.addTheme(styleName, themeNames);
    }
}
