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

package org.exoplatform.groovyscript.text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import org.exoplatform.commons.cache.future.FutureCache;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.groovyscript.GroovyTemplateEngine;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.resolver.ResourceKey;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import groovy.lang.Writable;
import groovy.text.Template;

/**
 * Created by The eXo Platform SAS Dec 26, 2005
 */
@Managed
@NameTemplate({ @Property(key = "view", value = "portal"), @Property(key = "service", value = "management"),
        @Property(key = "type", value = "template") })
@ManagedDescription("Template management service")
@RESTEndpoint(path = "templateservice")
public class TemplateService {

    private GroovyTemplateEngine engine_;

    private ExoCache<ResourceKey, GroovyTemplate> templatesCache_;

    private TemplateStatisticService statisticService;

    private boolean cacheTemplate_ = true;

    private final Loader<ResourceKey, GroovyTemplate, ResourceResolver> loader = new Loader<ResourceKey, GroovyTemplate, ResourceResolver>() {
        public GroovyTemplate retrieve(ResourceResolver context, ResourceKey key) throws Exception {
            byte[] bytes;
            InputStream is = context.getInputStream(key.getURL());
            try {
                bytes = IOUtil.getStreamContentAsBytes(is);
                is.close();
            } finally {
                IOTools.safeClose(is);
            }

            // The template class name
            int pos = key.getURL().lastIndexOf('/');
            if (pos == -1) {
                pos = 0;
            }
            String name = key.getURL().substring(pos);

            // Julien: it's a bit dangerious here, with respect to the file encoding...
            String text = new String(bytes);

            // Finally do the expensive template creation
            return engine_.createTemplate(key.getURL(), name, text);
        }
    };

    private FutureCache<ResourceKey, GroovyTemplate, ResourceResolver> futureCache;

    /** . */
    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    public TemplateService(TemplateStatisticService statisticService, CacheService cservice) throws Exception {
        this.engine_ = new GroovyTemplateEngine();
        this.statisticService = statisticService;
        this.templatesCache_ = cservice.getCacheInstance(TemplateService.class.getSimpleName());
        this.futureCache = new FutureExoCache<ResourceKey, GroovyTemplate, ResourceResolver>(loader, templatesCache_);
    }

    public void merge(String name, BindingContext context) throws Exception {
        long startTime = System.currentTimeMillis();

        GroovyTemplate template = getTemplate(name, context.getResourceResolver());
        context.put("_ctx", context);
        context.setGroovyTemplateService(this);
        template.render(context.getWriter(), context, (Locale) context.get("locale"));
        long endTime = System.currentTimeMillis();

        TemplateStatistic templateStatistic = statisticService.getTemplateStatistic(name);
        templateStatistic.setTime(endTime - startTime);
        templateStatistic.setResolver(context.getResourceResolver());
    }

    @Deprecated
    public void merge(Template template, BindingContext context) throws Exception {
        context.put("_ctx", context);
        context.setGroovyTemplateService(this);
        Writable writable = template.make(context);
        writable.writeTo(context.getWriter());
    }

    public void include(String name, BindingContext context) throws Exception {
        if (context == null)
            throw new Exception("Binding cannot be null");
        context.put("_ctx", context);
        GroovyTemplate template = getTemplate(name, context.getResourceResolver());
        template.render(context.getWriter(), context, (Locale) context.get("locale"));
    }

    public final GroovyTemplate getTemplate(String name, ResourceResolver resolver) throws Exception {
        return getTemplate(name, resolver, cacheTemplate_);
    }

    public final GroovyTemplate getTemplate(String url, ResourceResolver resolver, boolean cacheable) throws Exception {
        GroovyTemplate template;
        ResourceKey resourceId = resolver.createResourceKey(url);
        if (cacheable) {
            template = futureCache.get(resolver, resourceId);
        } else {
            template = loader.retrieve(resolver, resourceId);
        }

        //
        return template;
    }

    public final void invalidateTemplate(String name, ResourceResolver resolver) {
        ResourceKey resourceKey = resolver.createResourceKey(name);
        getTemplatesCache().remove(resourceKey);
    }

    public ExoCache<ResourceKey, GroovyTemplate> getTemplatesCache() {
        return templatesCache_;
    }

    /*
     * Clear the templates cache
     */
    @Managed
    @ManagedDescription("Clear the template cache")
    public void reloadTemplates() {
        try {
            templatesCache_.clearCache();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /*
     * Clear the template cache by name
     */
    @Managed
    @ManagedDescription("Clear the template cache for a specified template identifier")
    @Impact(ImpactType.IDEMPOTENT_WRITE)
    public void reloadTemplate(@ManagedDescription("The template id") @ManagedName("templateId") String name) {
        TemplateStatistic app = statisticService.findTemplateStatistic(name);
        if (app != null) {
            ResourceResolver resolver = app.getResolver();
            templatesCache_.remove(resolver.createResourceKey(name));
        }
    }

    @Managed
    @ManagedDescription("List the identifiers of the cached templates")
    @Impact(ImpactType.READ)
    public String[] listCachedTemplates() {
        try {
            ArrayList<String> list = new ArrayList<String>();
            for (GroovyTemplate template : templatesCache_.getCachedObjects()) {
                list.add(template.getId());
            }
            return list.toArray(new String[list.size()]);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
