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

package org.exoplatform.upload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.text.EntityEncoder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UploadService
{
   /** . */
   private static final Logger log = LoggerFactory.getLogger(UploadService.class);
   
   private List<MimeTypeUploadPlugin> plugins ;

   private Map<String, UploadResource> uploadResources = new LinkedHashMap<String, UploadResource>();

   private String uploadLocation_;

   private int defaultUploadLimitMB_;

   private Map<String, Integer> uploadLimitsMB_ = new LinkedHashMap<String, Integer>();
   
   public static String UPLOAD_RESOURCES_STACK = "uploadResourcesStack";

   public UploadService(PortalContainerInfo pinfo, InitParams params) throws Exception
   {
      String tmpDir = System.getProperty("java.io.tmpdir");
      if (params == null || params.getValueParam("upload.limit.size") == null)
         defaultUploadLimitMB_ = 0; // 0 means unlimited
      else
         defaultUploadLimitMB_ = Integer.parseInt(params.getValueParam("upload.limit.size").getValue());
      uploadLocation_ = tmpDir + "/" + pinfo.getContainerName() + "/eXoUpload";
      File uploadDir = new File(uploadLocation_);
      if (!uploadDir.exists())
         uploadDir.mkdirs();
   }
   
   public void register(MimeTypeUploadPlugin plugin) {
      if(plugins == null) plugins = new ArrayList<MimeTypeUploadPlugin>() ;
      plugins.add(plugin) ;
   }
   
   /**
    * Create UploadResource for HttpServletRequest
    * 
    * @param request
    *           the webapp's {@link javax.servlet.http.HttpServletRequest}
    * @throws FileUploadException
    */
   public void createUploadResource(HttpServletRequest request) throws FileUploadException
   {
      String uploadId = request.getParameter("uploadId");
      createUploadResource(uploadId, request);
   }

   public void createUploadResource(String uploadId, HttpServletRequest request) throws FileUploadException
   {
      UploadResource upResource = new UploadResource(uploadId);
      upResource.setFileName("");// Avoid NPE in UploadHandler
      uploadResources.put(upResource.getUploadId(), upResource);

      putToStackInSession(request.getSession(true), uploadId);

      double contentLength = request.getContentLength();
      upResource.setEstimatedSize(contentLength);
      if (isLimited(upResource, contentLength))
      {
         upResource.setStatus(UploadResource.FAILED_STATUS);
         return;
      }

      ServletFileUpload servletFileUpload = makeServletFileUpload(upResource);
      // parse request
      List<FileItem> itemList = servletFileUpload.parseRequest(request);
      if (itemList == null || itemList.size() != 1 || itemList.get(0).isFormField())
      {
         log.debug("Please upload 1 file per request");
         return;
      }

      DiskFileItem fileItem = (DiskFileItem)itemList.get(0);
      String fileName = fileItem.getName();
      if (fileName == null)
         fileName = uploadId;
      fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
      fileName = EntityEncoder.FULL.encode(fileName);
      String storeLocation = uploadLocation_ + "/" + uploadId + "." + fileName;

      
      // commons-fileupload will store the temp file with name *.tmp
      // we need to rename it to our desired name
      fileItem.getStoreLocation().renameTo(new File(storeLocation));

      upResource.setFileName(fileName);
      upResource.setMimeType(fileItem.getContentType());
      if(plugins != null) 
         for(MimeTypeUploadPlugin plugin : plugins) 
         {
            String mimeType = plugin.getMimeType(fileName) ;
            if(mimeType != null) upResource.setMimeType(mimeType) ;
         }
      upResource.setStoreLocation(storeLocation);
      upResource.setStatus(UploadResource.UPLOADED_STATUS);
   }

   @SuppressWarnings("unchecked")
   private void putToStackInSession(HttpSession session, String uploadId)
   {
      Set<String> uploadResouceIds = (Set<String>)session.getAttribute(UploadService.UPLOAD_RESOURCES_STACK);
      if (uploadResouceIds == null)
      {
         uploadResouceIds = new HashSet();
      }
      uploadResouceIds.add(uploadId);
      session.setAttribute(UploadService.UPLOAD_RESOURCES_STACK, uploadResouceIds);
   }

   /**
    * Get UploadResource by uploadId
    * 
    * @param uploadId
    *           uploadId of UploadResource
    * @return org.exoplatform.upload.UploadResource of uploadId
    */
   public UploadResource getUploadResource(String uploadId)
   {
      return uploadResources.get(uploadId);
   }

   /**
    * Clean up temporary files that are uploaded in the Session but not removed yet
    * 
    * @param session
    */
   public void cleanUp(HttpSession session)
   {
      log.debug("Cleaning up uploaded files for temporariness");
      Set<String> uploadIds = (Set<String>)session.getAttribute(UploadService.UPLOAD_RESOURCES_STACK);
      if (uploadIds != null)
      {
         for (String id : uploadIds)
         {
            removeUploadResource(id);
            uploadLimitsMB_.remove(id);
         }
      }
   }

   /**
    * @deprecated use {@link #removeUploadResource(String)} instead
    * 
    * @param uploadId
    */
   @Deprecated
   public void removeUpload(String uploadId)
   {
      removeUploadResource(uploadId);
   }
   
   /**
    * Remove the UploadResource and its temporary file that associated with given <code>uploadId</code>.
    * <br/>If <code>uploadId</code> is null or UploadResource is null, do nothing
    * 
    * @param uploadId uploadId of UploadResource will be removed
    */
   public void removeUploadResource(String uploadId)
   {
      if (uploadId == null)
         return;
      UploadResource upResource = uploadResources.get(uploadId);
      if (upResource != null)
      {
         uploadResources.remove(uploadId);

         if (upResource.getStoreLocation() != null)
         {
            File file = new File(upResource.getStoreLocation());
            file.delete();
         }
      }

      // uploadLimitsMB_.remove(uploadId);
   }

   /**
    * Registry upload limit size for uploadLimitsMB_. If limitMB is null,
    * defaultUploadLimitMB_ will be registried
    * 
    * @param uploadId
    * @param limitMB
    *           upload limit size
    */
   public void addUploadLimit(String uploadId, Integer limitMB)
   {
      if (limitMB == null)
         uploadLimitsMB_.put(uploadId, Integer.valueOf(defaultUploadLimitMB_));
      else
         uploadLimitsMB_.put(uploadId, limitMB);
   }

   /**
    * Get all upload limit sizes
    * 
    * @return all upload limit sizes
    */
   public Map<String, Integer> getUploadLimitsMB()
   {
      return uploadLimitsMB_;
   }

   private ServletFileUpload makeServletFileUpload(final UploadResource upResource)
   {
      // Create a factory for disk-based file items
      DiskFileItemFactory factory = new DiskFileItemFactory();

      // Set factory constraints
      factory.setSizeThreshold(0);
      factory.setRepository(new File(uploadLocation_));

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);
      upload.setHeaderEncoding("UTF-8");
      ProgressListener listener = new ProgressListener()
      {
         public void update(long pBytesRead, long pContentLength, int pItems)
         {
            if (pBytesRead == upResource.getUploadedSize())
               return;
            upResource.addUploadedBytes(pBytesRead - upResource.getUploadedSize());
         }
      };
      upload.setProgressListener(listener);
      return upload;
   }

   private boolean isLimited(UploadResource upResource, double contentLength)
   {
      // by default, use the limit set in the service
      int limitMB = defaultUploadLimitMB_;
      // if the limit is set in the request (specific for this upload) then use
      // this value instead of the default one
      if (uploadLimitsMB_.containsKey(upResource.getUploadId()))
      {
         limitMB = uploadLimitsMB_.get(upResource.getUploadId()).intValue();
      }

      int estimatedSizeMB = (int)((contentLength / 1024) / 1024);
      if (limitMB > 0 && estimatedSizeMB > limitMB)
      { // a limit set to 0 means unlimited         
         if (log.isDebugEnabled())
         {
            log.debug("Upload cancelled because file bigger than size limit : " + estimatedSizeMB + " MB > " + limitMB
               + " MB");
         }
         return true;
      }
      return false;
   }

   
}
