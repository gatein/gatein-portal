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

package org.exoplatform.portal.resource;

import org.exoplatform.commons.utils.BinaryOutput;
import org.exoplatform.commons.utils.ByteArrayOutput;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.Orientation;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.Startable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

@Managed
@NameTemplate({@Property(key = "view", value = "portal"), @Property(key = "service", value = "management"),
   @Property(key = "type", value = "skin")})
@ManagedDescription("Skin service")
@RESTEndpoint(path = "skinservice")
public class SkinService implements Startable
{

   protected static Log log = ExoLogger.getLogger("portal.SkinService");

   private static final Map<Orientation, String> suffixMap = new EnumMap<Orientation, String>(Orientation.class);

   static
   {
      suffixMap.put(Orientation.LT, "-lt.css");
      suffixMap.put(Orientation.RT, "-rt.css");
      suffixMap.put(Orientation.TL, "-lt.css");
      suffixMap.put(Orientation.TR, "-lt.css");
   }

   private static final String LEFT_P = "\\(";

   private static final String RIGHT_P = "\\)";

   /** Immutable and therefore thread safe. */
   private static final Pattern IMPORT_PATTERN =
      Pattern.compile("(@import\\s+" + "url" + LEFT_P + "['\"]?" + ")([^'\"]+.css)(" + "['\"]?" + RIGHT_P + "\\s*;)");

   /** Immutable and therefore thread safe. */
   private static final Pattern BACKGROUND_PATTERN =
      Pattern.compile("(background.*:.*url" + LEFT_P + "['\"]?" + ")([^'\"]+)(" + "['\"]?" + RIGHT_P + ".*;)");

   /** Immutable and therefore thread safe. */
   private static final Pattern LT = Pattern.compile("/\\*\\s*orientation=lt\\s*\\*/");

   /** Immutable and therefore thread safe. */
   private static final Pattern RT = Pattern.compile("/\\*\\s*orientation=rt\\s*\\*/");

   /** One month caching. */
   private static final int ONE_MONTH = 2592000;

   /** One hour caching. */
   private static final int ONE_HOUR = 3600;

   /** The deployer. */
   private final AbstractResourceHandler deployer;

   /** The removal. */
   private final AbstractResourceHandler removal;

   private final Map<SkinKey, SkinConfig> portalSkins_;

   private final Map<SkinKey, SkinConfig> skinConfigs_;

   private final HashSet<String> availableSkins_;

   private final Map<String, CachedStylesheet> ltCache;

   private final Map<String, CachedStylesheet> rtCache;

   private final Map<String, Set<String>> portletThemes_;

   private final MainResourceResolver mainResolver;

   /**
    * The name of the portal container
    */
   final String portalContainerName;

   /**
    * An id used for caching request. The id life cycle is the same than the
    * class instance because we consider css will change until server is
    * restarted. Of course this only applies for the developing mode set to
    * false.
    */
   final String id = Long.toString(System.currentTimeMillis());

   public SkinService(ExoContainerContext context)
   {
      portalSkins_ = new LinkedHashMap<SkinKey, SkinConfig>();
      skinConfigs_ = new LinkedHashMap<SkinKey, SkinConfig>(20);
      availableSkins_ = new HashSet<String>(5);
      ltCache = new ConcurrentHashMap<String, CachedStylesheet>();
      rtCache = new ConcurrentHashMap<String, CachedStylesheet>();
      portletThemes_ = new HashMap<String, Set<String>>();
      portalContainerName = context.getPortalContainerName();
      mainResolver = new MainResourceResolver(portalContainerName, skinConfigs_);
      deployer = new GateInSkinConfigDeployer(portalContainerName, this);
      removal = new GateInSkinConfigRemoval(portalContainerName, this);
   }

   public void addCategoryTheme(String categoryName)
   {
      if (!portletThemes_.containsKey(categoryName))
         portletThemes_.put(categoryName, new HashSet<String>());
   }

   public void addPortalSkin(String module, String skinName, String cssPath, ServletContext scontext)
   {
      addPortalSkin(module, skinName, cssPath, scontext, false);
   }

