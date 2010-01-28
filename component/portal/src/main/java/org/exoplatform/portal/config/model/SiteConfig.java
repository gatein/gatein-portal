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
   
   private String ownerType;
   
   private String ownerId;
   
   private Container layout;
   
   private PageNavigation navigation;
   
   private String[] accessPermissions;
   
   private String editPermission;
   
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
   
}
