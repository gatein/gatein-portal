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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.commons.utils.Safe;
import org.gatein.portal.wsrp.state.JCRPersister;
import org.gatein.portal.wsrp.state.mapping.BaseMapping;
import org.gatein.wsrp.consumer.migration.ExportInfo;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ExportInfoMapping.NODE_NAME)
public abstract class ExportInfoMapping implements BaseMapping<ExportInfo>
{
   public static final String NODE_NAME = "wsrp:exportinfo";

   @Property(name = "exporttime")
   public abstract long getExportTime();
   public abstract void setExportTime(long exportTime);

   @Property(name = "expirationtime")
   public abstract long getExpirationTime();
   public abstract void setExpirationTime(long expirationTime);

   @Property(name = "exportcontext")
   public abstract InputStream getExportContext();
   public abstract void setExportContext(InputStream exportContext);

   @OneToMany
   public abstract List<ExportedStateMapping> getExportedStates();

   @Create
   public abstract ExportedStateMapping internalCreateExportedState(String portletHandle);

   public ExportedStateMapping createExportedState(String portletHandle)
   {
      return internalCreateExportedState(JCRPersister.PortletNameFormatter.encode(portletHandle));
   }

   @OneToMany
   protected abstract List<ExportErrorMapping> getErrors();

   @Create
   public abstract ExportErrorMapping internalCreateError(String errorCode);

   public ExportErrorMapping createError(String errorCode)
   {
      return internalCreateError(JCRPersister.QNameFormatter.encode(errorCode));
   }

   public void initFrom(ExportInfo exportInfo)
   {
      setExportTime(exportInfo.getExportTime());
      setExpirationTime(exportInfo.getExpirationTime());

      byte[] exportContext = exportInfo.getExportContext();
      if(exportContext != null && exportContext.length > 0)
      {
         ByteArrayInputStream is = new ByteArrayInputStream(exportContext);
         setExportContext(is);
      }

      List<ExportedStateMapping> exportedStates = getExportedStates();
      exportedStates.clear();
      for (String handle : exportInfo.getExportedPortletHandles())
      {
         ExportedStateMapping exportedState = createExportedState(handle);

         // add then init idiom
         exportedStates.add(exportedState);
         exportedState.initFrom(handle, exportInfo.getPortletStateFor(handle));
      }

      List<ExportErrorMapping> errors = getErrors();
      errors.clear();
      for (Map.Entry<QName, List<String>> entry : exportInfo.getErrorCodesToFailedPortletHandlesMapping().entrySet())
      {
         QName errorCode = entry.getKey();
         ExportErrorMapping error = createError(errorCode.toString());

         // add then init idiom
         errors.add(error);
         error.initFrom(errorCode, entry.getValue());
      }
   }

   public ExportInfo toModel(ExportInfo initial)
   {
      List<ExportedStateMapping> exportedStates = getExportedStates();
      SortedMap<String, byte[]> states = new TreeMap<String,byte[]>();
      for (ExportedStateMapping exportedState : exportedStates)
      {
         states.put(JCRPersister.PortletNameFormatter.decode(exportedState.getHandle()), Safe.getBytes(exportedState.getState()));
      }

      List<ExportErrorMapping> errors = getErrors();
      SortedMap<QName, List<String>> errorCodesToHandles = new TreeMap<QName, List<String>>();
      for (ExportErrorMapping error : errors)
      {
         errorCodesToHandles.put(error.getErrorCode(), error.getPortletHandles());
      }

      return new ExportInfo(getExportTime(), errorCodesToHandles, states, Safe.getBytes(getExportContext()));
   }
}
