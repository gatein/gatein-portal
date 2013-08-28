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

package org.exoplatform.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;
import org.gatein.common.http.QueryStringParser;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picocontainer.Startable;

/**
 * The WebAppController is the entry point of the GateIn service.
 */
@Managed
@ManagedDescription("The portal controller")
@NameTemplate({ @Property(key = "view", value = "portal"), @Property(key = "service", value = "controller") })
@RESTEndpoint(path = "portalcontroller")
public class WebAppController implements Startable {

    /** . */
    public static final QualifiedName HANDLER_PARAM = QualifiedName.create("gtn", "handler");

    /** . */
    protected static Logger log = LoggerFactory.getLogger(WebAppController.class);

    /** . */
    private final HashMap<String, Object> attributes_;

    /** . */
    private volatile HashMap<String, Application> applications_;

    /** . */
    private final HashMap<String, WebRequestHandler> handlers;

    /** . */
    private final AtomicReference<Router> routerRef;

    /** . */
    private final AtomicReference<String> configurationPathRef;

    /**
     * The WebAppControler along with the PortalRequestHandler defined in the init() method of the PortalController servlet
     * (controller.register(new PortalRequestHandler())) also add the CommandHandler object that will listen for the incoming
     * /command path in the URL.
     *
     * @param params the init params
     * @throws Exception any exception
     */
    public WebAppController(InitParams params) throws Exception {
        // Get router config
        ValueParam routerConfig = params.getValueParam("controller.config");
        if (routerConfig == null) {
            throw new IllegalArgumentException("No router param defined");
        }
        String configurationPath = routerConfig.getValue();

        //
        this.applications_ = new HashMap<String, Application>();
        this.attributes_ = new HashMap<String, Object>();
        this.handlers = new HashMap<String, WebRequestHandler>();
        this.routerRef = new AtomicReference<Router>();
        this.configurationPathRef = new AtomicReference<String>(configurationPath);

        //
        reloadConfiguration();
    }

