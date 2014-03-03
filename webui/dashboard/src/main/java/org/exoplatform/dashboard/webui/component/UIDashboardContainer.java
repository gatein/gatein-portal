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

package org.exoplatform.dashboard.webui.component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.StaleModelException;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Param;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

@ComponentConfig(template = "classpath:groovy/dashboard/webui/component/UIDashboardContainer.gtmpl", initParams = @ParamConfig(name = "ContainerConfigs", value = "classpath:conf/uiconf/dashboard/webui/container/ContainerConfig.groovy"))
public class UIDashboardContainer extends org.exoplatform.webui.core.UIContainer {

    /** Specify max number of columns in dashboard container Default value is 4 */
    public static final int MAX_COLUMN = 4;

    /** Column Type of Container */
    public static final String COLUMN_CONTAINER = "column";

    /** Row Type of Container */
    public static final String ROW_CONTAINER = "row";

    /** Root Type of Container */
    public static final String ROOT_CONTAINER = "dashboard";

    /**
    *
    */
    private List<SelectItemOption<String>> containerOptions;

    /**
     * Specify windowId of <code>UIPortlet</code> that contains <code>UIDashboardPortlet</code> which this belong to
     *
     * @see UIPortlet
     * @see org.exoplatform.dashboard.webui.component.DashboardParent
     */
    private String windowId;

    /** Preconfig template */
    private String containerTemplate = null;

    public static final String COLINDEX = "colIndex";

    public static final String ROWINDEX = "rowIndex";

    public static String SAVE_FAIL = "UIDashboardContainer.saveFail";

    /**
     * Constructs new UIDashboardContainer which belongs to a UIDashboardPortlet
     *
     * @param initParams initial parameters
     * @throws Exception if can't create UIDashboardContainer
     * @see org.exoplatform.dashboard.webui.component.DashboardParent
     * @see InitParams
     */
    public UIDashboardContainer(final InitParams initParams) throws Exception {
        if (initParams == null) {
            return;
        }
        WebuiRequestContext context = RequestContext.getCurrentInstance();
        windowId = ((PortletRequestContext) context).getRequest().getWindowID();

        Param param = initParams.getParam("ContainerConfigs");
        containerOptions = (List<SelectItemOption<String>>) param.getMapGroovyObject(context);
        if (containerOptions == null) {
            return;
        }

        //
        UIContainer uicontainer = addChild(UIContainer.class, null, null);

        //
        Container container = createContainer(COLUMN_CONTAINER, null);

        //
        PortalDataMapper.toUIContainer(uicontainer, container);

        //
        setColumns(3);
    }

    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        DataStorage service = getApplicationComponent(DataStorage.class);

        // Get current ui portlet containing this dashboard
        UIPortlet currentUIPortlet = UIPortlet.getCurrentUIPortlet();

        // Get dashboard for merging
        Container dashboard = null;
        if (currentUIPortlet.getStorageId() != null) {
            dashboard = service.loadDashboard(currentUIPortlet.getStorageId());
        }

        //
        if (dashboard != null) {
            UIContainer uiRoot = getChild(UIContainer.class);

            // Clear the children created by default as we get state from the storage
            uiRoot.getChildren().clear();

            // Assemble the dashboard
            PortalDataMapper.toUIContainer(uiRoot, dashboard);
        }