   /**
    * Register the stylesheet for a portal Skin.
    * 
    * @param module
    *           skin module identifier
    * @param skinName
    *           skin name
    * @param cssPath
    *           path uri to the css file. This is relative to the root context,
    *           use leading '/'
    * @param scontext
    *           the webapp's {@link javax.servlet.ServletContext}
    * @param overwrite
    *           if any previous skin should be replaced by that one
    */
   public void addPortalSkin(String module, String skinName, String cssPath, ServletContext scontext, boolean overwrite)
   {
      availableSkins_.add(skinName);
      SkinKey key = new SkinKey(module, skinName);
      SkinConfig skinConfig = portalSkins_.get(key);
      if (skinConfig == null || overwrite)
      {
         skinConfig = new SimpleSkin(this, module, skinName, cssPath);
         portalSkins_.put(key, skinConfig);
         
         if (log.isDebugEnabled())
         {
            log.debug("Adding Portal skin : Bind " + key + " to " + skinConfig);
         }
      }
   }

   public void addPortalSkin(String module, String skinName, String cssPath, String cssData)
   {
      SkinKey key = new SkinKey(module, skinName);
      SkinConfig skinConfig = portalSkins_.get(key);
      if (skinConfig == null)
      {
         portalSkins_.put(key, new SimpleSkin(this, module, skinName, cssPath));

         if (log.isDebugEnabled())
         {
            log.debug("Adding Portal skin : Bind " + key + " to " + skinConfig);
         }
      }
      ltCache.put(cssPath, new CachedStylesheet(cssData));
      rtCache.put(cssPath, new CachedStylesheet(cssData));
   }

   public void addSkin(String module, String skinName, String cssPath, ServletContext scontext)
   {
      addSkin(module, skinName, cssPath, scontext, false);
   }

   /**
    * Merge several skins into one single skin.
    * 
    * @param skins
    *           the skins to merge
    * @return the merged skin
    */
   public Skin merge(Collection<SkinConfig> skins)
   {
      return new CompositeSkin(this, skins);
   }

   /**
    * Add a resource resolver to plug external resolvers.
    * 
    * @param resolver
    *           a resolver to add
    */
   public void addResourceResolver(ResourceResolver resolver)
   {
      mainResolver.resolvers.addIfAbsent(resolver);
   }

   public void addSkin(String module, String skinName, String cssPath, ServletContext scontext, boolean overwrite)
   {
      availableSkins_.add(skinName);
      SkinKey key = new SkinKey(module, skinName);
      SkinConfig skinConfig = skinConfigs_.get(key);
      if (skinConfig == null || overwrite)
      {
         skinConfig = new SimpleSkin(this, module, skinName, cssPath);
         skinConfigs_.put(key, skinConfig);
      }
   }

   public void addSkin(String module, String skinName, String cssPath, String cssData)
   {
      availableSkins_.add(skinName);
      SkinKey key = new SkinKey(module, skinName);
      SkinConfig skinConfig = skinConfigs_.get(key);
      if (skinConfig == null)
      {
         skinConfigs_.put(key, new SimpleSkin(this, module, skinName, cssPath));
      }
      ltCache.put(cssPath, new CachedStylesheet(cssData));
      rtCache.put(cssPath, new CachedStylesheet(cssData));
   }

   public void addTheme(String categoryName, List<String> themesName)
   {
      if (!portletThemes_.containsKey(categoryName))
         portletThemes_.put(categoryName, new HashSet<String>());
      Set<String> catThemes = portletThemes_.get(categoryName);
      for (String theme : themesName)
         catThemes.add(theme);
   }

   /**
    * Get names of all the currently registered skins.
    * 
    * @return an unmodifiable Set of the currently registered skins
    */
   public Set<String> getAvailableSkinNames()
   {
      return availableSkins_;
   }

   /**
    * Return the CSS content of the file specified by the given URI.
    * 
    * @param cssPath
    *           path of the css to find
    * @return the css
    */
   public String getCSS(String cssPath)
   {
      try
      {
         final ByteArrayOutput output = new ByteArrayOutput();
         renderCSS(new ResourceRenderer()
         {
            public BinaryOutput getOutput() throws IOException
            {
               return output;
            }
            public void setExpiration(long seconds)
            {
            }
         }, cssPath);
         return output.toString();
      }
      catch (IOException e)
      {
         log.error("Error while rendering css " + cssPath, e);
         return null;
      }
      catch (RenderingException e)
      {
         log.error("Error while rendering css " + cssPath, e);
         return null;
      }
   }