    public Object getAttribute(String name, Object value) {
        return attributes_.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Application> T getApplication(String appId) {
        return (T) applications_.get(appId);
    }

    public List<Application> getApplicationByType(String type) {
        List<Application> applications = new ArrayList<Application>();
        for (Application app : applications_.values()) {
            if (app.getApplicationType().equals(type))
                applications.add(app);
        }
        return applications;
    }

    public synchronized void removeApplication(String appId) {
        applications_.remove(appId);
    }

    @Managed
    @ManagedDescription("The configuration path")
    public String getConfigurationPath() {
        return String.valueOf(configurationPathRef.get());
    }

    @Managed
    @ManagedDescription("Load the controller configuration")
    @Impact(ImpactType.WRITE)
    public void loadConfiguration(@ManagedDescription("The configuration path") @ManagedName("path") String path)
            throws IOException, RouterConfigException {
        File f = new File(path);
        if (!f.exists()) {
            throw new MalformedURLException("Could not resolve path " + path);
        }
        if (!f.isFile()) {
            throw new MalformedURLException("Could not resolve path " + path + " to a valid file");
        }
        loadConfiguration(f.toURI().toURL());
        configurationPathRef.set(path);
    }

    private void loadConfiguration(URL url) throws RouterConfigException, IOException {
        log.info("Loading router configuration " + url);
        InputStream in = url.openStream();
        try {
            ControllerDescriptor routerDesc = new DescriptorBuilder().build(in);
            Router router = new Router(routerDesc);
            routerRef.set(router);
        } finally {
            Safe.close(in);
        }
    }

    @Managed
    @ManagedDescription("Reload the controller configuration")
    @Impact(ImpactType.WRITE)
    public void reloadConfiguration() throws RouterConfigException, IOException {
        log.info("Loading router configuration " + configurationPathRef.get());
        loadConfiguration(configurationPathRef.get());
    }

    @Managed
    @ManagedDescription("Enumerates the routes found for the specified request")
    @Impact(ImpactType.READ)
    public String findRoutes(
            @ManagedDescription("The request uri relative to the web application") @ManagedName("uri") String uri) {
        Router router = routerRef.get();
        if (router != null) {
            Map<String, String[]> parameters;
            String path;
            int pos = uri.indexOf('?');
            if (pos != -1) {
                parameters = QueryStringParser.getInstance().parseQueryString(uri.substring(pos + 1));
                path = uri.substring(0, pos);
            } else {
                parameters = Collections.emptyMap();
                path = uri;
            }

            //
            List<Map<QualifiedName, String>> results = new ArrayList<Map<QualifiedName, String>>();
            Iterator<Map<QualifiedName, String>> matcher = router.matcher(path, parameters);
            while (matcher.hasNext()) {
                Map<QualifiedName, String> match = matcher.next();
                results.add(match);
            }

            //
            return results.toString();
        } else {
            throw new IllegalStateException("No route currently configured");
        }
    }

    /**
     * Add application (portlet, gadget) to the global application map if and only if it has not been registered yet.
     *
     * @param <T>
     * @param app
     * @return
     */
    public <T extends Application> T addApplication(T app) {
        Application result = getApplication(app.getApplicationId());

        // Double-check block
        if (result == null) {
            synchronized (this) {
                result = getApplication(app.getApplicationId());
                if (result == null) {
                    HashMap<String, Application> temporalApplicationsMap = new HashMap<String, Application>(applications_);
                    temporalApplicationsMap.put(app.getApplicationId(), app);
                    this.applications_ = temporalApplicationsMap;
                    result = app;
                }
            }
        }

        return (T) result;
    }

    /**
     * Register an handler as a component plugin, this method is invoked by the kernel with reflection.
     *
     * @param handler the handler
     * @throws Exception any exception
     */
    public void register(WebRequestHandler handler) {
        handlers.put(handler.getHandlerName(), handler);
    }

    public void unregister(String[] paths) {
        for (String path : paths) {
            WebRequestHandler handler = handlers.remove(path);
            handler.onDestroy(this);
        }
    }

    public void onHandlersInit(ServletConfig config) throws Exception {
        Collection<WebRequestHandler> hls = handlers.values();
        for (WebRequestHandler handler : hls) {
            handler.onInit(this, config);
        }
    }

    /**
     * <p>
     * This is the first method - in the GateIn portal - reached by incoming HTTP request, it acts like a servlet service()
     * method. According to the servlet path used the correct handler is selected and then executed.
     * </p>
     *
     * <p>
     * During a request the request life cycle is demarcated by calls to {@link RequestLifeCycle#begin(ExoContainer);} and
     * {@link RequestLifeCycle#end()}.
     * </p>
     *
     * @param req the http request
     * @param res the http response
     * @throws Exception any exception
     */
    public void service(HttpServletRequest req, HttpServletResponse res) throws Exception {
        boolean debug = log.isDebugEnabled();

        try {
            // We set the character encoding now to UTF-8 before obtaining parameters
            req.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding not supported", e);
        }

        String portalPath = req.getRequestURI().substring(req.getContextPath().length());
        Router router = routerRef.get();

        //
        if (router != null) {
            Iterator<Map<QualifiedName, String>> matcher = router.matcher(portalPath, req.getParameterMap());

            //
            boolean started = false;
            boolean processed = false;

            //
            try {
                while (matcher.hasNext() && !processed) {
                    //
                    Map<QualifiedName, String> parameters = matcher.next();
                    String handlerKey = parameters.get(HANDLER_PARAM);
                    if (handlerKey != null) {
                        WebRequestHandler handler = handlers.get(handlerKey);
                        if (handler != null) {
                            if (debug) {
                                log.debug("Serving request path=" + portalPath + ", parameters=" + parameters
                                        + " with handler " + handler);
                            }

                            if (!started && handler.getRequiresLifeCycle()) {
                                if (debug) {
                                    log.debug("Starting RequestLifeCycle for handler " + handler);
                                }
                                RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
                                started = true;
                            }

                            //
                            processed = handler.execute(new ControllerContext(this, router, req, res, parameters));
                        } else {
                            if (debug) {
                                log.debug("No handler " + handlerKey + " for request path=" + portalPath + ", parameters="
                                        + parameters);
                            }
                        }
                    }
                }
            } finally {
                if (started) {
                    if (debug) {
                        log.debug("Finishing RequestLifeCycle for current request");
                    }
                    RequestLifeCycle.end();
                }
            }

            //
            if (!processed) {
                log.error("Could not associate the request path=" + portalPath + " with an handler");
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            log.error("Missing valid router configuration " + configurationPathRef.get());
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        for (WebRequestHandler handler : handlers.values()) {
            handler.onDestroy(this);
        }
    }
}
