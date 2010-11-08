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

function DragDropEvent(clickObject, dragObject) {
  this.clickObject = clickObject ;
  if (dragObject && dragObject != null) {
 		this.dragObject = dragObject ;
  } else {
  	this.dragObject = clickObject ;
  }
  this.foundTargetObject = null ;
  this.lastFoundTargetObject = null ;
  this.junkMove = false ;
  
 	if(eXo.core.I18n.isLT() && isNaN(parseInt(this.dragObject.style.left))) this.dragObject.style.left = "0px" ;
 	if(eXo.core.I18n.isRT() && isNaN(parseInt(this.dragObject.style.right))) this.dragObject.style.right = "0px" ;
	if(isNaN(parseInt(this.dragObject.style.top))) this.dragObject.style.top = "0px" ;
} ;

DragDropEvent.prototype.update = function(foundTargetObject, junkMove) {
  this.lastFoundTargetObject = this.foundTargetObject ;
  this.foundTargetObject = foundTargetObject ;
  this.junkMove = junkMove ;
}

DragDropEvent.prototype.isJunkMove = function() {
  return this.junkMove ;
} ;

/*************************************************************************************/

function DragDrop() {
  this.dropableTargets = null ;
  this.dndEvent = null ;

  this.initCallback = null ;
  this.dragCallback = null ;
  this.dropCallback = null ;
  this.destroyCallback = null ;
  this.isJunkMoveCallback = null ;
} ;

DragDrop.prototype.init = function(dropableTargets, clickObject, dragObject, evt) {
	if(evt && evt.preventDefault) evt.preventDefault();
  eXo.core.Mouse.init(evt) ;
  this.dropableTargets = dropableTargets ;
  
  var dndEvent = this.dndEvent = new DragDropEvent(clickObject, dragObject) ;
	document.onmousemove	= this.onMouseMove ;
	document.onmouseup		= this.onDrop ;
	document.onmouseout = this.onCancel ;
	document.onkeypress = this.onKeyPressEvt ;
	
  if(this.initCallback != null) {
    this.initCallback(dndEvent) ;
  }
} ;

DragDrop.prototype.onKeyPressEvt = function(evt) {
	if(!evt) evt = window.event ;
	if(evt.keyCode == 27) eXo.core.DragDrop.onDrop(evt) ;
}

DragDrop.prototype.onMouseMove = function(evt) {
  eXo.core.Mouse.update(evt) ;
	var dndEvent = eXo.core.DragDrop.dndEvent ;
  dndEvent.backupMouseEvent = evt ;
	var dragObject =  dndEvent.dragObject ;

	var y = parseInt(dragObject.style.top) ;
	var x = eXo.core.I18n.isRT() ? parseInt(dragObject.style.right) : parseInt(dragObject.style.left) ;

	if(eXo.core.I18n.isLT()) dragObject.style["left"] =  x + eXo.core.Mouse.deltax + "px" ;
	else dragObject.style["right"] =  x - eXo.core.Mouse.deltax + "px" ;
	dragObject.style["top"]  =  y + eXo.core.Mouse.deltay + "px" ;
	
  if(eXo.core.DragDrop.dragCallback != null) {
    var foundTarget = eXo.core.DragDrop.findDropableTarget(dndEvent, eXo.core.DragDrop.dropableTargets, evt) ;
    var junkMove =  eXo.core.DragDrop.isJunkMove(dragObject, foundTarget) ;
    dndEvent.update(foundTarget, junkMove) ;
    eXo.core.DragDrop.dragCallback(dndEvent) ;
  }
    
	return false ;
} ;

DragDrop.prototype.onDrop = function(evt) {
   if(!evt) evt = window.event ;
   /* should not remove this or move this line to  destroy since the onMouseMove method keep calling */
   if(eXo.core.DragDrop.dropCallback != null) {
      var dndEvent = eXo.core.DragDrop.dndEvent ;
      dndEvent.backupMouseEvent = evt ;
      var dragObject = dndEvent.dragObject ;

      var foundTarget = eXo.core.DragDrop.findDropableTarget(dndEvent, eXo.core.DragDrop.dropableTargets, evt) ;
      var junkMove =  eXo.core.DragDrop.isJunkMove(dragObject, foundTarget) ;

      dndEvent.update(foundTarget, junkMove) ;
      eXo.core.DragDrop.dropCallback (dndEvent) ;
   }
   eXo.core.DragDrop.destroy() ;
} ;

DragDrop.prototype.onCancel = function(evt) {
	if(eXo.core.DragDrop.cancelCallback) eXo.core.DragDrop.cancelCallback(eXo.core.DragDrop.dndEvent);
} ;

DragDrop.prototype.destroy = function() {
  if(this.destroyCallback != null) {
    this.destroyCallback(this.dndEvent) ;
  }

	document.onmousemove	= null ;
  document.onmouseup = null ;
  document.onmouseout = null ;
  document.onkeypress = null ;

  this.dndEvent = null ;
  this.dropableTargets = null ;

  this.initCallback = null ;
  this.dragCallback = null ;
  this.dropCallback = null ;
  this.destroyCallback = null ;
  this.isJunkMoveCallback = null ;
} ;
  
DragDrop.prototype.findDropableTarget = function(dndEvent, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var mousexInPage = eXo.core.Browser.findMouseXInPage(mouseEvent) ;
  var mouseyInPage = eXo.core.Browser.findMouseYInPage(mouseEvent) ;
  
	var clickObject = dndEvent.clickObject ;
	var dragObject = dndEvent.dragObject ;
  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;

    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
      if(foundTarget == null) {
        foundTarget = ele ;
      } else {
        if(eXo.core.DOMUtil.hasAncestor(ele, foundTarget)) {
          foundTarget = ele ;
        }
      } 
    }
  }
 	
  return foundTarget ;
} ;
  
DragDrop.prototype.isIn = function(x, y, component) {
  var componentLeft = eXo.core.Browser.findPosX(component);
  var componentRight = componentLeft + component.offsetWidth ;
  var componentTop = eXo.core.Browser.findPosY(component) ;
  var componentBottom = componentTop + component.offsetHeight ;
  var isOver = false ;

  if((componentLeft < x) && (x < componentRight)) {
    if((componentTop < y) && (y < componentBottom)) {
      isOver = true ;
    }
  }
  return isOver ;
} ;

DragDrop.prototype.isJunkMove = function(src, target) {
  if(this.isJunkMoveCallback != null) {
    return this.isJunkMoveCallback(src, target) ;
  }
  if(target == null) return true ;
  return false ;
} ;
	
eXo.core.DragDrop = new DragDrop() ;