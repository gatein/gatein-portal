/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.portal.mop.redirects;


import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@PrimaryType(name = "gtn:redirectDeviceProperty")
public abstract class DeviceProperty
{
   @Property (name="gtn:redirectDevicePropertyName")
   public abstract String getName();
   
   public abstract void setName(String name);
   
   @Property (name="gtn:redirectDevicePropertyGreaterThan")
   public abstract Float getGreaterThan();
   
   public abstract void setGreaterThan(float greaterThan);
   
   @Property (name="gtn:redirectDevicePropertyLessThan")
   public abstract Float getLessThan();
   
   public abstract void setLessThan(float lessThan);
   
   @Property (name="gtn:redirectDevicePropertyEquals")
   public abstract String getEquals();
   
   public abstract void setEquals(String equals);
   
   @Property (name="gtn:redirectDevicePropertyPattern")
   public abstract String getPattern();
   
   public abstract void setPattern(String pattern);

}

