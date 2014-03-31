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
  require(["SHARED/portalRequest"], function() {
	  doRequest("Get", url, null, callback);
  });
};

/**
 * Do a POST request in AJAX with given <code>url</code> and <code>queryString</code>.
 * The call is delegated to the doRequest() method with a callback function
 */
window.ajaxPost = function(url, queryString, callback) {
  if (!callback) callback = null ;
  require(["SHARED/portalRequest"], function() {
	  doRequest("POST", url, queryString, callback) ;
  });
};

eXo.env.server.createPortalURL = function(targetComponentId, actionName, useAjax, params) {
  var url = eXo.env.server.portalURLTemplate.replace("_portal:componentId_", targetComponentId);
  url = url.replace("_portal:action_", actionName);

  var ampPlain = "&";
  var ampXhtml = "&amp;";
  var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;

  if(params != null) {
  	var len = params.length ;
    for(var i = 0 ; i < len ; i++) {
      var paramName = encodeURIComponent(params[i].name);
      var paramValue = encodeURIComponent(params[i].value);
      url += amp +  paramName + "=" + paramValue ;
    }
  }
  if(useAjax) url += amp + "ajaxRequest=true" ;

  return url;
};

eXo.env.addLoadedRemoteScripts = function(scripts) {
	if (typeof define === 'function' && define.amd) {
		for (var i = 0; i < scripts.length; i++) {
			define(scripts[i]);
		}
	}
};

/**
 * This method is internal used for GateIn to simulate requirejs
 * 2 difference method signs
 * require(depName)
 * require(array, callback)
 */
eXo.require = function() {
	if (arguments.length == 1) {
		//eXo.define.names and eXo.define.deps are defined in GateIn JS wrapper
		var ctxDepNames = eXo.define.names;
		var ctxDeps = eXo.define.deps;

		var idx = eXo.inArray(ctxDepNames, arguments[0]);
		if (idx !== -1) {
			return ctxDeps[idx];
		} else {
			return window.require(arguments[0]);
		}
	} else {
		return eXo.define.apply(this, arguments);
	}
};

eXo.require.config = require.config;
eXo.require.undef = require.undef;
eXo.require.toUrl = require.toUrl;

/**
 * This method is internal used for GateIn to simulate requirejs
 * 3 difference method signs
 * define(name, array, callback)
 * define(array, callback)
 * define(callback)
 */
eXo.define = function() {
	var reqList = [], callback = null;

	if (arguments.length == 1) {
		callback = arguments[0];
		if (callback instanceof Function) {
			reqList = ["require", "exports", "module"];
		}
	} else {
		for (var i = 0; i < arguments.length; i++) {
			var arg = arguments[i];
			if (arg instanceof Array) {
				reqList = arg;
			} else if (arg instanceof Function) {
				callback = arg;
			}
		}
	}

	//eXo.define.names and eXo.define.deps are defined in GateIn JS wrapper
	var ctxDepNames = eXo.define.names;
	var ctxDeps = eXo.define.deps;

	var deps = [];
	for (var i = 0; i < reqList.length; i++) {
		var idx = eXo.inArray(ctxDepNames, reqList[i]);
		if (idx !== -1) {
			deps[i] = ctxDeps[idx];
		} else {
			deps[i] = null;
		}
	}

	var result;
	if (callback instanceof Function) {
		var result = callback.apply(this, deps);
		if (!result) {
			var idx = eXo.inArray(reqList, "module");
			if (idx !== -1) {
				result = deps[idx].exports;
			} else if ((idx = eXo.inArray(reqList, "exports")) != -1) {
				result = deps[idx];
			}
		}
	} else {
		result = callback;
	}
	return result;
};

//IE doesn't support Array#indexOf
eXo.inArray = function(arr, itm) {
	if (!arr) return -1;
	for (var i = 0; i < arr.length; i++) {
		if (arr[i] === itm) {
			return i;
		}
	}
	return -1;
};