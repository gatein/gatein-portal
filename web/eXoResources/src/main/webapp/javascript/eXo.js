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
};

/*****************************************************************************************/
/*
* This is the main entry method for every Ajax calls to the eXo Portal
*
* It is simply a dispatcher method that fills some init fields before 
* calling the doRequest() method
*/
window.ajaxGet = function(url, callback) {
  if (!callback) callback = null ;
  require(["SHARED/base"], function() {
	  doRequest("Get", url, null, callback);	  
  });
};

/**
 * Do a POST request in AJAX with given <code>url</code> and <code>queryString</code>.
 * The call is delegated to the doRequest() method with a callback function
 */
window.ajaxPost = function(url, queryString, callback) {
  if (!callback) callback = null ;
  require(["SHARED/base"], function() {
	  doRequest("POST", url, queryString, callback) ;
  });
};

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

  return  url;
};

eXo.env.addLoadedRemoteScripts = function(scripts) {
	if (typeof define === 'function' && define.amd) {
		for (var i = 0; i < scripts.length; i++) {
			define(scripts[i]);
		}		
	}
};