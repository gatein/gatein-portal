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

package org.exoplatform.web.command.handler;

import org.exoplatform.web.WebAppController;
import org.exoplatform.web.command.Command;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Ba Uoc
 *          thuy.le@exoplatform.com
 * July 24, 2007
 */
public class HelloJCRHandler extends Command
{

   public void execute(WebAppController controller, HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      res.setContentType("text/xml");
      PrintWriter out = res.getWriter();
      out.println("Hello from server");
      System.out.println("Client request");
   }
}