package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Page;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * An immutable page state class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageState implements Serializable
{

   /** . */
   final String editPermission;

   /** . */
   final boolean showMaxWindow;

   /** . */
   final String factoryId;

   /** . */
   final String name;

   /** . */
   final String description;

   /** . */
   final List<String> accessPermissions;

   public PageState(Page page)
   {

      Attributes attrs = page.getAttributes();
      Described described = page.adapt(Described.class);

      //
      List<String> accessPermissions = Collections.emptyList();
      String editPermission = null;
      if (page.isAdapted(ProtectedResource.class))
      {
         ProtectedResource pr = page.adapt(ProtectedResource.class);
         accessPermissions = pr.getAccessPermissions();
         editPermission = pr.getEditPermission();
      }

      //
      this.factoryId = attrs.getValue(MappedAttributes.FACTORY_ID);
      this.name = described.getName();
      this.description = described.getDescription();
      this.accessPermissions = Utils.safeImmutableList(accessPermissions);
      this.editPermission = editPermission;
      this.showMaxWindow = attrs.getValue(MappedAttributes.SHOW_MAX_WINDOW, false);
   }

   public PageState(
      String name,
      String description,
      boolean showMaxWindow,
      String factoryId,
      List<String> accessPermissions,
      String editPermission)
   {
      this.editPermission = editPermission;
      this.showMaxWindow = showMaxWindow;
      this.factoryId = factoryId;
      this.name = name;
      this.description = description;
      this.accessPermissions = accessPermissions;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public boolean getShowMaxWindow()
   {
      return showMaxWindow;
   }

   public String getFactoryId()
   {
      return factoryId;
   }

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   public List<String> getAccessPermissions()
   {
      return accessPermissions;
   }
}
