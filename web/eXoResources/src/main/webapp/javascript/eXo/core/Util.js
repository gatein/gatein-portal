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

/**
 * Make url portal request with parameters
 * 
 * @param targetComponentId identifier of component
 * @param actionName name of action
 * @param useAjax indicate Ajax request or none
 * @param params array contains others parameters
 * @return full url request
 */

/**
 * log out of user session
 */
eXo.portal.logout = function() {
	window.location = eXo.env.server.createPortalURL("UIPortal", "Logout", false) ;
} ;

eXo.session.openUrl = null ;
eXo.session.itvTime = null ;
eXo.session.itvObj = null;
eXo.session.initialized = false;

eXo.session.itvInit = function() {
   var session = eXo.session, env = eXo.env;
   if (!session.initialized && session.canKeepState && env.portal.accessMode == 'private') {
      if (!session.openUrl) session.openUrl = env.server.createPortalURL("UIPortal", "Ping", false) ;
      if (!session.itvTime) session.itvTime = 1800;
      session.initialized = true;
      session.openItv();
   }
} ;

eXo.session.startItv = function() {
   var session = eXo.session;
   if (session.initialized) {
      session.destroyItv();
      if (session.canKeepState && eXo.env.portal.accessMode == 'private') {
         if (session.itvTime > 0) session.itvObj = window.setTimeout("eXo.session.openItv()", (session.itvTime - 10) * 1000) ;
      }
   } else if (session.isOpen) {
      session.itvInit();
   }
} ;

eXo.session.openItv = function() {
	var session = eXo.session;
	var request = window.ActiveXObject ? new ActiveXObject( "Msxml2.XMLHTTP" ) : new XMLHttpRequest();
	request.open("GET", session.openUrl, true);
	request.setRequestHeader("Cache-Control", "max-age=86400");
	request.onreadystatechange = function() {
		if (request.readyState == 4) { 
			if (request.status == 200) {
				var result = request.responseText;
				if(!isNaN(result)) session.itvTime = parseInt(result); 				
			}
			delete request['onreadystatechange'];
		}
	}
	request.send(null);
} ;

eXo.session.destroyItv = function () {
   var session = eXo.session;
   window.clearTimeout(session.itvObj) ;
   session.itvObj = null ;
} ;

/**
 * Generates an id based on the current time and random number
 */
eXo.generateId = function(objectId) {
	return (objectId + "-" + new Date().getTime() + Math.random().toString().substring(2)) ;
};

eXo.debug = function(message) {
	if(!eXo.developing) return;
	
	var webui = eXo.webui;
	if(webui.UINotification) {
		message = "DEBUG: " + message;
		webui.UINotification.addMessage(message);
	}
};