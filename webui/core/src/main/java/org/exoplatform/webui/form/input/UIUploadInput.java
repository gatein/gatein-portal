/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.form.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.upload.UploadService.UploadUnit;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormInputBase;



/**
 * @author <a href="mailto:haint@exoplatform.com">Nguyen Thanh Hai</a>
 *
 * @datJul 19, 2011
 */

@ComponentConfig(template = "system:/groovy/webui/form/UIUploadInput.gtmpl")
public class UIUploadInput extends UIFormInputBase<String>
{
   private String[] uploadId;
   
   private boolean isAutoUpload = true;
   
   private int limitFile = 1;
   
   public UIUploadInput(String name, String bindingExpression, int limitFile)
   {
      super(name, bindingExpression, String.class);
      if(limitFile > 1) this.limitFile = limitFile ;
      uploadId = new String[this.limitFile] ;
      for(int i = 0; i < uploadId.length; i++) 
      {
         uploadId[i] = new StringBuffer().append(Math.abs(hashCode())).append('-').append(i).toString();
      }
      UploadService service = getApplicationComponent(UploadService.class);
      for(int i = 0; i < uploadId.length; i++)
      {
         service.addUploadLimit(uploadId[i], null); // Use the limit set by the service. Warning, the service can allow no size limit (value to 0)
      }
      setComponentConfig(UIUploadInput.class, null);
   }

   public UIUploadInput(String name, String bindingExpression,int limitFile, int limitSize)
   {
      this(name, bindingExpression, limitFile, limitSize, UploadUnit.MB);
   }
   
   public UIUploadInput(String name, String bindingExpression, int limitFile, int limitSize, UploadUnit unit)
   {
      super(name, bindingExpression, String.class);
      if(limitFile > 1) this.limitFile = limitFile ;
      uploadId = new String[this.limitFile] ;
      for(int i = 0; i < uploadId.length; i++) 
      {
         uploadId[i] = new StringBuffer().append(Math.abs(hashCode())).append('-').append(i).toString();
      }
      UploadService service = getApplicationComponent(UploadService.class);
      for(int i = 0; i < uploadId.length; i++)
      {
         service.addUploadLimit(uploadId[i], Integer.valueOf(limitSize), unit);
      }
      setComponentConfig(UIUploadInput.class, null);
   }
   
   public String[] getUploadId()
   {
      return uploadId;
   }
   
   public void setAutoUpload(boolean isAutoUpload)
   {
      this.isAutoUpload = isAutoUpload;
   }
   
   public boolean isAutoUpload()
   {
      return isAutoUpload;
   }
   
   public int getLimitFile() 
   {
      return limitFile;
   }
   
   public UploadResource[] getUploadResources() {
      List<UploadResource> holder = new ArrayList<UploadResource>();
      UploadService service = getApplicationComponent(UploadService.class);
      for(int i = 0; i < uploadId.length; i++)
      {
         UploadResource uploadResource = service.getUploadResource(uploadId[i]);
         if(uploadResource == null) continue;
         holder.add(uploadResource) ;
      }
      return holder.toArray(new UploadResource[holder.size()]);
   }
   
   public UploadResource getUploadResource(String uploadId) {
      UploadService service = getApplicationComponent(UploadService.class);
      return service.getUploadResource(uploadId);
   }
   
   public InputStream[] getUploadDataAsStreams() throws FileNotFoundException 
   {
      List<InputStream> holder = new ArrayList<InputStream>();
      UploadService service = getApplicationComponent(UploadService.class);
      for(int i = 0; i < uploadId.length; i++)
      {
         UploadResource uploadResource = service.getUploadResource(uploadId[i]);
         if(uploadResource == null) continue;
         File file = new File(uploadResource.getStoreLocation());
         holder.add(new FileInputStream(file));
      }
      return holder.toArray(new InputStream[holder.size()]);
   }
   
   public InputStream getUploadDataAsStream(String uploadId) throws FileNotFoundException
   {
      UploadService service = getApplicationComponent(UploadService.class);
      UploadResource uploadResource = service.getUploadResource(uploadId);
      if(uploadResource == null) return null;
      else return new FileInputStream(new File(uploadResource.getStoreLocation()));
   }

   public void decode(Object input, WebuiRequestContext context) throws Exception
   {
   }
}
