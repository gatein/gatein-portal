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
package org.exoplatform.webui.binding;

import junit.framework.TestCase;

import org.exoplatform.webui.binding.UIMockInputSet.MockModel;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */
public class TestBindingInputSet extends TestCase
{
   public void testBindingWithReadonly() 
   {
      try
      {
         UIMockInputSet input = new UIMockInputSet();
         MockModel model = new MockModel();
         assertEquals(((UIFormStringInput) input.getChildById("value1")).getValue(), "value1");
         assertEquals(((UIFormStringInput) input.getChildById("value1")).isReadOnly(), false);
         
         input.binding(model);
         assertEquals(model.getValue1(), "value1");
         assertEquals(model.getValue2(), "value2");
         assertEquals(model.getValue3(), "value3");
         
         input.setFieldValue("value1", "value1-0");
         
         input.setFieldValue("value2", "value2-0");
         input.setReadonlyForField("value2", true);
         
         input.setFieldValue("value3", "value3-0");
         input.setDisableForField("value3", true);
         
         assertEquals(((UIFormStringInput) input.getChildById("value1")).getValue(), "value1-0");
         assertEquals(((UIFormStringInput) input.getChildById("value2")).getValue(), "value2-0");
         assertEquals(((UIFormStringInput) input.getChildById("value3")).getValue(), "value3-0");
         
         model = new MockModel();
         input.binding(model);
         
         assertEquals(model.getValue1(), "value1-0");
         assertNull(model.getValue2());
         assertNull(model.getValue3());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
