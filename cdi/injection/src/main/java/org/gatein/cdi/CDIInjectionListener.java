package org.gatein.cdi;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.portlet.container.PortletApplication;
import org.gatein.pc.portlet.container.managed.LifeCycleStatus;
import org.gatein.pc.portlet.container.managed.ManagedObject;
import org.gatein.pc.portlet.container.managed.ManagedObjectAddedEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectLifeCycleEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEventListener;
import org.gatein.pc.portlet.container.managed.ManagedPortletContainer;
import org.gatein.pc.portlet.container.managed.ManagedPortletFilter;
import org.gatein.pc.portlet.impl.jsr168.PortletFilterImpl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.portlet.Portlet;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.EventFilter;
import javax.portlet.filter.PortletFilter;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class CDIInjectionListener implements ManagedObjectRegistryEventListener {
    private final Logger log = LoggerFactory.getLogger(CDIInjectionListener.class);

    private static final String BEAN_MGR_ATTRIBUTE = "javax.enterprise.inject.spi.BeanManager";
    private static final String SERVLET_BEAN_MGR_ATTRIBUTE = "org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager";

    private Map<String, CDIMetaData> cdiMetaDataMap = new ConcurrentHashMap<String, CDIMetaData>();

    private List<Class<? extends PortletFilter>> filterClasses = new ArrayList<Class<? extends PortletFilter>>();

    public CDIInjectionListener() {
        filterClasses.add(ActionFilter.class);
        filterClasses.add(EventFilter.class);
        filterClasses.add(RenderFilter.class);
        filterClasses.add(ResourceFilter.class);
    }

    @Override
    public void onEvent(ManagedObjectRegistryEvent event) {

        if (event instanceof ManagedObjectAddedEvent) {
            // Track whether a portletContainer or portletFilter needs CDI injection

            ManagedObject managedObject = ((ManagedObjectAddedEvent) event).getManagedObject();

            if (managedObject instanceof ManagedPortletContainer) {

                ManagedPortletContainer managedPortletContainer = (ManagedPortletContainer) managedObject;
                PortletApplication portletApp = managedPortletContainer.getManagedPortletApplication().getPortletApplication();

                createMetaData(managedPortletContainer.getId(), portletApp);

            } else if (managedObject instanceof ManagedPortletFilter) {

                ManagedPortletFilter managedPortletFilter = (ManagedPortletFilter) managedObject;
                PortletApplication portletApp = managedPortletFilter.getManagedPortletApplication().getPortletApplication();

                createMetaData(managedPortletFilter.getId(), portletApp);
            }

        } else if (event instanceof ManagedObjectLifeCycleEvent) {

            ManagedObjectLifeCycleEvent lifeCycleEvent = (ManagedObjectLifeCycleEvent) event;
            ManagedObject managedObject = lifeCycleEvent.getManagedObject();
            LifeCycleStatus status = lifeCycleEvent.getStatus();

            if (managedObject instanceof ManagedPortletContainer) {

                if (LifeCycleStatus.STARTED == status || LifeCycleStatus.INITIALIZED == status) {
                    return;
                }

                ManagedPortletContainer managedPortletContainer = (ManagedPortletContainer) managedObject;
                CDIMetaData cdiMetaData = cdiMetaDataMap.get(managedPortletContainer.getId());

                if (!cdiMetaData.cdiInjectionEnabled) {
                    return;
                }

                Portlet portlet = managedPortletContainer.getPortletInstance();

                if (null != portlet) {
                    if (null != portlet.getClass() && "javax.portlet.faces.GenericFacesPortlet".equals(portlet.getClass().getName())) {
                        // Only perform injection on non JSF portlets
                        cdiMetaData.cdiInjectionEnabled = false;
                        cdiMetaDataMap.put(cdiMetaData.key, cdiMetaData);
                        return;
                    }

                    PortletApplication portletApp = managedPortletContainer.getManagedPortletApplication().getPortletApplication();

                    if (!cdiMetaData.injectionPerformed) {
                        performInjection(portlet, cdiMetaData, portletApp.getContext().getServletContext());
                    } else {
                        performCleanup(portlet, cdiMetaData, portletApp.getContext().getServletContext());
                    }
                }
            } else if (managedObject instanceof ManagedPortletFilter) {

                if (LifeCycleStatus.INITIALIZED == status) {
                    return;
                }

                ManagedPortletFilter managedPortletFilter = (ManagedPortletFilter) managedObject;
                CDIMetaData cdiMetaData = cdiMetaDataMap.get(managedPortletFilter.getId());

                if (!cdiMetaData.cdiInjectionEnabled) {
                    return;
                }

                PortletFilterImpl portletFilterImpl = (PortletFilterImpl) managedPortletFilter.getPortletFilter();
                PortletFilter portletFilterInstance;

                for (Class type : filterClasses) {
                    portletFilterInstance = (PortletFilter) portletFilterImpl.instance(type);

                    if (null != portletFilterInstance) {
                        PortletApplication portletApp = managedPortletFilter.getManagedPortletApplication().getPortletApplication();

                        if (LifeCycleStatus.STARTED == status && !cdiMetaData.injectionPerformed) {
                            performInjection(portletFilterInstance, cdiMetaData, portletApp.getContext().getServletContext());
                        } else if (LifeCycleStatus.CREATED == status && cdiMetaData.injectionPerformed) {
                            performCleanup(portletFilterInstance, cdiMetaData, portletApp.getContext().getServletContext());
                        }

                        break;
                    }
                }
            }
        }
    }

    private void createMetaData(String id, PortletApplication portletApp) {
        CDIMetaData metaData = new CDIMetaData();
        metaData.key = id;

        if (null != portletApp.getContext().getServletContext().getAttribute(BEAN_MGR_ATTRIBUTE)) {
            metaData.cdiInjectionEnabled = true;
        } else {
            Object beanManager = portletApp.getContext().getServletContext().getAttribute(SERVLET_BEAN_MGR_ATTRIBUTE);
            if (null != beanManager) {
                metaData.cdiInjectionEnabled = true;
                portletApp.getContext().getServletContext().setAttribute(BEAN_MGR_ATTRIBUTE, beanManager);
            }
        }

        cdiMetaDataMap.put(id, metaData);
    }

    private void performInjection(Object instance, CDIMetaData metaData, ServletContext servletContext) {
        // Perform CDI injection
        Object beanManagerObject = servletContext.getAttribute(BEAN_MGR_ATTRIBUTE);

        if (null == beanManagerObject) {
            log.error("Unable to retrieve BeanManager from ServletContext");
            return;
        }

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

        BeanManager beanManager = (BeanManager) beanManagerObject;
        CreationalContext creationalContext = beanManager.createCreationalContext(null);
        InjectionTarget injectionTarget = beanManager.createInjectionTarget(beanManager.createAnnotatedType(instance.getClass()));
        injectionTarget.inject(instance, creationalContext);

        Thread.currentThread().setContextClassLoader(oldCL);

        metaData.injectionPerformed = true;
        metaData.creationalContext = creationalContext;
        metaData.injectionTarget = injectionTarget;

        cdiMetaDataMap.put(metaData.key, metaData);
    }

    private void performCleanup(Object instance, CDIMetaData metaData, ServletContext servletContext) {
        // Perform CDI cleanup
        InjectionTarget injectionTarget = metaData.injectionTarget;
        CreationalContext creationalContext = metaData.creationalContext;

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

        if (null != injectionTarget) {
            injectionTarget.dispose(instance);
            metaData.injectionTarget = null;
        }

        if (null != creationalContext) {
            creationalContext.release();
            metaData.creationalContext = null;
        }

        Thread.currentThread().setContextClassLoader(oldCL);

        metaData.injectionPerformed = false;
        cdiMetaDataMap.put(metaData.key, metaData);
    }

    private class CDIMetaData {
        private String key;
        private boolean cdiInjectionEnabled = false;
        private boolean injectionPerformed = false;
        private InjectionTarget injectionTarget;
        private CreationalContext creationalContext;
    }
}
