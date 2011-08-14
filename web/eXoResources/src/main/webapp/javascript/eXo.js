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
 * This class contains common js object that used in whole portal
 */
var eXo  = {
  animation : { },
  
  browser : { },
    
  desktop : { },
  
  core : { },

  env : { portal: {}, client: {}, server: {} },

  portal : { },
    
  util : { },
  
  webui : { },

  gadget : { },
  
  application : { 
  	browser : { }
  },
  
  ecm : { },  
  
  calendar : { },
  
  contact : { },
  
  forum : { }, 
   
  mail : { },
  
  faq : { },
  
  session : { },
  
  i18n : { }
} ;

/**
* This method will : 
*   1) dynamically load a javascript module from the server (if no root location is set 
*      then use '/eXoResources/javascript/', aka files
*      located in the eXoResources WAR in the application server). 
*      The method used underneath is a XMLHttpRequest
*   2) Evaluate the returned script
*   3) Cache the script on the client
*
*/
eXo.require = function(module, jsLocation, callback, context, params) {
  try {
    if(eval(module + ' != null'))  {
    	if (callback) {
    		var ctx = context ? context : {};
    	  	if(params && typeof(params) != "string" && params.length) callback.apply(ctx, params);
    	  	else callback.call(ctx, params) ;
    	}
    	return ;
    }
  } catch(err) {
    //alert(err + " : " + module);
  }
  window.status = "Loading Javascript Module " + module ;
  if(jsLocation == null) jsLocation = '/eXoResources/javascript/' ;
  var path = jsLocation  + module.replace(/\./g, '/')  + '.js' ;
  eXo.loadJS(path, module, callback, context, params);
} ;

eXo.loadJS = function(path, module, callback, context, params) {
  if (!module) module = path;
  
  eXo.core.Loader.register(module, path);
  eXo.core.Loader.init(module, callback, context, params);
  
  eXo.session.itvDestroy() ;
  if(eXo.session.canKeepState && eXo.session.isOpen && eXo.env.portal.accessMode == 'private') {
    eXo.session.itvInit() ;
  }
} ;

/**
 * Make url portal request with parameters
 * 
 * @param targetComponentId identifier of component
 * @param actionName name of action
 * @param useAjax indicate Ajax request or none
 * @param params array contains others parameters
 * @return full url request
 */
eXo.env.server.createPortalURL = function(targetComponentId, actionName, useAjax, params) {
  var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + targetComponentId + "&portal:action=" + actionName ;

  if(params != null) {
  	var len = params.length ;
    for(var i = 0 ; i < len ; i++) {
      var paramName = encodeURIComponent(params[i].name);
      var paramValue = encodeURIComponent(params[i].value);
      href += "&" +  paramName + "=" + paramValue ;
    }
  }
  if(useAjax) href += "&ajaxRequest=true" ;
  return  href ;
} ;
/**
 * log out of user session
 */
eXo.portal.logout = function() {
	window.location = eXo.env.server.createPortalURL("UIPortal", "Logout", false) ;
} ;

eXo.session.openUrl = null ;
eXo.session.itvTime = null ;
eXo.session.itvObj = null;

eXo.session.itvInit = function() {
	if(!eXo.session.openUrl) eXo.session.openUrl = eXo.env.server.createPortalURL("UIPortal", "Ping", false) ;
	if(!eXo.session.itvTime) eXo.session.itvTime = 1800;
	if(eXo.session.itvTime > 0) eXo.session.itvObj = window.setTimeout("eXo.session.itvOpen()", (eXo.session.itvTime - 10)*1000) ;
} ;

eXo.session.itvOpen = function() {
	var result = ajaxAsyncGetRequest(eXo.session.openUrl, false) ;
	if(!isNaN(result)) eXo.session.itvTime = parseInt(result) ;
} ;

eXo.session.itvDestroy = function() {
	window.clearTimeout(eXo.session.itvObj) ;
	eXo.session.itvObj = null ;
} ;

eXo.debug = function(message) {
	if(!eXo.developing) return;
	if(eXo.webui.UINotification) {
		message = "DEBUG: " + message;
		eXo.webui.UINotification.addMessage(message);
	}
}