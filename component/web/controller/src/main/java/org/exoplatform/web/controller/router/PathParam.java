/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.PathParamDescriptor;
import org.exoplatform.web.controller.regexp.RENode;
import org.exoplatform.web.controller.regexp.REParser;
import org.exoplatform.web.controller.regexp.RERenderer;
import org.exoplatform.web.controller.regexp.SyntaxException;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class PathParam extends Param
{

   static PathParam create(QualifiedName name)
   {
      return create(new PathParamDescriptor(name));
   }

   static PathParam create(PathParamDescriptor descriptor)
   {
      if (descriptor == null)
      {
         throw new NullPointerException("No null descriptor accepted");
      }

      //
      String regex = null;
      EncodingMode encodingMode = EncodingMode.FORM;
      if (descriptor != null)
      {
         regex = descriptor.getPattern();
         encodingMode = descriptor.getEncodingMode();
      }

      //
      if (regex == null)
      {
         if (encodingMode == EncodingMode.FORM)
         {
            regex = ".+";
         }
         else
         {
            regex = "[^/]+";
         }
      }

      // Now work on the regex
      StringBuilder renderingRegex = new StringBuilder();
      StringBuilder routingRegex = new StringBuilder();
      try
      {
         REParser parser = new REParser(regex);

         //
         RENode.Disjunction routingDisjunction = parser.parseDisjunction();
         if (encodingMode == EncodingMode.FORM)
         {
            RouteEscaper escaper = new RouteEscaper('/', '_');
            escaper.visit(routingDisjunction);
         }
         new RERenderer().render(routingDisjunction, routingRegex);

         //
         parser.reset();
         RENode.Disjunction renderingDisjunction = parser.parseDisjunction();
         renderingRegex.append("^");
         new RERenderer().render(renderingDisjunction, renderingRegex);
         renderingRegex.append("$");
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      catch (SyntaxException e)
      {
         throw new RuntimeException(e);
      }
      catch (MalformedRouteException e)
      {
         throw new RuntimeException(e);
      }

      //
      return new PathParam(
         descriptor.getQualifiedName(),
         encodingMode,
         routingRegex.toString(),
         renderingRegex.toString());
   }

   /** . */
   final EncodingMode encodingMode;

   /** . */
   final String routingRegex;

   /** . */
   final Pattern renderingPattern;

   PathParam(
      QualifiedName name,
      EncodingMode encodingMode,
      String routingRegex,
      String renderingRegex)
   {
      super(name);

      //
      if (renderingRegex == null)
      {
         throw new NullPointerException("No null pattern accepted");
      }

      //
      this.encodingMode = encodingMode;
      this.routingRegex = routingRegex;
      this.renderingPattern = Pattern.compile(renderingRegex);
   }

   @Override
   public String toString()
   {
      return "PathParam[name=" + name + ",encodingMode=" + encodingMode + ",pattern=" + renderingPattern + "]";
   }
}
