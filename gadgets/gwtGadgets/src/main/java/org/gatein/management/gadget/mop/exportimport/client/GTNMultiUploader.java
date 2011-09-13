/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.gatein.management.gadget.mop.exportimport.client;

import com.google.gwt.uibinder.client.UiConstructor;
import gwtupload.client.IFileInput;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;
import gwtupload.client.MultiUploader;
import gwtupload.client.Uploader;

/**
 * {@code GTNMultiUploader}
 * <p>
 * Implementation of an uploader panel that is able to handle several uploads.
 * This uploader allows to upload multiple files asynchronously using <i>Ajax</i>
 * This version of multi file uploader allows to enable/disable the automatic upload, i.e
 * the upload starts right after selecting file. The default version does not allow
 * this feature and upload is automatic by default.
 * </p>
 * Created on Feb 17, 2011, 6:47:18 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class GTNMultiUploader extends MultiUploader
{

   private boolean automaticUpload;

   /**
    * Create a new instance of {@code GTNMultiUploader}
    * Initialize widget components and layout elements.
    * Uses the default status widget and the standard input file.
    */
   public GTNMultiUploader()
   {
      super();
   }

   /**
    * Create a new instance of {@code GTNMultiUploader}
    * Initialize widget components and layout elements.
    * Uses the default status widget.
    *
    * @param type
    *   file input to use
    */
   @UiConstructor
   public GTNMultiUploader(FileInputType type)
   {
      super(type);
   }

   /**
    * Create a new instance of {@code GTNMultiUploader}
    * Initialize widget components and layout elements.
    *
    * @param type
    *   file input to use
    * @param status
    *   Customized status widget to use
    */
   public GTNMultiUploader(FileInputType type, IUploadStatus status)
   {
      super(type, status);
   }

   /**
    * Create a new instance of {@code GTNMultiUploader}
    * Initialize widget components and layout elements.
    *
    * @param status
    *   Customized status widget to use
    */
   public GTNMultiUploader(IUploadStatus status)
   {
      super(status);
   }

   /**
    * Create a new instance of {@code GTNMultiUploader}
    * This is the constructor for customized multi-uploaders.
    *
    * @param status
    *   Customized status widget to use
    * @param fileInput
    *   Customized file input
    */
   public GTNMultiUploader(IUploadStatus status, IFileInput fileInput)
   {
      super(status, fileInput);
   }

   @Override
   protected IUploader getUploaderInstance()
   {
      Uploader uploader = (Uploader) super.getUploaderInstance();
      uploader.setAutoSubmit(automaticUpload);
      return uploader;
   }

   /**
    * @return the automaticUpload
    */
   public boolean isAutomaticUpload()
   {
      return automaticUpload;
   }

   /**
    * @param automaticUpload the automaticUpload to set
    */
   public void setAutomaticUpload(boolean automaticUpload)
   {
      this.automaticUpload = automaticUpload;
   }
}
