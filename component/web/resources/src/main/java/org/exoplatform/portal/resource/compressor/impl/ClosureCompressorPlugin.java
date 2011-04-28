/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.portal.resource.compressor.impl;

import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.WarningLevel;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.resource.compressor.BaseResourceCompressorPlugin;
import org.exoplatform.portal.resource.compressor.ResourceCompressorException;
import org.exoplatform.portal.resource.compressor.ResourceType;
import java.io.Reader;
import java.io.Writer;

public class ClosureCompressorPlugin extends BaseResourceCompressorPlugin
{

   public ClosureCompressorPlugin(InitParams params) throws Exception
   {
      super(params);
   }

   public ResourceType getResourceType()
   {
      return ResourceType.JAVASCRIPT;
   }

   public void compress(Reader input, Writer output) throws ResourceCompressorException
   {
      Compiler compiler = new Compiler();
      CompilerOptions options = new CompilerOptions();
      CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(options);
      WarningLevel.QUIET.setOptionsForWarningLevel(options);
      JSSourceFile extern = JSSourceFile.fromCode("extern", "");
      
      JSSourceFile jsInput;
      try
      {
         String code = JSSourceFile.fromReader("code", input).getCode();
         jsInput = JSSourceFile.fromCode("jsInput", code);
         compiler.compile(extern, jsInput, options);
         output.write(compiler.toSource());
      }
      catch (Exception ex)
      {
         throw new ResourceCompressorException(ex);
      }
   }
}
