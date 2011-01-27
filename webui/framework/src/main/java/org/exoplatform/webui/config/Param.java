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

package org.exoplatform.webui.config;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.xml.object.XMLObject;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.InputStream;

public class Param
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(Param.class);

   private String name;

   private String value;

   private transient Object object;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
   }

   public Object getMapXMLObject(WebuiRequestContext context) throws Exception
   {
      if(object == null)
      {
        synchronized (this)
        {
          if(object == null)
          {
            ResourceResolver resolver = context.getResourceResolver(value);
            InputStream is = resolver.getInputStream(value);
            object = XMLObject.getObject(is);
            is.close();
          }
        }
      }
      return object;
   }

   @SuppressWarnings("unchecked")
   public Object getMapGroovyObject(WebuiRequestContext context) throws Exception
   {
      if(object == null)
      {
        synchronized (this)
        {
          if(object == null)
          {
            ResourceResolver resolver = context.getResourceResolver(value);
            InputStream is = resolver.getInputStream(value);
            Binding binding = new Binding();
            GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding);
            object = shell.evaluate(is);
            is.close();
          }
        }
      }
      return object;
   }

   public Object getFreshObject(WebuiRequestContext context) throws Exception
   {
      try
      {
         ResourceResolver resolver = context.getResourceResolver(value);
         InputStream is = resolver.getInputStream(value);
         Binding binding = new Binding();
         GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding);
         object = shell.evaluate(is);
         is.close();
         return object;
      }
      catch (Exception e)
      {
         log.error("A  problem in the groovy script : " + value, e);
         throw e;
      }
   }
}