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

package org.exoplatform.webui.form;

import org.exoplatform.webui.form.validator.Validator;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Jun 6, 2006
 * 
 * The base interface to create form elements.
 * This interface is implemented by UIFormInputBase, extend it instead of implementing this interface.
 * @see UIFormInputBase
 */
public interface UIFormInput<E>
{

   public String getName();

   public String getBindingField();

   public String getLabel();

   public <E extends Validator> UIFormInput addValidator(Class<E> clazz, Object... params) throws Exception;

   public List<Validator> getValidators();

   public E getValue() throws Exception;

   public UIFormInput setValue(E value) throws Exception;

   public Class<E> getTypeValue();

   public void reset();

}