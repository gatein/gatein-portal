/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.account.webui.component;

import nl.captcha.Captcha;
import nl.captcha.servlet.CaptchaServletUtil;
import org.exoplatform.webui.application.portlet.PortletApplicationController;

import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceServingPortlet;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static nl.captcha.Captcha.NAME;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RegisterPortletApplicationController extends PortletApplicationController implements ResourceServingPortlet
{

   private static final String PARAM_HEIGHT = "height";

   private static final String PARAM_WIDTH = "width";

   protected int _width = 200;

   protected int _height = 50;

   @Override
   public void init() throws PortletException
   {
      if (getInitParameter(PARAM_HEIGHT) != null)
      {
         _height = Integer.valueOf(getInitParameter(PARAM_HEIGHT));
      }

      if (getInitParameter(PARAM_WIDTH) != null)
      {
         _width = Integer.valueOf(getInitParameter(PARAM_WIDTH));
      }
   }

   public void serveResource(ResourceRequest req, ResourceResponse resp)
      throws PortletException, java.io.IOException
   {
      PortletSession session = req.getPortletSession();
      Captcha captcha;
      if (session.getAttribute(NAME) == null)
      {
         captcha = new Captcha.Builder(_width, _height).addText().gimp().addNoise().addBackground().build();


         session.setAttribute(NAME, captcha);
         writeImage(resp, captcha.getImage());

         return;
      }

      captcha = (Captcha)session.getAttribute(NAME);
      writeImage(resp, captcha.getImage());

   }

   public static void writeImage(ResourceResponse response, BufferedImage bi)
   {
      response.setProperty("Cache-Control", "private,no-cache,no-store");
      response.setContentType("image/png");   // PNGs allow for transparency. JPGs do not.
      try
      {
         CaptchaServletUtil.writeImage(response.getPortletOutputStream(), bi);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}
