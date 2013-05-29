/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
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

package org.gatein.web.security.impersonation;

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Provides info about identity of impersonated user and encapsulates identity of "original" admin user
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:ocarr@redhat.com">Oliver Carr</a>
 */
public class ImpersonatedIdentity extends Identity {
    private final ConversationState parentConversationState;

    public ImpersonatedIdentity(Identity impersonatedIdentity, ConversationState parentConversationState) {
        super(impersonatedIdentity.getUserId(), impersonatedIdentity.getMemberships(), impersonatedIdentity.getRoles());
        this.parentConversationState = parentConversationState;
    }

    public ConversationState getParentConversationState()  {
        return parentConversationState;
    }

}
