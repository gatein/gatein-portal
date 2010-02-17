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

import groovy.lang.Writable;
import groovy.text.Template;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.groovyscript.GroovyTemplate;
import org.exoplatform.groovyscript.GroovyTemplateEngine;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.management.annotations.RESTEndpoint;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by The eXo Platform SAS Dec 26, 2005
 */
@Managed
@NameTemplate({@Property(key = "view", value = "portal"), @Property(key = "service", value = "management"),
   @Property(key = "type", value = "template")})
@ManagedDescription("Template management service")
@RESTEndpoint(path = "templateservice")
public class TemplateService
{

   private GroovyTemplateEngine engine_;

   private ExoCache<String, GroovyTemplate> templatesCache_;

   private TemplateStatisticService statisticService;

   private boolean cacheTemplate_ = true;

   public TemplateService(InitParams params, TemplateStatisticService statisticService, CacheService cservice)
      throws Exception
   {
      engine_ = new GroovyTemplateEngine();
      this.statisticService = statisticService;
      templatesCache_ = cservice.getCacheInstance(TemplateService.class.getName());
      getTemplatesCache().setLiveTime(10000);
   }

   public void merge(String name, BindingContext context) throws Exception
   {
      long startTime = System.currentTimeMillis();

      GroovyTemplate template = getTemplate(name, context.getResourceResolver());
      context.put("_ctx", context);
      context.setGroovyTemplateService(this);
      template.render(context.getWriter(), context);

      long endTime = System.currentTimeMillis();

      TemplateStatistic templateStatistic = statisticService.getTemplateStatistic(name);
      templateStatistic.setTime(endTime - startTime);
      templateStatistic.setResolver(context.getResourceResolver());
   }

   @Deprecated
   public void merge(Template template, BindingContext context) throws Exception
   {
      context.put("_ctx", context);
      context.setGroovyTemplateService(this);
      Writable writable = template.make(context);
      writable.writeTo(context.getWriter());
   }

   public void include(String name, BindingContext context) throws Exception
   {
      if (context == null)
         throw new Exception("Binding cannot be null");
      context.put("_ctx", context);
      GroovyTemplate template = getTemplate(name, context.getResourceResolver());
      template.render(context.getWriter(), context);
   }

   final public GroovyTemplate getTemplate(String name, ResourceResolver resolver) throws Exception
   {
      return getTemplate(name, resolver, cacheTemplate_);
   }

   final public GroovyTemplate getTemplate(String url, ResourceResolver resolver, boolean cacheable) throws Exception
   {
      GroovyTemplate template = null;
      if (cacheable)
      {
         String resourceId = resolver.createResourceId(url);
         template = getTemplatesCache().get(resourceId);
      }
      if (template != null)
         return template;
      InputStream is;
      byte[] bytes = null;
      is = resolver.getInputStream(url);
      bytes = IOUtil.getStreamContentAsBytes(is);
      is.close();

      // The template class name
      int pos = url.lastIndexOf('/');
      if (pos == -1)
      {
         pos = 0;
      }
      String name = url.substring(pos);

      String text = new String(bytes);
      template = engine_.createTemplate(url, name, text);

      if (cacheable)
      {
         String resourceId = resolver.createResourceId(url);
         getTemplatesCache().put(resourceId, template);
      }

      return template;
   }

   final public void invalidateTemplate(String name, ResourceResolver resolver) throws Exception
   {
      String resourceId = resolver.createResourceId(name);
      getTemplatesCache().remove(resourceId);
   }

   public ExoCache<String, GroovyTemplate> getTemplatesCache()
   {
      return templatesCache_;
   }

   /*
    * Clear the templates cache
    */
   @Managed
   @ManagedDescription("Clear the template cache")
   public void reloadTemplates()
   {
      try
      {
         templatesCache_.clearCache();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /*
    * Clear the template cache by name
    */
   @Managed
   @ManagedDescription("Clear the template cache for a specified template identifier")
   @Impact(ImpactType.IDEMPOTENT_WRITE)
   public void reloadTemplate(@ManagedDescription("The template id") @ManagedName("templateId") String name)
   {
      try
      {
         TemplateStatistic app = statisticService.apps.get(name);
         ResourceResolver resolver = app.getResolver();
         templatesCache_.remove(resolver.createResourceId(name));
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   @Managed
   @ManagedDescription("List the identifiers of the cached templates")
   @Impact(ImpactType.READ)
   public String[] listCachedTemplates()
   {
      try
      {
         ArrayList<String> list = new ArrayList<String>();
         for (GroovyTemplate template : templatesCache_.getCachedObjects())
         {
            list.add(template.getId());
         }
         return list.toArray(new String[list.size()]);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
}
