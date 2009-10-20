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

package org.exoplatform.webui.form.wysiwyg;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the configuration settings for the FCKEditor.<br>
 * Adding element to this collection you can override the settings specified in
 * the config.js file.
 * 
 * @version $Id: FCKeditorConfig.java 1905 2008-04-10 15:32:00Z th-schwarz $
 */
public class FCKEditorConfig extends HashMap<String, String>
{

   private static final long serialVersionUID = -4831190504944866644L;

   /**
    * Initialize the configuration collection
    */
   public FCKEditorConfig()
   {
      super();
   }

   /**
    * Generate the url parameter sequence used to pass this configuration to
    * the editor.
    * 
    * @return html endocode sequence of configuration parameters
    */
   public String getUrlParams()
   {
      StringBuffer osParams = new StringBuffer();
      for (Map.Entry<String, String> entry : this.entrySet())
      {
         osParams.append("&");
         osParams.append(encodeConfig(entry.getKey()));
         osParams.append("=");
         osParams.append(encodeConfig(entry.getValue()));
      }
      return osParams.toString();
   }

   private String encodeConfig(String s)
   {
      s = s.replaceAll("&", "%26");
      s = s.replaceAll("=", "%3D");
      s = s.replaceAll("\"", "%22");
      return s;
   }
}
