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
import java.io.InputStream;
import java.security.Principal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import juzu.impl.common.Name;
import juzu.impl.common.Resource;
import juzu.impl.common.Timestamped;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadFileSystem;
import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;

/**
 * Partially delegates to the app fs for GET, otherwise use the temp real file system.
 * todo : use fully the app fs
 *
 * @author Julien Viet
 */
public class IWebdavStoreImpl implements IWebdavStore {

    public IWebdavStoreImpl(File file) {
        // We need this constructor for the webdav servlet
    }

    @Override
    public ITransaction begin(Principal principal) {
        return ApplicationRepository.instance.store.begin(principal);
    }

    @Override
    public void checkAuthentication(ITransaction transaction) {
        ApplicationRepository.instance.store.checkAuthentication(transaction);
    }

    @Override
    public void commit(ITransaction transaction) {
        ApplicationRepository.instance.store.commit(transaction);
    }

    @Override
    public void rollback(ITransaction transaction) {
        ApplicationRepository.instance.store.rollback(transaction);
    }

    @Override
    public void createFolder(ITransaction transaction, String folderUri) {
        ApplicationRepository.instance.store.createFolder(transaction, folderUri);
    }

    @Override
    public void createResource(ITransaction transaction, String resourceUri) {
        ApplicationRepository.instance.store.createResource(transaction, resourceUri);
    }

    @Override
    public InputStream getResourceContent(ITransaction transaction, String resourceUri) {
        AppStoreObject so = getStoredObject(transaction, resourceUri);
        if (so != null) {
            try {
                Timestamped<Resource> content = so.fs.getResource(so.path);
                return content.getObject().getInputStream();
            } catch (IOException e) {
                throw new WebdavException(e);
            }
        } else {
            throw new WebdavException();
        }
    }

    @Override
    public long setResourceContent(ITransaction transaction, String resourceUri, InputStream content, String contentType, String characterEncoding) {
        return ApplicationRepository.instance.store.setResourceContent(transaction, resourceUri, content, contentType, characterEncoding);
    }

    @Override
    public String[] getChildrenNames(ITransaction transaction, String folderUri) {
        return ApplicationRepository.instance.store.getChildrenNames(transaction, folderUri);
    }

    @Override
    public long getResourceLength(ITransaction transaction, String path) {
        return ApplicationRepository.instance.store.getResourceLength(transaction, path);
    }

    @Override
    public void removeObject(ITransaction transaction, String uri) {
        ApplicationRepository.instance.store.removeObject(transaction, uri);
    }

    static final Pattern P = Pattern.compile("^/([^/]+)(?:/(.*))?$");

    @Override
    public AppStoreObject getStoredObject(ITransaction transaction, String uri) {
        Matcher matcher = P.matcher(uri);
        if (matcher.matches()) {
            try {
                String name = matcher.group(1);
                App app = ApplicationRepository.getInstance().getApplication(Name.parse(name));
                if (app != null) {
                    String path = matcher.group(2);
                    Object current = app.fs.getRoot();
                    if (path != null) {
                        for (String atom : Tools.split(matcher.group(2), '/')) {
                            if (atom.length() > 0) {
                                current = app.fs.getChild(current, atom);
                                if (current == null) {
                                    return null;
                                }
                            }
                        }
                    }
                    long lastModified = app.fs.getLastModified(current);
                    AppStoreObject so = new AppStoreObject(app.fs, current);
                    so.setLastModified(new Date(lastModified));
                    so.setCreationDate(new Date(lastModified));
                    if (app.fs.isDir(current)) {
                        so.setFolder(true);
                    } else {
                        so.setFolder(false);
                        Timestamped<Resource> content = app.fs.getResource(current);
                        so.setResourceLength(content.getObject().getSize());
                    }
                    return so;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    static class AppStoreObject extends StoredObject {

        /** . */
        final ReadFileSystem fs;

        /** . */
        final Object path;

        AppStoreObject(ReadFileSystem fs, Object path) {
            this.fs = fs;
            this.path = path;
        }
    }
}
