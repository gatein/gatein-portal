/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.migration.mapping;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.common.util.ParameterValidation;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ExportErrorMapping.NODE_NAME)
public abstract class ExportErrorMapping
{
   public static final String NODE_NAME = "wsrp:exporterror";
   private static final String COMMA = ",";

   @Property(name = "errorcode")
   public abstract String getCode();

   public abstract void setCode(String code);

   @Property(name = "handles")
   public abstract String getHandles();

   public abstract void setHandles(String handles);

   public QName getErrorCode()
   {
      return QName.valueOf(getCode());
   }

   public List<String> getPortletHandles()
   {
      return Arrays.asList(getHandles().split(COMMA));
   }

   public void initFrom(QName errorCode, List<String> handles)
   {
      setCode(errorCode.toString());

      if (ParameterValidation.existsAndIsNotEmpty(handles))
      {
         StringBuilder sb = new StringBuilder();
         int size = handles.size();
         int index = 0;
         for (String handle : handles)
         {
            sb.append(handle);
            if (index++ < size)
            {
               sb.append(COMMA);
            }
         }
         setHandles(sb.toString());
      }
   }
}
