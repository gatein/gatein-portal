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

package org.gatein.portal.api.impl.content;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.Source;
import org.gatein.api.content.Content;
import org.gatein.api.id.Id;
import org.gatein.api.util.Type;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.IdentifiableImpl;

import java.net.URI;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class GadgetImpl extends IdentifiableImpl<org.gatein.api.content.Gadget> implements org.gatein.api.content.Gadget
{
   private final Gadget gadget;

   public GadgetImpl(Id<org.gatein.api.content.Gadget> id, Gadget gadget, GateInImpl gateIn)
   {
      super(id, gadget.getName(), gateIn);
      this.gadget = gadget;
   }

   @Override
   public String toString()
   {
      return (isLocal() ? "Local " : "") + "Gadget '" + getName() + "' @" + getId() + " URI: " + getURI();
   }

   public URI getReferenceURI()
   {
      return URI.create(gadget.getReferenceUrl());
   }

   public URI getURI()
   {
      return URI.create(gadget.getUrl());
   }

   public Data getData()
   {
      if (isLocal())
      {
         return new LocalData()
         {

            public String getSource()
            {
               try
               {
                  return GadgetImpl.this.getSource().getTextContent();
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }

            public void setSource(String source)
            {
               Source initial = GadgetImpl.this.getSource();
               try
               {
                  initial.setTextContent(source);
                  getGateInImpl().getSourceStorage().saveSource(gadget, initial);
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
         };
      }
      else
      {
         return new RemoteData()
         {
            public URI getURI()
            {
               return GadgetImpl.this.getURI();
            }

            public void setURI(URI uri)
            {
               gadget.setUrl(uri.toString());
            }
         };
      }
   }

   private Source getSource()
   {
      try
      {
         return getGateInImpl().getSourceStorage().getSource(gadget);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean isLocal()
   {
      return gadget.isLocal();
   }

   public Type<org.gatein.api.content.Gadget> getType()
   {
      return Content.GADGET;
   }
}
