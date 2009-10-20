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

function RTEManager() {
  this.HTMLUtil = eXo.core.HTMLUtil ;
  this.coreEditor = eXo.core.CoreEditor ;
}

RTEManager.prototype.init = function() {
  this.preSplitNodes = [] ;
  if(window.getSelection) { // Netscape/Firefox/Opera
    var selObj = window.getSelection() ;
    var anchorTextNode = selObj.anchorNode ;
    var anchorOffset = selObj.anchorOffset ;
    var focusTextNode = selObj.focusNode ;
    var focusOffset = selObj.focusOffset ;
    if (focusTextNode === anchorTextNode && 
        (this.coreEditor.isEditableNode(focusTextNode) || 
        this.coreEditor.isEditableNode(focusTextNode.parentNode))) {
      var nodeValue = anchorTextNode.nodeValue ;
      nodeValue = this.HTMLUtil.entitiesDecode(nodeValue) ;
      this.preSplitNodes[0] = this.createEditableNode(nodeValue.substr(0, anchorOffset)) ;
      this.preSplitNodes[1] = this.createEditableNode(nodeValue.substr(anchorOffset, focusOffset)) ;
      this.preSplitNodes[2] = this.createEditableNode(nodeValue.substr(focusOffset, nodeValue.length - 1)) ;
    } else {
      if (this.coreEditor.isEditableNode(anchorTextNode) ||
          this.coreEditor.isEditableNode(anchorTextNode.parentNode)) {
        var anchorNodeContent = this.HTMLUtil.entitiesDecode(anchorTextNode.nodeValue) ;
        this.preSplitNodes[0] = this.createEditableNode(anchorNodeContent.substr(0, anchorOffset)) ;
        this.preSplitNodes[1] = 
            this.createEditableNode(anchorNodeContent.substr(anchorOffset, anchorNodeContent.length - 1)) ;
      }
      var iNode = anchorTextNode ;
      // TODO: Fix bug: can not nextSibling to get next selected node
      while (iNode && (iNode = iNode.nextSibling)) {
        if (iNode == focusTextNode || iNode == focusTextNode.parentNode) break ;
        if (this.coreEditor.isEditableNode(iNode)) {
          this.preSplitNodes[this.preSplitNodes.length] = iNode ;
        }
      }
      if (this.coreEditor.isEditableNode(focusTextNode) ||
          this.coreEditor.isEditableNode(focusTextNode.parentNode)) {
        var focusNodeContent = this.HTMLUtil.entitiesDecode(focusTextNode.nodeValue) ;
        this.preSplitNodes[this.preSplitNodes.length] = this.createEditableNode(focusNodeContent.substr(0, focusOffset)) ;
        this.preSplitNodes[this.preSplitNodes.length] = 
            this.createEditableNode(focusNodeContent.substr(focusOffset, focusNodeContent.length - 1)) ;
      }
    }
  } else if(document.selection) {
    return true ;
  }
  // Check data
  var strTmp = '' ;
  for (var i=0; i < this.preSplitNodes.length; i++) {
    strTmp += i + '.' + this.preSplitNodes[i].innerHTML + '\n' ;
  }
  window.alert(strTmp) ;
} ;

// Create editable node wrapper for text node.
RTEManager.prototype.createEditableNode = function(nodeContent, tagName) {
  if (!tagName) tagName = 'SPAN' ;
  var editableNode = document.createElement(tagName) ;
  editableNode.setAttribute('editable', '1') ;
  editableNode.innerHTML = nodeContent ;
  return editableNode ;
} ;

eXo.core.text.RTEManager = new RTEManager() ;