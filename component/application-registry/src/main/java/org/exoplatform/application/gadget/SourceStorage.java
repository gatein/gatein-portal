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
    * Return {@link Source} object of gadget that name provided in gadget object param <br/>
    * If can't find gadget with that name, return null <br/>
    * If gadget is remote, throw Exception <br/>
    * @param gadget - Gadget object used to retrieved gadget's name
    */
   public Source getSource(Gadget gadget) throws Exception;

   /**
    * This method will save source to database to a specify path
    * @param dirPath
    * @param source
    * @throws Exception
    */
   public void saveSource(Gadget gadget, Source source) throws Exception;

   /**
    * julien : this method does nothing and should be removed since now deleting a gadget
    * in the gadget registry also deletes the source for the local gadgets.
    *
    * @param sourcePath the source path
    * @throws Exception any exception
    */
   public void removeSource(String sourcePath) throws Exception;

}