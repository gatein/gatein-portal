/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.wsrp.webui.component;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputSet;

import java.io.Writer;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class UIWSRPFormInputSet extends UIFormInputSet
{
   public UIWSRPFormInputSet(String name)
   {
      super(name);
   }

   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      if (getComponentConfig() != null)
      {
         super.processRender(context);
         return;
      }

      StringBuilder sb = new StringBuilder(512);
      sb.append("<div class=\"UIFormInputSet\">");

      ResourceBundle res = context.getApplicationResourceBundle();
      UIForm uiForm = getAncestorOfType(UIForm.class);
      for (UIComponent inputEntry : getChildren())
      {
         if (inputEntry.isRendered())
         {
            sb.append("<div class=\"row\">");

            String label;
            try
            {
               label = uiForm.getLabel(res, inputEntry.getId());
               if (inputEntry instanceof UIFormInputBase)
               {
                  ((UIFormInputBase)inputEntry).setLabel(label);
               }
            }
            catch (MissingResourceException ex)
            {
               //label = "&nbsp;" ;
               label = inputEntry.getName();
               System.err.println("\n " + uiForm.getId() + ".label." + inputEntry.getId() + " not found value");
            }

            sb.append("<label>").append(label).append("</label>");
            renderUIComponent(inputEntry);

            sb.append("</div>");
         }
      }
      sb.append("</div>");

      Writer w = context.getWriter();
      w.write(sb.toString());
   }
}
