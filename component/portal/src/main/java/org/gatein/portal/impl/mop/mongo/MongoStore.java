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
package org.gatein.portal.impl.mop.mongo;

import java.lang.reflect.UndeclaredThrowableException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.portal.mop.Store;
import org.picocontainer.Startable;

/**
 * @author Julien Viet
 */
public class MongoStore implements Store, Startable {

    /** . */
    private DB db;

    /** . */
    private MongoSiteStore siteStore;

    /** . */
    private MongoPageStore pageStore;

    /** . */
    private MongoNavigationStore navigationStore;

    /** . */
    private MongoLayoutStore layoutStore;

    /** . */
    private MongoDescriptionStore descriptionStore;
    
    /** . */
    private MongoSecurityStore securityStore;

    /** . */
    private MongoCustomizationStore customizationStore;

    /** . */
    private final String host;

    /** . */
    private final int port;

    /**
     * Create a mongo store with the specified init params.
     *
     * @param params the init params
     */
    public MongoStore(InitParams params) {

        //
        ValueParam hostParam = params.getValueParam("host");
        ValueParam portParam = params.getValueParam("port");

        //
        String host = hostParam != null ? hostParam.getValue().trim() : "localhost";
        int port = portParam != null ? Integer.parseInt(portParam.getValue().trim()) : 27017;

        this.host = host;
        this.port = port;
    }

    /**
     * Create a mongo store with <code>localhost</code> host and <code>27017</code> port.
     */
    public MongoStore() {
        this("localhost", 27017);
    }

    /**
     * Create a mongo store with the specified connection parameters.
     *
     * @param host the host
     * @param port the port
     */
    public MongoStore(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            MongoClient mongo = new MongoClient(host, port);
            this.db = mongo.getDB("gatein");
            this.siteStore = new MongoSiteStore(this);
            this.pageStore = new MongoPageStore(this);
            this.descriptionStore = new MongoDescriptionStore(this);
            this.securityStore = new MongoSecurityStore(this);
            this.navigationStore = new MongoNavigationStore(this, descriptionStore, securityStore);
            this.layoutStore = new MongoLayoutStore(this);
            this.customizationStore = new MongoCustomizationStore(layoutStore);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public void stop() {
    }

    public MongoSiteStore getSiteStore() {
        return siteStore;
    }

    public MongoPageStore getPageStore() {
        return pageStore;
    }

    public MongoNavigationStore getNavigationStore() {
        return navigationStore;
    }

    public MongoLayoutStore getLayoutStore() {
        return layoutStore;
    }

    public MongoDescriptionStore getDescriptionStore() {
        return descriptionStore;
    }
    
    public MongoSecurityStore getSecurityStore() {
        return securityStore;
    }

    public MongoCustomizationStore getCustomizationStore() {
        return customizationStore;
    }

    public DB getDB() {
        return db;
    }
}
