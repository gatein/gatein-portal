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

package org.exoplatform.services.xml.parser;

import org.exoplatform.services.common.ThreadSoftRef;
import org.exoplatform.services.html.refs.RefsEncoder;
import org.exoplatform.services.token.TokenParser;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          thuan.nhu@exoplatform.com
 * Sep 14, 2006  
 */
public class Services
{

   static ThreadSoftRef<TokenParser> TOKEN_PARSER = new ThreadSoftRef<TokenParser>(TokenParser.class);

   static ThreadSoftRef<RefsEncoder> ENCODER = new ThreadSoftRef<RefsEncoder>(RefsEncoder.class);

}
