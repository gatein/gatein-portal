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
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.picocontainer.Startable;

/**
 * @author Julien Viet
 */
public class MongoStore {

    /** . */
    private DB db;

    /** . */
    private MongodProcess mongod;

    /** . */
    private MongodExecutable mongodExe;

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
    private MongoCustomizationStore customizationStore;

    public void start() throws Exception {
        try {
            MongodStarter runtime = MongodStarter.getDefaultInstance();
            this.mongodExe = runtime.prepare(new MongodConfig(Version.V2_0_5, 27777, Network.localhostIsIPv6()));
            this.mongod = mongodExe.start();
            MongoClient mongo = new MongoClient("localhost", 27777);
            this.db = mongo.getDB("gatein");
            this.siteStore = new MongoSiteStore(this);
            this.pageStore = new MongoPageStore(this);
            this.descriptionStore = new MongoDescriptionStore(this);
            this.navigationStore = new MongoNavigationStore(this, descriptionStore);
            this.layoutStore = new MongoLayoutStore(this);
            this.customizationStore = new MongoCustomizationStore(layoutStore);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
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

    public MongoCustomizationStore getCustomizationStore() {
        return customizationStore;
    }

    public void stop() {
        if (mongod != null) {
            if (db != null) {
                db.dropDatabase();
            }
            mongod.stop();
            mongodExe.stop();
        }
    }

    public DB getDB() {
        return db;
    }
}
