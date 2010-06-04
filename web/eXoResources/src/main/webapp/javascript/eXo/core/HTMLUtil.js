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
 * @author Nguyen Ba Uoc
 */
// 4test
if (eXo.require) eXo.require('eXo.core.html.HTMLEntities');

function HTMLUtil() {
  this.entities = eXo.core.html.HTMLEntities ;
}

/**
 * Encode string
 * @param {String} str string to encode 
 * @return {String} encoded string
 * 
 */
HTMLUtil.prototype.entitiesEncode = function(str) {
  if (!str || str == '') {
    return str ;
  }
  for(var n in this.entities) {
    var entityChar = String.fromCharCode(this.entities[n]) ;
    if(entityChar == '&') {
      entityChar = '\\' + entityChar ;
    }
    while(str.indexOf(entityChar) != -1) {
      str = str.replace(entityChar, '&' + n + ';') ;
    }
  }
  return str ;
}

/**
 * Decode string
 * @param {String} str to decode
 * @return {String} decoded string
 * 
 */
HTMLUtil.prototype.entitiesDecode = function(str) {
  if (!str || str == '') {
    return str ;
  }
  for(var n in this.entities) {
    var entityChar = String.fromCharCode(this.entities[n]) ;
    var htmlEntity = '&' + n + ';' ;
    while(str.indexOf(htmlEntity) != -1) {
      str = str.replace(htmlEntity, entityChar) ;
    }
  }
  return str ;
}

eXo.core.HTMLUtil = new HTMLUtil() ;