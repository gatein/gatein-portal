/*
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

package org.exoplatform.portal.mop;

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;

/**
 * An object that has a surrounding configurable decoration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@MixinType(name = "gtn:decorated")
public abstract class Decorated
{

   @Property(name = "gtn:showinfobar", defaultValue = "false")
   public abstract boolean getShowInfoBar();

   public abstract void setShowInfoBar(boolean showInfoBar);

   @Property(name = "gtn:showmode", defaultValue = "false")
   public abstract boolean getShowMode();

   public abstract void setShowMode(boolean showMode);

   @Property(name = "gtn:showwindowstate", defaultValue = "false")
   public abstract boolean getShowWindowState();

   public abstract void setShowWindowState(boolean showWindowState);

   @Property(name = "gtn:theme")
   public abstract String getTheme();

   public abstract void setTheme(String theme);

}
