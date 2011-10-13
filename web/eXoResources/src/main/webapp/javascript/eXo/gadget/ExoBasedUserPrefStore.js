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

gadgets.ExoBasedUserPrefStore = function() {
  gadgets.UserPrefStore.call(this);
};

gadgets.ExoBasedUserPrefStore.inherits(gadgets.UserPrefStore);

gadgets.ExoBasedUserPrefStore.prototype.getPrefs = function(gadget) {
  return gadget.userPrefs_;
};

gadgets.ExoBasedUserPrefStore.prototype.savePrefs = function(gadget, newPrefs) {
  	//TODO: dang.tung - sent event to portal
  var prefs = eXo.core.JSON.stringify(newPrefs || gadget.userPrefs_);
  prefs = encodeURIComponent(prefs);
  var DOMUtil = eXo.core.DOMUtil;
	var gadget = document.getElementById("gadget_" + gadget.id) ;
	if(gadget != null ) {
		var uicomponent = gadget.parentNode.id.replace(/^content-/,"");
		var portletFragment = DOMUtil.findAncestorByClass(gadget, "PORTLET-FRAGMENT");
		var gadgetPortlet = DOMUtil.findAncestorByClass(gadget, "UIGadgetPortlet");
		if(gadgetPortlet != null) {
			uicomponent = gadgetPortlet.id;
		}
		if (portletFragment != null) {
			var compId = portletFragment.parentNode.id;
			var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
			href += "&portal:type=action&uicomponent=" + uicomponent;
			href += "&op=SaveUserPref";
			href += "&ajaxRequest=true";
			href += "&userPref=" + prefs;
			ajaxGet(href,true);
		} else {
			var params = [
			 {name : "userPref", value : prefs}
			] ;
			ajaxGet(eXo.env.server.createPortalURL(uicomponent, "SaveUserPref", true, params),true) ;
		}
	}
};

gadgets.Container.prototype.userPrefStore = new gadgets.ExoBasedUserPrefStore();