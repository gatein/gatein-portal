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

package org.exoplatform.portal.application;

import org.exoplatform.commons.utils.*;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.portal.resource.ResourceRenderer;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.imageio.ImageIO;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceRequestFilter extends AbstractFilter
{

   protected static Log log = ExoLogger.getLogger(ResourceRequestFilter.class);

   private static final Charset UTF_8 = Charset.forName("UTF-8");

   private FilterConfig cfg;

   private ImageType[] imageTypes = ImageType.values();

   private ConcurrentMap<String, FutureTask<Image>> mirroredImageCache = new ConcurrentHashMap<String, FutureTask<Image>>();

   public static final String IF_MODIFIED_SINCE     = "If-Modified-Since";
 
   public static final String LAST_MODIFIED     = "Last-Modified";    
   
   public void afterInit(FilterConfig filterConfig)
   {
      cfg = filterConfig;
      log.info("Cache eXo Resource at client: " + !PropertyManager.isDevelopping());
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      final String uri = URLDecoder.decode(httpRequest.getRequestURI(), "UTF-8");
      final HttpServletResponse httpResponse = (HttpServletResponse)response;
      ExoContainer portalContainer = getContainer();
      final SkinService skinService = (SkinService) portalContainer.getComponentInstanceOfType(SkinService.class);
      long ifModifiedSince = httpRequest.getDateHeader(IF_MODIFIED_SINCE);

      //
      if (uri.endsWith(".css"))
      {
//     Check if cached resource has not been modifed, return 304 code      
         long cssLastModified = skinService.getLastModified(uri);
         if (isNotModified(ifModifiedSince, cssLastModified)) {
            httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
         }
         response.setContentType("text/css; charset=UTF-8");
         
         final OutputStream out = response.getOutputStream();
         final BinaryOutput output = new BinaryOutput()
         {
            public Charset getCharset()
            {
               return UTF_8;
            }
            public void write(byte b) throws IOException
            {
               out.write(b);
            }
            public void write(byte[] bytes) throws IOException
            {
               out.write(bytes);
            }
            public void write(byte[] bytes, int off, int len) throws IOException
            {
               out.write(bytes, off, len);
            }
         };
         ResourceRenderer renderer = new ResourceRenderer()
         {
            public BinaryOutput getOutput() throws IOException
            {
               return output;
            }
            public void setExpiration(long seconds)
            {
               if (seconds > 0)
               {
                  httpResponse.addHeader("Cache-Control", "max-age=" + seconds + ",s-maxage=" + seconds);
               }
               else
               {
                  httpResponse.setHeader("Cache-Control", "no-cache");
               }

               long lastModified = skinService.getLastModified(uri);
               processIfModified(lastModified, httpResponse);
            }
         };

         //
         try
         {
            skinService.renderCSS(renderer, uri);
            if (log.isDebugEnabled())
            {
               log.debug("Use a merged CSS: " + uri);
            }
         }
         catch (Exception e)
         {
            if (e instanceof SocketException)
            {
               //Should we print something/somewhere exception message
            }
            else
            {
               log.error("Could not render css " + uri, e);
               httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
         }
      }
      else
      {

         // Fast matching
         final int len = uri.length();
         if (len >= 7 && uri.charAt(len - 7) == '-' && uri.charAt(len - 6) == 'r' && uri.charAt(len - 5) == 't')
         {
            for (final ImageType imageType : imageTypes)
            {
               if (imageType.matches(uri))
               {
                  final String resource =
                     uri.substring(httpRequest.getContextPath().length(), len - 7) + uri.substring(len - 4);
                  FutureTask<Image> futureImg = mirroredImageCache.get(resource);
                  if (futureImg == null)
                  {
                     FutureTask<Image> tmp = new FutureTask<Image>(new Callable<Image>()
                     {
                        public Image call() throws Exception
                        {
                           InputStream in = cfg.getServletContext().getResourceAsStream(resource);
                           if (in == null)
                           {
                              return null;
                           }

                           //
                           BufferedImage img = ImageIO.read(in);
                           log.debug("Read image " + uri + " (" + img.getWidth() + "," + img.getHeight() + ")");
                           AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
                           tx.translate(-img.getWidth(null), 0);
                           AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                           img = op.filter(img, null);
                           log.debug("Mirrored image " + uri + " (" + img.getWidth() + "," + img.getHeight() + ")");
                           ByteArrayOutputStream baos = new ByteArrayOutputStream(1000);
                           ImageIO.write(img, imageType.getFormat(), baos);
                           baos.close();
                           return new Image(imageType, baos.toByteArray());
                        }
                     });

                     //
                     futureImg = mirroredImageCache.putIfAbsent(resource, tmp);
                     if (futureImg == null)
                     {
                        futureImg = tmp;
                        futureImg.run();
                     }
                  }

                  //
                  try
                  {
                     Image img = futureImg.get();
                     if (img != null)
                     {
                        //Check if cached resource has not been modifed, return 304 code      
                        long imgLastModified = img.getLastModified();
                        if (isNotModified(ifModifiedSince, imgLastModified)) {
                           httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                           return;
                        }
                        httpResponse.setContentType(img.type.getMimeType());
                        httpResponse.setContentLength(img.bytes.length);
                        processIfModified(imgLastModified, httpResponse);
                        
                        OutputStream out = httpResponse.getOutputStream();
                        out.write(img.bytes);
                        out.close();
                     }
                     else
                     {
                        mirroredImageCache.remove(resource);
                        httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                     }
                     return;
                  }
                  catch (InterruptedException e)
                  {
                     // Find out what is relevant to do
                     e.printStackTrace();
                  }
                  catch (ExecutionException e)
                  {
                     // Cleanup
                     e.printStackTrace();
                     mirroredImageCache.remove(resource);
                  }
               }
            }
         }

         //
         if (!PropertyManager.isDevelopping())
         {
            httpResponse.addHeader("Cache-Control", "max-age=2592000,s-maxage=2592000");
         }
         else
         {
            if (uri.endsWith(".jstmpl") || uri.endsWith(".js"))
            {
               httpResponse.setHeader("Cache-Control", "no-cache");
            }
            if (log.isDebugEnabled())
               log.debug(" Load Resource: " + uri);
         }
         chain.doFilter(request, response);
      }
   }

   /**
    * Add Last-Modified Http header to HttpServetResponse
    */
   public void processIfModified(long lastModified, HttpServletResponse httpResponse) {
      httpResponse.setDateHeader(ResourceRequestFilter.LAST_MODIFIED, lastModified);
   }

   /**
    * If cached resource has not changed since date in http header (If_Modified_Since), return true
    * Else return false;
    * @param ifModifedSince - String, and HttpHeader element
    * @param lastModified
    * @param httpResponse
    * @return
    */
   public boolean isNotModified(long ifModifedSince, long lastModified) {
      if (!PropertyManager.isDevelopping()) {
         if (ifModifedSince >= lastModified) {
            return true;
         }
      }
      return false;
   }
   
   public void destroy()
   {
   }
}
