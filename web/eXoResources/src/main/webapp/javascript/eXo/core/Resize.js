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

function Resize() {} ;

Resize.prototype.init = function(o, oToResizeWidth, oToResizeHeight) {
	o.onmousedown = Resize.start ;
	o.oToResizeWidth = oToResizeWidth || o ;
	o.oToResizeHeight = oToResizeHeight || o.oToResizeWidth ;
	o.oToResizeWidth.style.width = o.oToResizeWidth.offsetWidth + "px" ;
	o.oToResizeHeight.style.height = o.oToResizeHeight.offsetHeight + "px" ;
} ;
	
Resize.prototype.start = function(e)	{
	Resize.obj = new Object();
	var o = Resize.obj.elemt = this;
	e = app.fixE(e);
	Resize.obj.initMouseX = browser.findMouseXInPage(e);
	Resize.obj.initMouseY = browser.findMouseYInPage(e);
	Resize.obj.initWidth = parseInt(o.oToResizeWidth.style.width) ;
	Resize.obj.initHeight = parseInt(o.oToResizeHeight.style.height);
//	window.status = Resize.obj.initWidth + " : " + Resize.obj.initHeight;
	document.onmousemove = Resize.drag;
	document.onmouseup = Resize.end;
	return false;
} ;
	
Resize.prototype.drag = function(e) {
	e = app.fixE(e);
	var o = Resize.obj.elemt;
	var nx = Resize.obj.initWidth + (browser.findMouseXInPage(e) - Resize.obj.initMouseX) ;
	var ny = Resize.obj.initHeight + (browser.findMouseYInPage(e) - Resize.obj.initMouseY) ;
	nx = Math.max(100, nx) ;
	ny = Math.max(100, ny) ;
	o.oToResizeHeight.style.height = ny + 'px';
	o.oToResizeWidth.style.width = nx + 'px' ;
	return false;
} ;
	
Resize.prototype.end = function(e) {
	e = app.fixE(e);
	document.onmousemove = null;
	document.onmouseup = null ;
	delete Resize.obj;
} ;

eXo.core.Resize = new Resize() ;