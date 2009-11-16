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

package org.exoplatform.services.resources;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A {@link ClassLoader} extension that will retrieve resources from the parent
 * classloader. For each resource having a ".properties" suffix it the
 * classloader will try first to locate a corresponding resource using the same
 * base name but with an ".xml" suffix. If such a resource is found, it will be
 * loaded using {@link XMLResourceBundleParser}
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertiesClassLoader extends ClassLoader
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(PropertiesClassLoader.class);

   /**
    * Indicates whether the resource must be encoded into UTF-8
    */
   private boolean unicode;

   public PropertiesClassLoader(ClassLoader parent)
   {
      this(parent, false);
   }

   public PropertiesClassLoader(ClassLoader parent, boolean unicode)
   {
      super(parent);
      this.unicode = unicode;
   }

   public PropertiesClassLoader()
   {
   }

   @Override
   public URL getResource(String name)
   {
      if (name.endsWith(".properties"))
      {
         URL url = null;
         ClassLoader parent = getParent();
         while (parent != null)
         {
            url = getResource(parent, name, unicode);
            if (url != null)
            {
               return url;
            }
            parent = parent.getParent();
         }
         if (url == null)
         {
            url = getResource(this, name, unicode);
         }
         return url;
      }
      return super.getResource(name);
   }

   private static URL getResource(ClassLoader cl, String name, boolean unicode)
   {
      String xmlName = name.substring(0, name.length() - ".properties".length()) + ".xml";
      URL url = getResource(cl, xmlName, unicode, true);
      if (url == null)
      {
         url = getResource(cl, name, unicode, false);
      }
      return url;
   }

   private static URL getResource(ClassLoader cl, String name, boolean unicode, boolean xml)
   {
      Enumeration<URL> urls = null;
      try
      {
         urls = cl.getResources(name);
      }
      catch (Exception e)
      {
         LOG.error("An error occured while seeking all the resources with the name " + name, e);
         return null;
      }
      if (urls != null && urls.hasMoreElements())
      {
         // At least one such resource has been found
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         URL url = null;
         Map<Object, Object> props = unicode ? new TreeMap<Object, Object>() : new Properties();
         while (urls.hasMoreElements())
         {
            url = urls.nextElement();
            InputStream in = null;
            try
            {
               // Load content
               in = url.openStream();
               if (xml)
               {
                  props.putAll(XMLResourceBundleParser.asProperties(in));
               }
               else
               {
                  byte[] buf = IOUtil.getStreamContentAsBytes(in);
                  ExoResourceBundle bundle = new ExoResourceBundle(new String(buf, "UTF-8"));
                  bundle.putAll(props);
               }
            }
            catch (Exception e)
            {
               LOG.error("An error occured while loading the content of " + url, e);
               return null;
            }
            finally
            {
               if (in != null)
               {
                  try
                  {
                     in.close();
                  }
                  catch (IOException e)
                  {
                     // Do nothing
                  }
               }
            }
         }
         try
         {
            // Now serialize as binary
            if (unicode)
            {
               // Encoded into UTF-8
               for (Map.Entry<Object, Object> entry : props.entrySet())
               {
                  out.write(((String)entry.getKey()).getBytes("UTF-8"));
                  out.write('=');
                  out.write(((String)entry.getValue()).getBytes("UTF-8"));
                  out.write('\n');
               }
            }
            else
            {
               // Properties format : encoded into ISO-8859-1 with unicode characters
               ((Properties)props).store(out, null);
            }
            out.close();
            InputStream in = new ByteArrayInputStream(out.toByteArray());

            //
            return new URL(url, "", new InputStreamURLStreamHandler(in));
         }
         catch (Exception e)
         {
            LOG.error("An error occured while creating the content of " + url, e);
            return null;
         }
      }
      return null;
   }
}
