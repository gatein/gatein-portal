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

function CacheJSonService() {	
	this.cacheData = new eXo.core.HashMap() ;
} ;

if(eXo.core.CacheJSonService == undefined){
  eXo.core.CacheJSonService = new CacheJSonService() ;
} ;

CacheJSonService.prototype.getData = function(url, invalidCache) {
  if(invalidCache){
  	this.cacheData.remove(url) ;	
  } else {
	  var value = this.cacheData.get(url) ;
		if(value != null && value != undefined)	return value ;	
  }
	var responseText = ajaxAsyncGetRequest(url, false) ;
	
	if(responseText == null || responseText == '') return null ;
	
//	alert("Response Text1: " + responseText);
	
  var response ;
  try {
  	if(request.responseText != '') {
  	  eval("response = "+responseText) ;
  	}
  } catch(err) {
    /**Created: Comment by Le Bien Thuy**/
  	//TODO. alert(err + " : "+responseText);
    return  null ;  
  }
  if(response == null || response == undefined) return null ;
  this.cacheData.put(url, response) ;
  return response ;
} ;