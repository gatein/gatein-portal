/**
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
package org.exoplatform.upload;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Thanh Hai
 *          haint@exoplatform.com
 * Jun 29, 2011  
 */
public class MimeTypeUploadPlugin extends BaseComponentPlugin
{
   final private static Logger log = LoggerFactory.getLogger(MimeTypeUploadPlugin.class);
   final private static String MIMETYPE_PATH = "mimetype-path" ;
   final private static String DEFAULT_MIMETYPE = "mimetype-default";
   
   private Properties mimeTypes = new Properties();
   private String mimetypeDefault ;
   
   public MimeTypeUploadPlugin(InitParams initParams, ConfigurationManager configurationService) throws Exception
   {
      ValueParam param = initParams.getValueParam(MIMETYPE_PATH) ;
      URL filePath = configurationService.getURL(param.getValue());
      URLConnection connection = filePath.openConnection() ;
      mimeTypes.load(connection.getInputStream()) ;
      
      param = initParams.getValueParam(DEFAULT_MIMETYPE) ;
      if(param != null) mimetypeDefault = param.getValue() ;
   }
   
   public String getMimeType(String fileName) 
   {
      if(fileName.indexOf('.') == -1) return mimetypeDefault ;
      String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
      String mimeType = mimeTypes.getProperty(ext.toLowerCase(), mimetypeDefault);
      if (mimeType == null || mimeType.length() == 0) return null ;
      return mimeType;
   }
}
