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

package org.exoplatform.application.gadget;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Aug 6, 2008  
 */
public interface SourceStorage
{
   /**
    * This method will get source from a specify source's path in database
    * @param sourcePath
    * @return
    * @throws Exception
    */
   public Source getSource(String sourcePath) throws Exception;

   /**
    * This method will save source to database to a specify path
    * @param dirPath
    * @param source
    * @throws Exception
    */
   public void saveSource(String dirPath, Source source) throws Exception;

   /**
    * This method will remove source from database base on source path
    * @param sourcePath
    * @throws Exception
    */
   public void removeSource(String sourcePath) throws Exception;

   /**
    * This method will get source URI from database. 
    * For example: jcr/repository/collaboration/source/Todo.xml
    * @param sourcePath
    * @return
    */
   public String getSourceURI(String sourcePath);

}