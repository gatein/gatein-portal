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
package org.gatein.portal.appzu;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import juzu.impl.common.Resource;
import juzu.impl.common.Name;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.disk.DiskFileSystem;
import juzu.impl.fs.spi.url.Node;
import juzu.impl.fs.spi.url.URLFileSystem;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.LocalFileSystemStore;

/**
 * @author Julien Viet
 */
public class ApplicationRepository {

    /** . */
    static final ApplicationRepository instance = new ApplicationRepository();

    static {
        if ("true".equals(System.getProperty("gatein.appzu.security_manager"))) {
            System.setSecurityManager(new AppSecurityManager());
        }
    }

    public static ApplicationRepository getInstance() {
        return instance;
    }

    /** . */
    private final ConcurrentHashMap<Name, App> applications = new ConcurrentHashMap<Name, App>();

    /** . */
    final File root;

    /** . */
    final IWebdavStore store;

    public ApplicationRepository() {
        File root;
        try {
            root = File.createTempFile("appzu", ".tmp");
            if (root.delete()) {
                if (root.mkdirs()) {
                    root.deleteOnExit();
                } else {
                    throw new IOException("Could not create " + root);
                }
            } else {
                throw new IOException("Could not delete " + root);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create application repository", e);
        }
        this.root = root;
        this.store = new LocalFileSystemStore(root);
    }

    public App getApplication(Name name) {
        return applications.get(name);
    }

    public Iterable<App> getApplications() {
        return applications.values();
    }

    /**
     * Add a new application from an URL.
     *
     * @param name the application name
     * @param url the application url
     * @return the app
     */
    public App addApplication(Name name, String displayName, URL url) throws Exception {
        URLFileSystem fs = new URLFileSystem();
        fs.add(url);
        return addApplication(name, displayName, fs);
    }

    public App addApplication(final Name name, String displayName, final String templateId) throws Exception {
        if (name.length() == 0) {
            throw new Exception("Name cannot be empty");
        }
        for (String atom : name) {
            for (int i = 0;i < atom.length();i++) {
                char c = atom.charAt(i);
                if (!((i == 0 && Character.isJavaIdentifierStart(c)) || (i > 0 && Character.isJavaIdentifierPart(c)))) {
                    throw new Exception("Invalid name " + name);
                }
            }
        }
        URL url = ApplicationRepository.class.getResource("templates/" + templateId + "/");
        if (url == null) {
            throw new Exception("No such template " + templateId);
        } else {
            // Copy the sample on disk
            final URLFileSystem source = new URLFileSystem();
            source.add(url);
            final DiskFileSystem target = createFileSystem(name);
            source.traverse(new Visitor.Default<Node>() {
                File current = target.getRoot();
                {
                    for (String atom : name) {
                        enterDir(null, atom);
                    }
                }
                @Override
                public void enterDir(Node dir, String name) throws IOException {
                    current = new File(current, name);
                    if (!current.exists()) {
                        current.mkdirs();
                    }
                }
                @Override
                public void leaveDir(Node dir, String name) throws IOException {
                    current = current.getParentFile();
                }
                String srcPkg = ApplicationRepository.class.getPackage().getName() + ".templates." + templateId;
                String dstPkg = name.toString();
                @Override
                public void file(Node file, String name) throws IOException {
                    File f = new File(current, name);
                    if (!f.exists()) {
                        Resource content = source.getResource(file).getObject();
                        if (name.endsWith(".java")) {
                            // Rewrite content
                            String s = content.getCharSequence().toString();
                            s = s.replaceAll(srcPkg, dstPkg);
                            content = new Resource(s);
                        }
                        target.updateResource(f, content);
                    }
                }
            });
            return addApplication(name, displayName, target);
        }
    }

    private DiskFileSystem createFileSystem(Name name) throws Exception {
        File app = new File(root, name.toString());
        if (app.exists()) {
            if (!app.isDirectory()) {
                throw new IOException("App dir " + app.getAbsolutePath() + " already exists and is a file");
            }
        } else {
            if (!app.mkdirs()) {
                throw new IOException("Could not create dir " + app.getAbsolutePath());
            }
        }
        return new DiskFileSystem(app);
    }

    /**
     * Add a new sample application
     *
     * @param name the application name
     * @return the app
     */
    public App addApplication(Name name, String displayName) throws Exception {
        DiskFileSystem fs = createFileSystem(name);
        File root = fs.makePath(name);
        fs.createDir(root);
        fs.updateResource(new File(root, "package-info.java"), new Resource("@Application\npackage " + name + ";\n\nimport juzu.Application;\n"));
        fs.updateResource(new File(root, "Controller.java"), new Resource(
                "package " + name + ";\n" +
                        "import juzu.View;\n" +
                        "import juzu.Response;\n" +
                        "\n" +
                        "public class Controller {\n" +
                        "\n" +
                        "public @View Response index() {\n" +
                        "return Response.ok(\"Hello World\");\n" +
                        "}\n" +
                        "}\n"));

        //
        System.out.println("Created app " + name + " at " + fs.getRoot().getAbsolutePath());
        return addApplication(name, displayName, fs);
    }

    /**
     * Add a new application
     *
     * @param name the application name
     * @param fs the application file system
     * @return the app
     */
    public <T> App addApplication(Name name, String displayName, ReadFileSystem<T> fs) throws Exception {
        App app = new App(name, displayName, fs);
        App phantom = applications.putIfAbsent(name, app);
        if (phantom != null) {
            app = phantom;
        }
        return app;
    }
}
