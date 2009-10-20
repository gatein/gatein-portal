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

package org.exoplatform.services.xml.serialize;

import org.exoplatform.services.xml.parser.XMLDocument;
import org.exoplatform.services.xml.parser.XMLNode;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Apr 21, 2007  
 */
public interface BeanMapper
{

   public <T> T toBean(Class<T> clazz, XMLDocument document) throws Exception;

   public <T> T toBean(Class<T> clazz, XMLNode node) throws Exception;

   public <T> void toBean(Class<T> clazz, T object, XMLNode node) throws Exception;
}
