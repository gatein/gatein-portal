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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

public class UploadService {
    /** . */
    private static final Logger log = LoggerFactory.getLogger(UploadService.class);

    /**
     * These are list ascii-codes of special characters. We should not enable these characters in fileName.
     * They are control codes - that can not printable (from 0 to 31) or special characters like: *, <, >, \, /, :, ?, ", etc.
     * For mor details about ascii-code please see at: http://www.ascii-code.com/
     */
    private static final int[] illegalChars = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 34, 42, 47, 58, 60, 62, 63, 92, 124};

    private List<MimeTypeUploadPlugin> plugins;

    private Map<String, UploadResource> uploadResources = new LinkedHashMap<String, UploadResource>();

    private String uploadLocation_;

    private UploadLimit defaultUploadLimitMB_;

    private Map<String, UploadLimit> uploadLimits = new LinkedHashMap<String, UploadLimit>();

    public static String UPLOAD_RESOURCES_STACK = "uploadResourcesStack";

    public static enum UploadUnit {
        KB, MB, GB
    };

    public UploadService(PortalContainerInfo pinfo, InitParams params) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (params == null || params.getValueParam("upload.limit.size") == null)
            defaultUploadLimitMB_ = new UploadLimit(0, UploadUnit.MB); // 0 means unlimited
        else
            defaultUploadLimitMB_ = new UploadLimit(Integer.parseInt(params.getValueParam("upload.limit.size").getValue()),
                    UploadUnit.MB);
        uploadLocation_ = tmpDir + "/" + pinfo.getContainerName() + "/eXoUpload";
    }

    public void register(MimeTypeUploadPlugin plugin) {
        if (plugins == null)
            plugins = new ArrayList<MimeTypeUploadPlugin>();
        plugins.add(plugin);
    }

    /**
     * Create UploadResource for HttpServletRequest
     *
     * @param request the webapp's {@link javax.servlet.http.HttpServletRequest}
     * @throws FileUploadException
     */
    public void createUploadResource(HttpServletRequest request) throws FileUploadException {
        String uploadId = request.getParameter("uploadId");
        createUploadResource(uploadId, request);
    }

    public void createUploadResource(String uploadId, HttpServletRequest request) throws FileUploadException {
        UploadResource upResource = new UploadResource(uploadId);
        upResource.setFileName("");// Avoid NPE in UploadHandler
        uploadResources.put(upResource.getUploadId(), upResource);

        putToStackInSession(request.getSession(true), uploadId);

        double contentLength = request.getContentLength();
        upResource.setEstimatedSize(contentLength);
        if (isLimited(upResource, contentLength)) {
            upResource.setStatus(UploadResource.FAILED_STATUS);
            return;
        }

        ServletFileUpload servletFileUpload = makeServletFileUpload(upResource);
        // parse request
        List<FileItem> itemList = null;
        try {
            itemList = servletFileUpload.parseRequest(request);
        } catch (FileUploadException uploadEx) {
            if (uploadEx instanceof FileUploadBase.IOFileUploadException) {
                log.debug("IOException while upload resource", uploadEx);
            } else {
                throw uploadEx;
            }
        }

        if (itemList == null || itemList.size() != 1 || itemList.get(0).isFormField()) {
            log.debug("Please upload 1 file per request");
            removeUploadResource(uploadId);
            return;
        }

        DiskFileItem fileItem = (DiskFileItem) itemList.get(0);
        String fileName = fileItem.getName();
        if (fileName == null)
            fileName = uploadId;
        fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        fileName = this.correctFileName(fileName);
        String storeLocation = uploadLocation_ + "/" + uploadId + "." + fileName;

        // commons-fileupload will store the temp file with name *.tmp
        // we need to rename it to our desired name
        fileItem.getStoreLocation().renameTo(new File(storeLocation));
        File fileStore = new File(storeLocation);
        if (!fileStore.exists())
            try {
                fileStore.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        upResource.setFileName(fileName);
        upResource.setMimeType(fileItem.getContentType());
        if (plugins != null)
            for (MimeTypeUploadPlugin plugin : plugins) {
                String mimeType = plugin.getMimeType(fileName);
                if (mimeType != null)
                    upResource.setMimeType(mimeType);
            }
        upResource.setStoreLocation(storeLocation);
        upResource.setStatus(UploadResource.UPLOADED_STATUS);
    }

    /**
     * @deprecated use {@link #createUploadResource(String, javax.servlet.http.HttpServletRequest)} instead
     *
     */
    public void createUploadResource(String uploadId, String encoding, String contentType, double contentLength,
            InputStream inputStream) throws Exception {
        File uploadDir = new File(uploadLocation_);
        if (!uploadDir.exists())
            uploadDir.mkdirs();
        UploadResource upResource = new UploadResource(uploadId);
        RequestStreamReader reader = new RequestStreamReader(upResource);
        uploadResources.put(upResource.getUploadId(), upResource);
        if (isLimited(upResource, contentLength)) {
            upResource.setStatus(UploadResource.FAILED_STATUS);
            return;
        }

        Map<String, String> headers = reader.parseHeaders(inputStream, encoding);

        String fileName = reader.getFileName(headers);
        if (fileName == null)
            fileName = uploadId;
        fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);

        upResource.setFileName(fileName);
        upResource.setMimeType(headers.get(RequestStreamReader.CONTENT_TYPE));
        upResource.setStoreLocation(uploadLocation_ + "/" + uploadId + "." + fileName);
        upResource.setEstimatedSize(contentLength);
        File fileStore = new File(upResource.getStoreLocation());
        if (!fileStore.exists())
            fileStore.createNewFile();
        FileOutputStream output = new FileOutputStream(fileStore);
        reader.readBodyData(inputStream, contentType, output);

        if (upResource.getStatus() == UploadResource.UPLOADING_STATUS) {
            upResource.setStatus(UploadResource.UPLOADED_STATUS);
            return;
        }

        uploadResources.remove(uploadId);
        fileStore.delete();
    }

    @SuppressWarnings("unchecked")
    private void putToStackInSession(HttpSession session, String uploadId) {
        Set<String> uploadResouceIds = (Set<String>) session.getAttribute(UploadService.UPLOAD_RESOURCES_STACK);
        if (uploadResouceIds == null) {
            uploadResouceIds = new HashSet();
        }
        uploadResouceIds.add(uploadId);
        session.setAttribute(UploadService.UPLOAD_RESOURCES_STACK, uploadResouceIds);
    }

    /**
     * Get UploadResource by uploadId
     *
     * @param uploadId uploadId of UploadResource
     * @return org.exoplatform.upload.UploadResource of uploadId
     */
    public UploadResource getUploadResource(String uploadId) {
        return uploadResources.get(uploadId);
    }

    /**
     * Clean up temporary files that are uploaded in the Session but not removed yet
     *
     * @param session
     */
    public void cleanUp(HttpSession session) {
        log.debug("Cleaning up uploaded files for temporariness");
        Set<String> uploadIds = (Set<String>) session.getAttribute(UploadService.UPLOAD_RESOURCES_STACK);
        if (uploadIds != null) {
            for (String id : uploadIds) {
                removeUploadResource(id);
                uploadLimits.remove(id);
            }
        }
    }

    /**
     * @deprecated use {@link #removeUploadResource(String)} instead
     *
     * @param uploadId
     */
    @Deprecated
    public void removeUpload(String uploadId) {
        removeUploadResource(uploadId);
    }

    /**
     * Remove the UploadResource and its temporary file that associated with given <code>uploadId</code>. <br/>
     * If <code>uploadId</code> is null or UploadResource is null, do nothing
     *
     * @param uploadId uploadId of UploadResource will be removed
     */
    public void removeUploadResource(String uploadId) {
        if (uploadId == null)
            return;
        UploadResource upResource = uploadResources.get(uploadId);
        if (upResource != null) {
            uploadResources.remove(uploadId);

            if (upResource.getStoreLocation() != null) {
                File file = new File(upResource.getStoreLocation());
                file.delete();
            }
        }

        // uploadLimitsMB_.remove(uploadId);
    }

    /**
     * Registry upload limit size for uploadLimitsMB_. If limitMB is null, defaultUploadLimitMB_ will be registried
     *
     * @param uploadId
     * @param limitMB upload limit size
     */
    public void addUploadLimit(String uploadId, Integer limitMB) {
        addUploadLimit(uploadId, limitMB, UploadUnit.MB);
    }

    public void addUploadLimit(String uploadId, Integer limit, UploadUnit unit) {
        if (limit == null) {
            uploadLimits.put(uploadId, defaultUploadLimitMB_);
        } else if (unit == null) {
            uploadLimits.put(uploadId, new UploadLimit(limit, UploadUnit.MB));
        } else {
            uploadLimits.put(uploadId, new UploadLimit(limit, unit));
        }
    }

    public void removeUploadLimit(String uploadId) {
        uploadLimits.remove(uploadId);
    }

    /**
     * Get all upload limit sizes
     *
     * @return all upload limit sizes
     */
    public Map<String, UploadLimit> getUploadLimits() {
        return uploadLimits;
    }

    public String correctFileName(String fileName) {
        if(fileName == null || fileName.isEmpty()) return "NULL";

        char[] chars = fileName.toCharArray();
        for(int i = 0; i < chars.length; i++) {
            if (Arrays.binarySearch(illegalChars, chars[i]) >= 0) {
                chars[i] = '_';
            }
        }

        return new String(chars);
    }

    private ServletFileUpload makeServletFileUpload(final UploadResource upResource) {
        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Set factory constraints
        factory.setSizeThreshold(0);
        File uploadDir = new File(uploadLocation_);
        if (!uploadDir.exists())
            uploadDir.mkdirs();
        factory.setRepository(uploadDir);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        ProgressListener listener = new ProgressListener() {
            public void update(long pBytesRead, long pContentLength, int pItems) {
                if (pBytesRead == upResource.getUploadedSize())
                    return;
                upResource.addUploadedBytes(pBytesRead - upResource.getUploadedSize());
            }
        };
        upload.setProgressListener(listener);
        return upload;
    }

    private boolean isLimited(UploadResource upResource, double contentLength) {
        // by default, use the limit set in the service
        UploadLimit limit = defaultUploadLimitMB_;
        // if the limit is set in the request (specific for this upload) then use
        // this value instead of the default one
        if (uploadLimits.containsKey(upResource.getUploadId())) {
            limit = uploadLimits.get(upResource.getUploadId());
        }

        double estimatedSize = contentLength / limit.division;
        if (limit.getLimit() > 0 && estimatedSize > limit.getLimit()) { // a limit set to 0 means unlimited
            if (log.isDebugEnabled()) {
                log.debug("Upload cancelled because file bigger than size limit : " + estimatedSize + " " + limit.unit + " > "
                        + limit.getLimit() + " " + limit.unit);
            }
            return true;
        }
        return false;
    }

    public static class UploadLimit {
        private int limit;

        private int division;

        private UploadUnit unit;

        public UploadLimit(int limit, UploadUnit unit) {
            this.limit = limit;
            this.unit = unit;
            if (unit == UploadUnit.KB) {
                division = 1024;
            } else if (unit == UploadUnit.MB) {
                division = 1024 * 1024;
            } else if (unit == UploadUnit.GB) {
                division = 1024 * 1024 * 1024;
            }
        }

        public int getLimit() {
            return limit;
        }

        public String getUnit() {
            return unit.toString();
        }
    }
}
