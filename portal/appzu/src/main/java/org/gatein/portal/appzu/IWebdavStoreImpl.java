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
import java.io.InputStream;
import java.security.Principal;

import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;

/**
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
        return ApplicationRepository.instance.store.getResourceContent(transaction, resourceUri);
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

    @Override
    public StoredObject getStoredObject(ITransaction transaction, String uri) {
        return ApplicationRepository.instance.store.getStoredObject(transaction, uri);
    }
}
