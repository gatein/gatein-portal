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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.exoplatform.application.gadget.impl.GadgetDefinition;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class GadgetImporter {

    /** . */
    protected static final Logger log = LoggerFactory.getLogger(GadgetImporter.class);

    /** The gadget name as seen by GateIn. */
    private String gadgetName;

    /** The gadget uri. */
    private String gadgetURI;

    protected GadgetImporter(String gadgetName, String gadgetURI) {
        this.gadgetName = gadgetName;
        this.gadgetURI = gadgetURI;
    }

    public String getGadgetName() {
        return gadgetName;
    }

    public String getGadgetURI() {
        return gadgetURI;
    }

    protected abstract byte[] getGadgetBytes(String gadgetURI) throws IOException;

    protected abstract String getGadgetURL();

    protected abstract void process(String gadgetURI, GadgetDefinition def) throws Exception;

    protected abstract void processMetadata(ModulePrefs prefs, GadgetDefinition def);

    public void doImport(GadgetDefinition def) throws Exception {
        // Get bytes
        byte[] bytes = getGadgetBytes(gadgetURI);
        if (bytes == null) {
            throw new IOException("Cannot import gadget " + gadgetURI + " because its data could not be found");
        }

        //
        process(gadgetURI, def);

        String encoding = EncodingDetector.detect(new ByteArrayInputStream(bytes));
        String gadget = new String(bytes, encoding);
        String gadgetURL = getGadgetURL();
        GadgetSpec spec = new GadgetSpec(Uri.parse(gadgetURL), gadget);
        ModulePrefs prefs = spec.getModulePrefs();
        processMetadata(prefs, def);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + getGadgetName() + ",path=" + getGadgetURI() + "]";
    }
}
