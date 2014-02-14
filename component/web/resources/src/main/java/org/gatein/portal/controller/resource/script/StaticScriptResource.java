/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
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

package org.gatein.portal.controller.resource.script;

import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

/**
 * A non-JavaScript resource possibly distributed with a third party JavaScript framework,
 * such as image, HTML template, CSS, etc.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class StaticScriptResource {

    private final String contextPath;

    private final String directory;

    private final String resourcePath;

    private final String directoryAndPath;

    private final long lastModified;

    /**
     * In in this constructor, {@code lastModified} argument gets rounded down to the nearest second
     * as the related HTTP header is in seconds.
     *
     * @param contextPath must pass {@link #validate(String, String)}
     * @param directory can be {@code null} and must pass {@link #validate(String, String)}
     * @param resourceURI must pass {@link #validate(String, String)}
     * @param lastModified
     *
     * @throws IllegalArgumentException see {@link #validate(String, String)}
     */
    public StaticScriptResource(String contextPath, String directory, String resourceURI, long lastModified) {
        super();
        validate("contextPath", contextPath);
        if (directory != null) {
            validate("directory", directory);
        }
        validate("resourceURI", resourceURI);

        this.contextPath = contextPath;
        this.directory = directory;
        this.resourcePath = resourceURI;
        this.directoryAndPath = directory == null ? resourcePath : directory + resourcePath;
        this.lastModified = (lastModified / 1000) * 1000;
    }

    private static void validate(String argName, String path) {
        if (path == null) {
            throw new IllegalArgumentException(argName +" cannot be null");
        }
        if (path.length() < 1) {
            throw new IllegalArgumentException(argName +" cannot be shorter than 1");
        }
        if (path.charAt(0) != '/') {
            throw new IllegalArgumentException(argName +" must start with '/'; actual: '" + path + "'");
        }
        int contextPathLength = path.length();
        if (contextPathLength >= 2 && path.charAt(contextPathLength -1) == '/') {
            throw new IllegalArgumentException(argName +" cannot end with '/'; actual: '" + path + "'");
        }
    }

    /**
     * Returns the path of the context where this resource lives. Typically {@code /[my-war-name]}
     * from {@code [my-war-name].war} where the resource is packaged.
     *
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns a WAR-internal directory which needs to be prepended to {@link #getResourcePath()}
     * to get a WAR-internal absolute path of this resource, see {@link #getDirectoryAndPath()}.
     *
     * @return the directory or {@code null} if {@link #resourcePath} is absolute.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Returns the path under which this resource can be made publicly accessible relative to {@code baseURL}
     * defined in {@link JavascriptConfigService#getSharedBaseUrl(ControllerContext controllerContext)}.
     * <p>
     * The returned value is granted to be non-null and start with {@code '/'}
     * <p>
     * Example: {@code "/package1/red-shiny.css"}
     * @return the resourcePath
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * Returns a WAR-internal absolute path of this resource that can be used e.g. to open a stream:
     * {@code servletContext.getResourceAsStream(myStaticResource.getDirectoryAndPath())}
     *
     * Equivalent to {@code getDirectory() == null ? getResourcePath() : getDirectory() + getResourcePath()}.
     *
     * @return the directoryAndPath
     */
    public String getDirectoryAndPath() {
        return directoryAndPath;
    }

    /**
     * Number of milliseconds since UNIX epoch rounded down to the nearest second
     * as the related HTTP header is in seconds.
     *
     * @return the lastModified
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "StaticScriptResource [contextPath=" + contextPath + ", directory=" + directory + ", resourcePath="
                + resourcePath + "]";
    }

}
