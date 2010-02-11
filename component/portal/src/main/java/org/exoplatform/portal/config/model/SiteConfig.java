package org.exoplatform.portal.config.model;

import org.exoplatform.portal.pom.data.ModelData;

/**
 * 
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class SiteConfig extends ModelObject
{
   
   final public static String USER_TYPE = "user";
   
   final public static String GROUP_TYPE = "group";
   
   final public static String PORTAL_TYPE = "portal";
   
   private String ownerType;
   
   private String ownerId;
   
   private PageNavigation navigation;
   
   /** Access permissions on UI */
   private String[] accessPermissions;
   
   /** Edit permissions on UI */
   private String editPermission;
   
   /** Layout of the site */
   private Container siteLayout;
   
   private String siteSkin;
   
   public SiteConfig(String _ownerType, String _ownerId, String storageId)
   {
      super(storageId);
      this.ownerType = _ownerType;
      this.ownerId = _ownerId;
   }
   
   @Override
   public ModelData build()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public void setSiteLayout(Container _siteLayout)
   {
      this.siteLayout = _siteLayout;
   }
   
   public Container getSiteLayout()
   {
      return this.siteLayout;
   }
   
   public String getSiteSkin()
   {
      return this.siteSkin;
   }
   
   public void setSiteSkin(String _siteSkin)
   {
      this.siteSkin = _siteSkin;
   }
}
