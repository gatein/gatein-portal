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

import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.application.WebuiRequestContext;

List categories = new ArrayList(); 
WebuiRequestContext contextres = WebuiRequestContext.getCurrentInstance();
    
SelectItemCategory normalPageConfigs = new SelectItemCategory("normalPageConfigs") ;
categories.add(normalPageConfigs);
normalPageConfigs.addSelectItemOption(new SelectItemOption("normalPage.EmptyLayout", "empty", "EmptyLayout"));
normalPageConfigs.addSelectItemOption(new SelectItemOption("normalPage.DashboardLayout", "dashboard", "DashboardLayout"));

SelectItemCategory columnPageConfigs = new SelectItemCategory("columnPageConfigs") ;
categories.add(columnPageConfigs);  
columnPageConfigs.addSelectItemOption(new SelectItemOption("columnPage.TwoColumnsLayout", "two-columns", "TwoColumnsLayout"));
columnPageConfigs.addSelectItemOption(new SelectItemOption("columnPage.ThreeColumnsLayout", "three-columns", "ThreeColumnsLayout"));

SelectItemCategory rowPageConfigs = new SelectItemCategory("rowPageConfigs") ;
categories.add(rowPageConfigs); 
rowPageConfigs.addSelectItemOption(new SelectItemOption("rowPage.TwoRowsLayout", "two-rows", "TwoRowsLayout"));
rowPageConfigs.addSelectItemOption(new SelectItemOption("rowPage.ThreeRowsLayout", "three-rows", "ThreeRowsLayout"));

SelectItemCategory tabsPageConfigs = new SelectItemCategory("tabsPageConfigs") ;
categories.add(tabsPageConfigs) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption("tabsPage.TwoTabsLayout", "two-tabs", "TwoTabsLayout")) ;
tabsPageConfigs.addSelectItemOption(new SelectItemOption("tabsPage.ThreeTabsLayout", "three-tabs", "ThreeTabsLayout")) ;

SelectItemCategory mixPageConfigs = new SelectItemCategory("mixPageConfigs") ;
categories.add(mixPageConfigs); 
mixPageConfigs.addSelectItemOption(new SelectItemOption("mixPage.TwoColumnsOneRowLayout", "two-columns-one-row", "TwoColumnsOneRowLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption("mixPage.OneRowTwoColumnsLayout", "one-row-two-columns", "OneRowTwoColumnsLayout"));
mixPageConfigs.addSelectItemOption(new SelectItemOption("mixPage.ThreeRowsTwoColumnsLayout", "three-rows-two-columns", "ThreeRowsTwoColumnsLayout"));

return categories;

