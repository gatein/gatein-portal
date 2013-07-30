/*
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
package org.gatein.portal.ui;

import juzu.Action;
import juzu.Resource;
import juzu.Response;
import juzu.View;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class A {
  
  @View
  public Response index() {
    return Response.ok("<a id='index' href='" + A_.index() + "'>view</a>" +
    		"<a id='action' href='" + A_.action() + "'>action</a>" +
    		"<a id='resource' href='" + A_.resource() + "'>resource</a>");
  }
  
  @Action
  public Response action() {
     //Do something
     return A_.index();
  }
  
  @Resource
  public Response resource() {
      return Response.ok("<html><body><a id='view' href='" + A_.index() + "'>view</a>" +
                  "<a id='action' href='" + A_.action() + "'>action</a></body></html>");
  }
}
