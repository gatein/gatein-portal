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
  core : { },

  env : { portal: {}, client: {}, server: {} },

  portal : { },
    
  webui : { },

  gadget : { },

  session : { },
  
  i18n : { }
} ;

/**
* This function is deprecated, please use eXo.loadJS instead
* 
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
    if(eval(module + ' != null')) {
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
  eXo.loadJS(path, callback, context, params);
} ;

eXo.loadJS = function(paths, callback, context, params) {
  if (!paths || !paths.length) return;  
  var tmp = [], loader = eXo.core.Loader;
  
  paths = typeof paths === 'string' ? [paths] : paths;  
  for (var i = 0; i < paths.length; i++) {
	  if (!loader.loadedScripts[paths[i]]) {
		  loader.register(paths[i], paths[i]);
		  tmp.push(paths[i]);
	  }	  
  }
  if (tmp.length > 0) {
	  loader.init(tmp, callback, context, params);	  
  }
  
  eXo.session.startItv();
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
  var url = eXo.env.server.portalURLTemplate.replace("{portal:componentId}", targetComponentId);
  url = url.replace("{portal:action}", actionName);
  
  if(params != null) {
  	var len = params.length ;
    for(var i = 0 ; i < len ; i++) {
      var paramName = encodeURIComponent(params[i].name);
      var paramValue = encodeURIComponent(params[i].value);
      url += "&" +  paramName + "=" + paramValue ;
    }
  }
  if(useAjax) url += "&ajaxRequest=true" ;

  return  url ;
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
	var result = ajaxAsyncGetRequest(session.openUrl, false) ;
	if(!isNaN(result)) session.itvTime = parseInt(result) ;
} ;

eXo.session.destroyItv = function () {
   var session = eXo.session;
   window.clearTimeout(session.itvObj) ;
   session.itvObj = null ;
} ;

eXo.debug = function(message) {
	if(!eXo.developing) return;
	
	var webui = eXo.webui;
	if(webui.UINotification) {
		message = "DEBUG: " + message;
		webui.UINotification.addMessage(message);
	}
}
