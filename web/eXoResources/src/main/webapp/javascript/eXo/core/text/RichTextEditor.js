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

function RichTextEditor() {
}

RichTextEditor.prototype = new eXo.core.DefaultKeyboardListener() ;

RichTextEditor.prototype.onAlphabet = RichTextEditor.prototype.onDefault ;
RichTextEditor.prototype.onDigit = RichTextEditor.prototype.onDefault ;

RichTextEditor.prototype.onNavGetBegin = function(keynum, keychar) {
  var node = this.currentNode ;
  var previousEditableNode = false ;
  while((node = node.previousSibling) &&
        node.className != 'UIWindow') {
    if (eXo.core.CoreEditor.isEditableNode(node)) {
      previousEditableNode = node ;
      break ;
    }
  }
  if (previousEditableNode) {
    eXo.core.CoreEditor.init(previousEditableNode) ;
    this.beforeCursor = this.getEditContent() ;
    this.afterCursor = '' ;
    this.defaultWrite() ;
  }
} ;

RichTextEditor.prototype.onNavGetEnd = function(keynum, keychar) {
  var node = this.currentNode ;
  var nextEditableNode = false ;
  while((node = node.nextSibling)) {
    if (eXo.core.CoreEditor.isEditableNode(node)) {
      nextEditableNode = node ;
      break ;
    }
  }
  if (nextEditableNode) {
    eXo.core.CoreEditor.init(nextEditableNode) ;
    this.beforeCursor = '' ; 
    this.afterCursor = this.getEditContent() ;
    this.defaultWrite() ;
  }
} ;

if (!eXo.core.text) eXo.core.text = {} ;
eXo.core.text.RichTextEditor = new RichTextEditor() ;