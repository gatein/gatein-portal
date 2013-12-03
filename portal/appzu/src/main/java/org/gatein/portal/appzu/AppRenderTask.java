/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.appzu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import juzu.impl.common.Completion;
import juzu.impl.common.Tools;
import juzu.impl.compiler.CompilationError;
import juzu.impl.compiler.CompilationException;
import juzu.io.OutputStream;
import juzu.request.Phase;
import org.gatein.portal.content.RenderTask;
import org.gatein.portal.content.Result;
import org.gatein.portal.content.WindowContentContext;
import org.gatein.portal.web.page.Decoder;
import org.w3c.dom.Element;

/**
 * @author Julien Viet
 */
public class AppRenderTask extends RenderTask {

    /** . */
    final AppContent content;

    /** . */
    final WindowContentContext<AppState> contentContext;

    public AppRenderTask(AppContent content, WindowContentContext<AppState> contentContext) {
        this.content = content;
        this.contentContext = contentContext;
    }

    @Override
    public Result.View execute(Locale locale) {
        Map<String, String[]> parameters;
        if (content.getParameters() != null) {
            parameters = new Decoder(content.getParameters()).decode().getParameters();
        } else {
            parameters = Collections.emptyMap();
        }

        //
        try {
            // Refresh application
            Completion<Boolean> completion = content.app.refresh();
            if (completion.isFailed()) {
                Exception cause = completion.getCause();
                if (cause instanceof CompilationException) {
                    CompilationException ce = (CompilationException) cause;
                    StringBuilder buffer = new StringBuilder();
                    // Get length of max line number
                    int max = 0;
                    for (CompilationError error : ce.getErrors()) {
                        max = Math.max(max, error.getLocation().getLine() + 3);
                    }
                    int length = Integer.toString(max).length();
                    for (CompilationError error : ce.getErrors()) {
                        buffer.append("<section>");
                        buffer.append("<div class=\"alert alert-error\">").append(error.getMessage()).append("</p><pre>");
                        File source = error.getSourceFile();
                        if (source != null) {
                            int line = error.getLocation().getLine();
                            int from = line - 2;
                            int to = line + 3;
                            BufferedReader reader = new BufferedReader(new FileReader(source));
                            int count = 1;
                            for (String s = reader.readLine();s != null;s = reader.readLine()) {
                                if (count >= from && count < to) {
                                    String number = Integer.toString(count);
                                    for (int i = number.length();i < length;i++) {
                                        buffer.append(" ");
                                    }
                                    buffer.append(number);
                                    buffer.append("   ").append(s).append("\n");
                                }
                                count++;
                            }
                            buffer.append("</pre>");
                        }
                        buffer.append("</section>");
                    }
                    return new Result.Fragment(
                            Collections.<Map.Entry<String, String>>emptyList(),
                            Collections.<Element>emptyList(),
                            content.resolveTitle(Locale.ENGLISH),
                            buffer.toString()
                    );
                } else {
                    return new Result.Error(true, completion.getCause());
                }
            }

            // Invoke
            juzu.request.Result result = content.invoke(contentContext, Phase.VIEW, parameters);
            if (result instanceof juzu.request.Result.Status) {
                juzu.request.Result.Status status = (juzu.request.Result.Status) result;
                if (status.streamable != null) {
                    StringWriter buffer = new StringWriter();
                    OutputStream stream = OutputStream.create(Tools.UTF_8, buffer);
                    status.streamable.send(stream);
                    return new Result.Fragment(
                            Collections.<Map.Entry<String, String>>emptyList(),
                            Collections.<Element>emptyList(),
                            content.resolveTitle(Locale.ENGLISH),
                            buffer.toString());
                }
            } else if (result instanceof juzu.request.Result.Error) {
                juzu.request.Result.Error appError = (juzu.request.Result.Error) result;
                return new Result.Error(true, appError.cause);
            } {
                return new Result.Fragment(
                        Collections.<Map.Entry<String, String>>emptyList(),
                        Collections.<Element>emptyList(),
                        content.resolveTitle(Locale.ENGLISH),
                        "Unhandled result " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(true, e);
        }
    }
}