        //
        super.processRender(context);
    }

    /**
     * Adds an UIGadget into UIDashboardContainer in specified position
     *
     * @param gadget UIGadget to add in UIDashboardContainer
     * @param col index of column to add
     * @param row index of row to add
     * @return <code>UIGadget</code> object which added in
     * @see UIGadget
     */
    public void addUIGadget(final UIGadget gadget, final int col, final int row) {
        UIContainer uiContainer = getColumn(col);
        List<UIComponent> children = uiContainer.getChildren();
        if (uiContainer == null || row < 0 || row > children.size()) {
            return;
        }
        gadget.setParent(uiContainer);
        children.add(row, gadget);
    }

    /**
     * Gets an UIGadget at specified position
     *
     * @param col index of column
     * @param row index of row
     * @return <code>null</code> if specified position doesn't exist <code>UIGadget</code> otherwise
     * @see UIGadget
     */
    public UIGadget getUIGadget(final int col, final int row) {
        UIContainer uiContainer = getColumn(col);
        if (uiContainer == null || row < 0 || row >= uiContainer.getChildren().size()) {
            return null;
        }
        return uiContainer.getChild(row);
    }

    /**
     * Gets an UIGadget which has applicationInstanceUniqueId_ according to specified Id
     *
     * @param gadgetId applicationInstanceUniqueId_ of UIGadget
     * @return <code>null</code> if UIGadget doesn't exist<br>
     *         <code>UIGadget<code> otherwise
     * @see UIGadget
     */
    public UIGadget getUIGadget(final String gadgetId) {
        List<UIContainer> columns = getColumns();
        for (int iCol = 0; iCol < getColumns().size(); iCol++) {
            for (int iRow = 0; iRow < columns.get(iCol).getChildren().size(); iRow++) {
                UIGadget gadget = (UIGadget) columns.get(iCol).getChild(iRow);
                if (gadgetId.equals(gadget.getId())) {
                    return gadget;
                }
            }
        }
        return null;
    }

    /**
     * Removes an UIGadget belongs to this which has specified applicationInstanceUniqueId_
     *
     * @param gadgetId applicationInstanceUniqueId_ of UIGadget
     * @return <code>UIGadget</code> which deleted<br>
     *         <code>null</code> otherwise
     * @see UIGadget
     */
    public UIGadget removeUIGadget(final String gadgetId) {
        UIGadget gadget = getUIGadget(gadgetId);
        if (gadget != null) {
            UIContainer uiContainer = gadget.getParent();
            gadget.setParent(null);
            uiContainer.getChildren().remove(gadget);
        }
        return gadget;
    }

    /**
     * Removes an UIGadget at specified position
     *
     * @param col index of the column
     * @param row index of the row
     * @return <code>UIGadget</code> which deleted<br>
     *         <code>null</code> otherwise
     * @see UIGadget
     */
    public UIGadget removeUIGadget(final int col, final int row) {
        UIGadget gadget = getUIGadget(col, row);
        if (gadget != null) {
            removeUIGadget(gadget.getId());
        }
        return gadget;
    }

    /**
     * Moves an UIGadget which has specified applicationInstanceUniqueId_ to specified position
     *
     * @param gadgetId applicationInstanceUniqueId_ of UIGadget
     * @param col index of destination column
     * @param row index of destination row
     * @see UIGadget
     */
    public void moveUIGadget(final String gadgetId, final int col, final int row) {
        UIGadget gadget = removeUIGadget(gadgetId);
        if (gadget == null) {
            return;
        }
        addUIGadget(gadget, col, row);
    }

    /**
     * Gets an UIContainer representation of a column at specified index
     *
     * @param col index of column
     * @return <code>UIContainer</code> if the column exist<br>
     *         <code>null</code> otherwise
     * @see UIContainer
     */
    public UIContainer getColumn(final int col) {
        if (col < 0 || col > getColumns().size()) {
            return null;
        }
        return getColumns().get(col);
    }

    /**
     * Tests if this UIDashboardContainer has UIGadget
     *
     * @return <code>false</code> if and only if this UIDashboardContainer has no UIGadget<br>
     *         <code>true</code> otherwise
     */
    public boolean hasUIGadget() {
        UIGadget gadget = findFirstComponentOfType(UIGadget.class);
        return (gadget != null);
    }

    /**
     * Sets total of columns of this UIDashboardContainer, total of columns is between 1 and <tt>MAX_COLUMN</tt>
     *
     * @param num total of columns
     * @return <code>null</code> if totals of columns is less than 1 or greater than <tt>MAX_COLUMN</tt> this
     *         <code>UIDashboardContainer</code> otherwise
     * @throws Exception if this UIDashboardContainer can not create new UIComponent
     */
    public UIDashboardContainer setColumns(final int num) throws Exception {
        if (num < 1 || num > MAX_COLUMN) {
            return null;
        }

        UIContainer uiRoot = findFirstComponentOfType(UIContainer.class);
        List<UIContainer> columns = getColumns();
        int colSize = columns.size();

        if (num < colSize) {
            for (int i = num; i < colSize; i++) {
                UIContainer tempCol = columns.get(i);
                List<UIComponent> components = new ArrayList<UIComponent>();
                for (UIComponent component : tempCol.getChildren()) {
                    columns.get(num - 1).addChild(component);
                    components.add(component);
                }
                uiRoot.removeChildById(tempCol.getId());
                for (UIComponent component : components) {
                    component.setParent(columns.get(num - 1));
                }
            }
        } else {
            if (num > colSize) {
                do {
                    UIContainer uiContainer = createUIComponent(UIContainer.class, null, null);
                    PortalDataMapper.toUIContainer(uiContainer, createContainer(ROW_CONTAINER, "UIColumn-" + colSize));
                    uiRoot.addChild(uiContainer);
                    colSize++;
                } while (num > colSize);
            }
        }
        return this;
    }

    /**
     * Returns a <tt>List</tt> of the UIContainers of this UIDashboardContainer which representation of list of columns.
     *
     * @return <code>List</code> of UIContainer
     * @see List
     * @see UIContainer
     */
    public List<UIContainer> getColumns() {
        List<UIContainer> list = new ArrayList<UIContainer>();
        UIContainer uiRoot = findFirstComponentOfType(UIContainer.class);
        uiRoot.findComponentOfType(list, UIContainer.class);
        if (list.size() > 0 && list.contains(uiRoot)) {
            list.remove(uiRoot);
        }
        return list;
    }

    /**
     * Creates a Container that representation of row or column in this UIDashboardContainer
     *
     * @param type type of Container, that is <tt>ROW_CONTAINER</tt> or <tt>COLUMN_CONTAINER</tt>
     * @param id id of Container
     * @return <code>Container</code> if {@link UIDashboardContainer} has specified type <br>
     *         <code>null</code> otherwise
     * @throws Exception if <code>toContainer(String)</code> method throws an Exception
     * @see Container
     * @see UIDashboardContainer#toContainer(String)
     */
    public Container createContainer(final String type, final String id) throws Exception {
        for (SelectItemOption<String> item : containerOptions) {
            if (item.getLabel().equals(type)) {
                Container container = toContainer(item.getValue());
                if (id != null) {
                    container.setId(id);
                }
                return container;
            }
        }
        return null;
    }

    /**
     * Creates a Container from a xml template
     *
     * @param xml template of Container
     * @return <code>null</code> if template is not suitable for {@link Container}<br />
     *         <code>Container</code> otherwise
     * @throws Exception if have problems in unmarshal process
     * @see IUnmarshallingContext#unmarshalDocument(java.io.InputStream, String)
     */
    private Container toContainer(final String xml) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        return (Container) uctx.unmarshalDocument(is, null);
    }

    /**
     * Get windowId of <code>UIPortlet</code>
     *
     * @return a String represents windowId of portlet
     * @see UIPortlet
     */
    public String getWindowId() {
        return this.windowId;
    }

    /**
    *
    */
    public void setContainerTemplate(String template) {
        this.containerTemplate = template;
    }

    /**
    *
    */
    public String getContainerTemplate() {
        return this.containerTemplate;
    }

    /**
     * Saves all <tt>UIComponent</tt> of this <tt>UIDashboardContainer</tt> to database
     *
     * @throws Exception
     */
    public void save() throws Exception {
        UIContainer uiRoot = findFirstComponentOfType(UIContainer.class);

        if (!((UIDashboard) getParent()).canEdit()) {
            return;
        }

        // Get current ui portlet containing this dashboard
        UIPortlet currentUIPortlet = UIPortlet.getCurrentUIPortlet();

        // We save only if we have an existing storage id
        // we would not have a storage id in the case where
        // the dashboard portlet is initially dropped in the
        // page
        if (currentUIPortlet.getStorageId() != null) {
            // Build dashboard
            Dashboard dashboard = new Dashboard(currentUIPortlet.getStorageId());

            // Assemble the dashboard
            PortalDataMapper.toContainer(dashboard, uiRoot);

            // Get data storage
            DataStorage service = getApplicationComponent(DataStorage.class);

            // Save
            try {
                service.saveDashboard(dashboard);
            } catch (Exception e) {
                UIPortalApplication uiApp = Util.getUIPortalApplication();
                uiApp.refreshCachedUI();
                WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
                context.getUIApplication().addMessage(
                        new ApplicationMessage("UIDashboard.msg.StaleData", null, ApplicationMessage.ERROR));
                context.setAttribute(SAVE_FAIL, true);
            }
        }
    }

    public static class AddNewGadgetActionListener extends EventListener<UIDashboard> {
        @Override
        public final void execute(final Event<UIDashboard> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIDashboard uiDashboard = event.getSource();
            if (!uiDashboard.canEdit()) {
                return;
            }
            int col = Integer.parseInt(context.getRequestParameter(COLINDEX));
            int row = Integer.parseInt(context.getRequestParameter(ROWINDEX));
            String objectId = context.getRequestParameter(UIComponent.OBJECTID);

            /**
             * Gadgets IDs have "/" characters.
             * We need to workaround them in the markup to be W3C compliant.
             *
             * "_slash_" -> "/"
             *
             * @see UIDashboardSelectContainer#getGadgetsOfCategory(org.exoplatform.application.registry.ApplicationCategory)
             */
            objectId = objectId.replaceAll("_slash_", "/");

            ApplicationRegistryService service = uiDashboard.getApplicationComponent(ApplicationRegistryService.class);
            Application application = service.getApplication(objectId);
            if (application == null) {
                UIApplication uiApplication = context.getUIApplication();
                uiApplication.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
                context.setAttribute(UIDashboard.APP_NOT_EXIST, true);
                return;
            }
            UIGadget uiGadget = event.getSource().createUIComponent(context, UIGadget.class, null, null);
            uiGadget.setState(new TransientApplicationState<Gadget>(application.getApplicationName()));
            UIDashboardContainer uiDashboardContainer = uiDashboard.getChild(UIDashboardContainer.class);
            uiDashboardContainer.addUIGadget(uiGadget, col, row);
            uiDashboardContainer.save();
            context.addUIComponentToUpdateByAjax(uiDashboardContainer);
        }
    }

    public static class MoveGadgetActionListener extends EventListener<UIDashboard> {
        @Override
        public final void execute(final Event<UIDashboard> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIDashboard uiDashboard = event.getSource();
            if (!uiDashboard.canEdit()) {
                return;
            }
            UIDashboardContainer uiDashboardContainer = uiDashboard.getChild(UIDashboardContainer.class);
            int col = Integer.parseInt(context.getRequestParameter(COLINDEX));
            int row = Integer.parseInt(context.getRequestParameter(ROWINDEX));
            String objectId = context.getRequestParameter(OBJECTID);

            uiDashboardContainer.moveUIGadget(objectId, col, row);
            uiDashboardContainer.save();
            if (context.getAttribute(SAVE_FAIL) != null) {
                return;
            }
            Util.getPortalRequestContext().setResponseComplete(true);
        }
    }

    public static class DeleteGadgetActionListener extends EventListener<UIDashboard> {
        @Override
        public final void execute(final Event<UIDashboard> event) throws Exception {
            UIDashboard uiDashboard = event.getSource();
            if (!uiDashboard.canEdit()) {
                return;
            }

            WebuiRequestContext context = event.getRequestContext();
            String objectId = context.getRequestParameter(OBJECTID);
            UIDashboardContainer uiDashboardContainer = uiDashboard.getChild(UIDashboardContainer.class);
            uiDashboardContainer.removeUIGadget(objectId);
            boolean isMaximized = false;
            if (uiDashboard.getMaximizedGadget() != null && uiDashboard.getMaximizedGadget().getId().equals(objectId)) {
                uiDashboard.setMaximizedGadget(null);
                isMaximized = true;
            }
            uiDashboardContainer.save();
            if (!isMaximized && context.getAttribute(SAVE_FAIL) == null) {
                Util.getPortalRequestContext().setResponseComplete(true);
            }
        }
    }

}
