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
package org.gatein.portal.appzu.ui;

import javax.inject.Inject;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.impl.common.Name;
import juzu.template.Template;
import org.gatein.portal.appzu.App;
import org.gatein.portal.appzu.ApplicationRepository;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    ApplicationRepository repository;

    @Inject
    @Path("index.gtmpl")
    Template index;

    @Inject
    Flash flash;

    @View
    public Response.Content index() {
        return index.with().set("repository", repository).ok();
    }

    @Action
    public Response.View add(String name, String displayName, String templateId) throws Exception {
        if (displayName == null || displayName.length() == 0) {
            displayName = name;
        }
        try {
            repository.addApplication(Name.parse(name), displayName, templateId);
            flash.success = "Application " + name + " created";
        } catch (Exception e) {
            flash.error = e.getMessage();
        }
        return Controller_.index();
    }

    @Action
    public Response start(String name) throws Exception {
        App app = repository.getApplication(Name.parse(name));
        if (app != null) {
            app.refresh().get();
            return Controller_.index();
        } else {
            return Response.error("Application " + name + " does not exists");
        }
    }
}
