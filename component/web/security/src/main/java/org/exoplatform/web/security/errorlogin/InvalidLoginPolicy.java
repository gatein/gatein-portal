/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
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
package org.exoplatform.web.security.errorlogin;

/**
 * Policy for indication that invalid login attempts are coming from same source. For example: <br>
 * If value is SERVER, then mail will be send after detection of 3 successive invalid login attempts from same remote server.<br>
 * If value is SESSION, then mail will be send after detection of 3 successive invalid login attempts from same HTTP session.<br>
 * etc.
 *
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public enum InvalidLoginPolicy {
    /**
     * SESSION is default and it means that login attempts are coming from same HTTP session.
     */
    SESSION,

    /**
     * SESSION_AND_USER is indicating login attempts of same user and from same HTTP session.
     */
    SESSION_AND_USER,

    /**
     * SERVER means login attempts from same remote server.
     */
    SERVER

}
