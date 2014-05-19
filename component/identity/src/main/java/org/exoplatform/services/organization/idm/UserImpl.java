/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.services.organization.idm;

import java.io.Serializable;


/**
 * TODO: This is temporary implementation, which should be removed after https://issues.jboss.org/browse/EXOJCR-1780 will be
 * fixed and available in GateIn.
 * Because issue https://issues.jboss.org/browse/EXOJCR-1780 was fixed and this class will be removed soon.
 * You should use {@org.exoplatform.services.organization.impl.UserImpl} instead of this class for avoiding error in future.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Deprecated
public class UserImpl extends org.exoplatform.services.organization.impl.UserImpl implements Serializable {
    private String displayName;

    public UserImpl() {
        super();
    }

    public UserImpl(String username) {
        super(username);
    }

    @Override
    public String getFullName() {
        return displayName != null ? displayName : getFirstName() + " " + getLastName();
    }

    @Override
    public void setFullName(String fullName) {
        this.displayName = fullName;
    }

    @Override
    public void setDisplayName(String displayName) {
        if (displayName != null) {
            this.displayName = displayName.trim().isEmpty() ? null : displayName;
        } else {
            this.displayName = displayName;
        }
    }

    @Override
    public String getDisplayName() {
        return this.displayName != null ? this.displayName : this.getFirstName() + " " + this.getLastName();
    }
}