   public void renderCSS(ResourceRenderer renderer, String path) throws RenderingException, IOException
   {
      Orientation orientation = Orientation.LT;
      if (path.endsWith("-lt.css"))
      {
         path = path.substring(0, path.length() - "-lt.css".length()) + ".css";
      }
      else if (path.endsWith("-rt.css"))
      {
         path = path.substring(0, path.length() - "-rt.css".length()) + ".css";
         orientation = Orientation.RT;
      }

      // Try cache first
      if (!PropertyManager.isDevelopping())
      {

         if (path.startsWith("/" + portalContainerName + "/resource"))
         {
            renderer.setExpiration(ONE_MONTH);
         }
         else
         {
            renderer.setExpiration(ONE_HOUR);
         }

         //
         Map<String, CachedStylesheet> cache = orientation == Orientation.LT ? ltCache : rtCache;
         CachedStylesheet css = cache.get(path);
         if (css == null)
         {
            StringBuilder sb = new StringBuilder();
            processCSS(sb, path, orientation, true);
            css = new CachedStylesheet(sb.toString());
            cache.put(path, css);
         }
         css.writeTo(renderer.getOutput());
      }
      else
      {
         StringBuffer sb = new StringBuffer();
         processCSS(sb, path, orientation, false);
         byte[] bytes = sb.toString().getBytes("UTF-8");
         renderer.getOutput().write(bytes);
      }
   }

   public String getMergedCSS(String cssPath)
   {
      CachedStylesheet stylesheet = ltCache.get(cssPath);
      return stylesheet != null ? stylesheet.getText() : null;
   }

   public Collection<SkinConfig> getPortalSkins(String skinName)
   {
      Set<SkinKey> keys = portalSkins_.keySet();
      Collection<SkinConfig> portalSkins = new ArrayList<SkinConfig>();
      for (SkinKey key : keys)
      {
         if (key.getName().equals(skinName))
            portalSkins.add(portalSkins_.get(key));
      }
      return portalSkins;
   }

   public Map<String, Set<String>> getPortletThemes()
   {
      return portletThemes_;
   }

   public SkinConfig getSkin(String module, String skinName)
   {
      SkinConfig config = skinConfigs_.get(new SkinKey(module, skinName));
      if (config == null)
         skinConfigs_.get(new SkinKey(module, "Default"));
      return config;
   }

   public void invalidatePortalSkinCache(String portalName, String skinName)
   {
      SkinKey key = new SkinKey(portalName, skinName);
      skinConfigs_.remove(key);
   }

   public void remove(String module, String skinName) throws Exception
   {
      SkinKey key;
      if (skinName.length() == 0)
         key = new SkinKey(module, "Default");
      else
         key = new SkinKey(module, skinName);
      skinConfigs_.remove(key);
   }

   public void removeSupportedSkin(String skinName) throws Exception
   {
      availableSkins_.remove(skinName);
   }

   public void remove(List<SkinKey> keys) throws Exception
   {
      if (keys == null)
      {
         return;
      }
      for (SkinKey key : keys)
      {
         skinConfigs_.remove(key);
      }
   }

   public int size()
   {
      return skinConfigs_.size();
   }
   
   /**
    *
    * This method delegates the resource resolving to MainResourceResolver and prints out appropriated log messages 
    * 
    * Consider the two cases the method is invoked 
    * 
    * Case 1: Resolve nested .css file 
    * 
    *  In Stylesheet.css we have the statement 
    * 
    *  @import url(xyzt.css);
    * 
    *  To resolve the resource from xyzt.css, getCSSResource("xyzt.css", "Stylesheet.css") is called
    *  
    * Case 2: Resolve top root .css file
    * 
    *  To resolve a top root Stylesheet.css file, getCSSResource("Stylesheet.css", "Stylesheet.css") is called
    * 
    * @param cssPath
    * @param outerCssFile
    * @return
    * 
    */
   private Resource getCSSResource(String cssPath, String outerCssFile)
   {
      Resource resource = mainResolver.resolve(cssPath);
      if (resource == null)
      {
         String logMessage;
         if (!cssPath.equals(outerCssFile))
         {
            int lastIndexOfSlash = cssPath.lastIndexOf('/');
            String loadedCssFile = (lastIndexOfSlash >= 0)?(cssPath.substring(lastIndexOfSlash + 1)) : cssPath;
            logMessage =
               "Invalid <CSS FILE> configuration, please check the @import url(" + loadedCssFile + ") in "
                  + outerCssFile + " , SkinService could not load the skin " + cssPath;
         }
         else
         {
            logMessage =
               "Not found <CSS FILE>, the path " + cssPath + " is invalid, SkinService could not load the skin "
                  + cssPath;
         }
         log.error(logMessage);
      }
      return resource;
   }

