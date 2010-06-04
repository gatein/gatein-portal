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

function MouseEventManager () {} ;
/**
 * Add mouse down event handler
 * @param method handler method
 */
MouseEventManager.prototype.addMouseDownHandler = function(method) {
	document.onmousedown = this.docMouseDownEvt ;
	this.onMouseDownHandlers = method ;
} ;
/**
 * Document mouse down event, it will cancel default behavior of browser and process behavior in handler chain
 * @param {Event} evt
 */
MouseEventManager.prototype.docMouseDownEvt = function(evt) {
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;

	if(eXo.core.MouseEventManager.onMouseDownHandlers == null) return;
	if(typeof(eXo.core.MouseEventManager.onMouseDownHandlers) == "string") eval(eXo.core.MouseEventManager.onMouseDownHandlers) ;
	else eXo.core.MouseEventManager.onMouseDownHandlers(evt) ;
	document.onmousedown = null ;
} ;
/**
 * Add Mouse up event handler
 * @param method handler method
 */
MouseEventManager.prototype.addMouseUpHandler = function(method) {
	document.onmouseup = this.docMouseUpEvt ;
	this.onMouseUpHandlers = method ;
} ;
/**
 * Document mouse up event, it will cancel default behavior of browser and process behavior in handler chain
 * @param {Event} evt
 */
MouseEventManager.prototype.docMouseUpEvt = function() {
	var mouseUpHandlers = eXo.core.MouseEventManager.onMouseUpHandlers ;
	
} ;
/**
 * Document mouse click event, it will cancel default behavior of browser and process behavior in handler chain
 * @param {Event} evt
 */
MouseEventManager.prototype.docMouseClickEvt = function(evt) {
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;
	
	if(typeof(eXo.core.MouseEventManager.onMouseClickHandlers) == "string") eval(eXo.core.MouseEventManager.onMouseClickHandlers) ;
	else eXo.core.MouseEventManager.onMouseClickHandlers(evt) ;
	document.onclick = null ;
} ;
/**
 * Add mouse click handler
 * @param method handler method
 */
MouseEventManager.prototype.addMouseClickHandler = function(method) {
	document.onclick = this.docMouseClickEvt ;
	this.onMouseClickHandlers = method ;
} ;

eXo.core.MouseEventManager = new MouseEventManager() ;