/******************************************************************************
 * JBoss by Red Hat                                                           *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.web.security;

import static nl.captcha.Captcha.NAME;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;
import nl.captcha.servlet.CaptchaServletUtil;

/**
 * @author <a href="mailto:theute@redhat.com">Thomas Heute</a>
 * @version $Revision$
 */
public class CaptchaServlet extends HttpServlet
{

   private static final long serialVersionUID = 1L;

   private static final String PARAM_HEIGHT = "height";

   private static final String PARAM_WIDTH = "width";

   protected int _width = 200;

   protected int _height = 50;

   @Override
   public void init() throws ServletException
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

   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      HttpSession session = req.getSession();
      Captcha captcha;
      if (session.getAttribute(NAME) == null)
      {
         captcha = new Captcha.Builder(_width, _height).addText().gimp().addNoise().addBackground().build();

         session.setAttribute(NAME, captcha);
         CaptchaServletUtil.writeImage(resp, captcha.getImage());

         return;
      }

      captcha = (Captcha) session.getAttribute(NAME);
      CaptchaServletUtil.writeImage(resp, captcha.getImage());
   }

}