   private void processCSS(Appendable appendable, String cssPath, Orientation orientation, boolean merge)
      throws RenderingException, IOException
   {
      Resource skin = getCSSResource(cssPath, cssPath);
      processCSSRecursively(appendable, merge, skin, orientation);
   }

   private void processCSSRecursively(Appendable appendable, boolean merge, Resource skin, Orientation orientation)
      throws RenderingException, IOException
   {

      if(skin == null)
      {
         return;
      }
      // The root URL for the entry
      String basePath = skin.getContextPath() + skin.getParentPath();

      //
      String line = "";
      Reader tmp = skin.read();
      if (tmp == null)
      {
         throw new RenderingException("No skin resolved for path " + skin.getResourcePath());
      }
      BufferedReader reader = new BufferedReader(tmp);
      try
      {
         while ((line = reader.readLine()) != null)
         {
            Matcher matcher = IMPORT_PATTERN.matcher(line);
            if (matcher.find())
            {
               String includedPath = matcher.group(2);
               if (includedPath.startsWith("/"))
               {
                  if (merge)
                  {
                     Resource ssskin = getCSSResource(includedPath, basePath + skin.getFileName());
                     processCSSRecursively(appendable, merge, ssskin, orientation);
                  }
                  else
                  {
                     appendable.append(matcher.group(1)).append(
                        includedPath.substring(0, includedPath.length() - ".css".length())).append(
                        getSuffix(orientation)).append(matcher.group(3)).append("\n");
                  }
               }
               else
               {
                  if (merge)
                  {
                     String path = skin.getContextPath() + skin.getParentPath() + includedPath;
                     Resource ssskin = getCSSResource(path, basePath + skin.getFileName());
                     processCSSRecursively(appendable, merge, ssskin, orientation);
                  }
                  else
                  {
                     appendable.append(matcher.group(1));
                     appendable.append(basePath);
                     appendable.append(includedPath.substring(0, includedPath.length() - ".css".length()));
                     appendable.append(getSuffix(orientation));
                     appendable.append(matcher.group(3));
                  }
               }
            }
            else
            {
               if (orientation == null || wantInclude(line, orientation))
               {
                  append(line, basePath, appendable);
               }
            }
         }
      }
      finally
      {
         Safe.close(reader);
      }
   }

   /**
    * Filter what if it's annotated with the alternative orientation.
    * 
    * @param line
    *           the line to include
    * @param orientation
    *           the orientation
    * @return true if the line is included
    */
   private boolean wantInclude(String line, Orientation orientation)
   {
      Pattern orientationPattern = orientation == Orientation.LT ? RT : LT;
      Matcher matcher2 = orientationPattern.matcher(line);
      return !matcher2.find();
   }

   private void append(String line, String basePath, Appendable appendable) throws IOException
   {
      // Rewrite background url pattern
      Matcher matcher = BACKGROUND_PATTERN.matcher(line);
      if (matcher.find() && !matcher.group(2).startsWith("\"/") && !matcher.group(2).startsWith("'/")
         && !matcher.group(2).startsWith("/"))
      {
         appendable.append(matcher.group(1)).append(basePath).append(matcher.group(2)).append(matcher.group(3)).append(
            '\n');
      }
      else
      {
         appendable.append(line).append('\n');
      }
   }

   String getSuffix(Orientation orientation)
   {
      if (orientation == null)
      {
         orientation = Orientation.LT;
      }
      return suffixMap.get(orientation);
   }

   @Managed
   @ManagedDescription("The list of registered skins identifiers")
   public String[] getSkinList()
   {
      // get all available skin
      List<String> availableSkin = new ArrayList<String>();
      for (String skin : availableSkins_)
      {
         availableSkin.add(skin);
      }
      // sort skin name asc
      Collections.sort(availableSkin);

      return availableSkin.toArray(new String[availableSkin.size()]);
   }

   public void registerContext(ServletContext sContext)
   {
      mainResolver.registerContext(sContext);
   }

   @Managed
   @ManagedDescription("Reload all skins")
   @Impact(ImpactType.WRITE)
   public void reloadSkins()
   {
      // remove all ltCache, rtCache
      ltCache.clear();
      rtCache.clear();
   }

   @Managed
   @ManagedDescription("Reload a specified skin")
   public void reloadSkin(@ManagedDescription("The skin id") @ManagedName("skinId") String skinId)
   {
      ltCache.remove(skinId);
      rtCache.remove(skinId);
   }

   public void start()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(deployer);
      DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(removal);
   }

   public void stop()
   {
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(deployer);
      DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(removal);
   }
}