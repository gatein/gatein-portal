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
package org.exoplatform.application.gadget;

import org.exoplatform.application.gadget.impl.GadgetRegistryServiceImpl;
import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.commons.xml.XMLValidator;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.xml.XMLTools;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GadgetDeployer implements WebAppListener, Startable {

    /** . */
    private static final XMLValidator validator = new XMLValidator(GadgetDeployer.class,
            "http://www.gatein.org/xml/ns/gadgets_1_0", "gadgets_1_0.xsd");

    /** . */
    private final Logger log = LoggerFactory.getLogger(GadgetDeployer.class);

    /** . */
    private GadgetRegistryServiceImpl gadgetRegistryService;

    /** . */
    private ExoContainerContext context;

    public GadgetDeployer(ExoContainerContext context, GadgetRegistryService gadgetRegistryService) {
        this.context = context;
        this.gadgetRegistryService = (GadgetRegistryServiceImpl) gadgetRegistryService;
    }

    public void onEvent(WebAppEvent webAppEvent) {
        if (webAppEvent instanceof WebAppLifeCycleEvent) {
            WebAppLifeCycleEvent lfEvent = (WebAppLifeCycleEvent) webAppEvent;
            if (lfEvent.getType() == WebAppLifeCycleEvent.ADDED) {
                WebApp webApp = webAppEvent.getWebApp();
                ServletContext scontext = webApp.getServletContext();
                try {
                    final URL url = scontext.getResource("/WEB-INF/gadget.xml");
                    if (url != null) {
                        final RootContainer.PortalContainerPostInitTask task = new RootContainer.PortalContainerPostInitTask() {
                            public void execute(ServletContext context, PortalContainer portalContainer) {
                                handle(context, url);
                            }
                        };
                        PortalContainer.addInitTask(scontext, task, context.getPortalContainerName());
                    }
                } catch (MalformedURLException e) {
                    log.error("Could not read the content of the gadget file", e);
                }
            }
        }
    }

    public void start() {
        ServletContainerFactory.getServletContainer().addWebAppListener(this);
    }

    public void stop() {
        ServletContainerFactory.getServletContainer().removeWebAppListener(this);
    }

    private void handle(ServletContext scontext, URL gadgetsURL) {
        try {
            List<GadgetImporter> importers = new ArrayList<GadgetImporter>();
            Document docXML = validator.validate(DocumentSource.create(gadgetsURL));
            NodeList nodeList = docXML.getElementsByTagName("gadget");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element gadgetElement = (Element) nodeList.item(i);
                String gadgetName = gadgetElement.getAttribute("name");

                //
                log.debug("About to parse gadget " + gadgetName);
                Element pathElt = XMLTools.getUniqueChild(gadgetElement, "path", false);
                GadgetImporter importer = null;
                if (pathElt != null) {
                    String path = XMLTools.asString(pathElt, true);
                    importer = new ServletLocalImporter(gadgetName, path, scontext, gadgetRegistryService);
                } else {
                    Element urlElt = XMLTools.getUniqueChild(gadgetElement, "url", false);
                    if (urlElt != null) {
                        String url = XMLTools.asString(urlElt, true);
                        importer = new RemoteImporter(gadgetName, url);
                    }
                }

                //
                if (importer != null) {
                    importers.add(importer);
                    log.debug("Add gadget " + gadgetName + " to gadget imports");
                }
            }

            // Import everything
            gadgetRegistryService.deploy(importers);
        } catch (Exception e) {
            log.error("Could not process gadget file " + gadgetsURL, e);
        }
    }
}
