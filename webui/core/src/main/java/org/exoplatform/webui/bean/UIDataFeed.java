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

package org.exoplatform.webui.bean;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.NoSuchDataException;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 26,
 * 2009
 */
public interface UIDataFeed
{

   public void setDataSource(PageList datasource) throws Exception;

   /***
    * Load data of next page. Throws DataMissingException when page's data is cannot load
    * @throws NoSuchDataException
    * @throws Exception
    */
   public void feedNext() throws NoSuchDataException, Exception;

   public boolean hasNext();
}
