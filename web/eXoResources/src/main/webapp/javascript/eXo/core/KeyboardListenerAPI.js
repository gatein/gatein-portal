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
 * @author uoc.nb
 * 
 * A Keyboard's listener API. 
 * 
 */
function KeyboardListenerAPI() {
}

KeyboardListenerAPI.prototype = {
  init : function(node, beforeCursor, afterCursor) {}
  ,
  
  onFinish : function() {}
  ,
  
  removeCursor : function() {}
  ,
  
  defaultWrite : function() {}
  ,
  
  /**
   * 
   * @param {String} beforeCursor
   * @param {String} cursor
   * @param {String} afterCursor
   */
  write: function(beforeCursor, cursor, afterCursor){}
  ,
  
  isSameNode : function(node) {}
  ,
  
  /**
   * @return {String}
   */
  getEditContent : function() {}
  ,
  
  preKeyProcess : function() {}
  ,
  
  // Printable keys
  onDefault : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onAlphabet : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onDigit : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onPunctuation : function(keynum, keychar) { return true ;}
  ,
  
  // Control keys
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onBackspace : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onDelete : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onEnter : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onTab : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onEscapse : function(keynum, keychar) { return true ;}
  ,
  
  // Navigate keys
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onLeftArrow : function(keynum, keychar) { return true ; }
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onRightArrow : function(keynum, keychar) { return true ; }
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onUpArrow : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onDownArrow : function(keynum, keychar) { return true ;}
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onHome : function(keynum, keychar) { return true ; }
  ,
  
  /**
   * 
   * @param {Number} keynum
   * @param {Char} keychar
   */
  onEnd : function(keynum, keychar) { return true ; }  
} ;

eXo.core.KeyboardListenerAPI = KeyboardListenerAPI ;