/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.portal.resource;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class ResourceNotFoundException extends RuntimeException
{
   private String resourcePath;
   
   private String errorType;
   
   private final static String INVALID_RESOURCE_PATH = "invalid resource path";
   
   private final static String RESOURCE_RESOLVER_WRONG_PROCESSING = "wrong processing in resource resolver";
   
   public ResourceNotFoundException(String _resourcePath, String message)
   {
      super(message);
      resourcePath = _resourcePath;
      errorType = INVALID_RESOURCE_PATH;
   }
   
   public ResourceNotFoundException(String _resourcePath, String _errorType, String message)
   {
      super(message);
      resourcePath = _resourcePath;
      errorType = _errorType;
   }
   
   public String getResourcePath()
   {
      return resourcePath;
   }
   
   public String getErrorType()
   {
      return errorType;
   }
}
