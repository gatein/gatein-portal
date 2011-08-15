/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.portal.gadget.core;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An endpoint to send requests to Shindig from Portal.
 *
 * This endpoint is necessary as Shindig does not expose any public API to manipulates
 * its caches from Portal.
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 8/15/11
 */
public class ShindigClientEndpoint
{

   private final long delay;

   private final Timer timer;

   public ShindigClientEndpoint(InitParams params) throws Exception
   {
      long delayTime = 1000;
      if(params != null)
      {
         ValueParam delayParam = params.getValueParam("delayTime");
         delayTime = Long.parseLong(delayParam.getValue());
      }
      delay = delayTime;
      timer = new Timer(true);
   }

   /**
    * Etablish URLConnection to shindigURL and post request data to it
    *
    * @param requestData
    * @param shindigURL
    */
   public void sendRequest(String requestData, String shindigURL)
   {
      timer.schedule(createTimerTask(requestData, shindigURL), delay);
   }

   private TimerTask createTimerTask(final String requestData, final String shindigURL)
   {
      return new TimerTask()
      {
         @Override
         public void run()
         {
            OutputStreamWriter out = null;
            InputStream in = null;

            try
            {
               URLConnection conn = new URL(shindigURL).openConnection();
               conn.setDoOutput(true);
               out = new OutputStreamWriter(conn.getOutputStream());
               out.write(requestData);
               out.flush();

               in = conn.getInputStream(); //Don't remove this if you don't understand!
            }
            catch (IOException ioEx)
            {
               ioEx.printStackTrace();

            }
            finally
            {
               try
               {
                  if (out != null)
                     out.close();
               }
               catch (IOException ex)
               {
                  ex.printStackTrace();
               }

               try
               {
                  if (in != null)
                     in.close();
               }
               catch (IOException ex)
               {
                  ex.printStackTrace();
               }
            }

         }
      };
   }

}
