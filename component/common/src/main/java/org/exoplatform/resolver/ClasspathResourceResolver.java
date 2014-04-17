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

package org.exoplatform.resolver;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net Mar 15, 2006
 */
public class ClasspathResourceResolver extends ResourceResolver {

    public URL getResource(String url) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResource(removeScheme(url));
    }

    public InputStream getInputStream(String url) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(removeScheme(url));
    }

    public List<URL> getResources(String url) throws Exception {
        ArrayList<URL> urlList = new ArrayList<URL>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> e = cl.getResources(removeScheme(url));
        while (e.hasMoreElements())
            urlList.add(e.nextElement());
        return urlList;
    }

    public List<InputStream> getInputStreams(String url) throws Exception {
        ArrayList<InputStream> inputStreams = new ArrayList<InputStream>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> e = cl.getResources(removeScheme(url));
        while (e.hasMoreElements())
            inputStreams.add(e.nextElement().openStream());
        return inputStreams;
    }

    @SuppressWarnings("unused")
    public boolean isModified(String url, long lastAccess) {
        return false;
    }

    public String getResourceScheme() {
        return "classpath:";
    }

    @Override
    protected String removeScheme(String url) {
        String scaledURL = super.removeScheme(url);

        // Support url with a leading slash
        if (scaledURL.startsWith("/")) {
            scaledURL = scaledURL.substring(1);
        }
        return scaledURL;
    }

    @Override
    public ResourceKey createResourceKey(String url) {
        return new ResourceKey(this.getResourceScheme().hashCode(), url);
    }
}
