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

package org.exoplatform.commons.xml;

import org.gatein.common.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ValidationReporter implements ErrorHandler
{

   /** . */
   private final String identifier;

   /** . */
   private boolean valid;

   /** . */
   private Logger log;

   public ValidationReporter(Logger log, String identifier)
   {
      this.identifier = identifier;
      this.log = log;
      this.valid = true;
   }

   public boolean isValid()
   {
      return valid;
   }

   public void warning(SAXParseException exception) throws SAXException
   {
      log.warn(exception.getMessage(), exception);
   }

   public void error(SAXParseException exception) throws SAXException
   {
      log.error("Error in document " + identifier + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
               + ") :" + exception.getMessage());
      valid = false;
   }

   public void fatalError(SAXParseException exception) throws SAXException
   {
      log.error("Fatal error in document " + identifier + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
         + ") :" + exception.getMessage());
      valid = false;
   }
}
