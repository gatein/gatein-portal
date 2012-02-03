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

//var count = 1 ;
function DragDrop2() {
	var obj = null;
	
	DragDrop2.prototype.init = function(o, oRoot) {
		o.onmousedown = eXo.core.DragDrop2.start;

		o.root = oRoot && oRoot != null ? oRoot : o ;
		
		o.root.onDragStart = new Function();
		o.root.onDragEnd = new Function();
		o.root.onDrag = new Function();
	};
	
	DragDrop2.prototype.start = function(e)	{
		if (!e) e = window.event;
		if(((e.which) && (e.which == 2 || e.which == 3)) || ((e.button) && (e.button == 2)))	{
			return;
		}
		var o = obj = this;
		e = eXo.core.DragDrop2.fixE(e);
		var y = parseInt(eXo.core.DOMUtil.getStyle(o.root,"top"));
		var x = parseInt(eXo.core.DOMUtil.getStyle(o.root,"left"));
		if(isNaN(x)) x=0;		if(isNaN(y)) y=0;
		o.lastMouseX = 		eXo.core.Browser.findMouseXInPage(e);
		o.lastMouseY = 		eXo.core.Browser.findMouseYInPage(e);
		o.root.onDragStart(x, y, o.lastMouseX, o.lastMouseY, e);
		document.onmousemove = eXo.core.DragDrop2.drag;
		document.onmouseup = eXo.core.DragDrop2.end;
		document.onmouseout = eXo.core.DragDrop2.cancel;
		return false;
	};
	
	DragDrop2.prototype.drag = function(e) {
		e = eXo.core.DragDrop2.fixE(e);
		var o = obj, browser = eXo.core.Browser;
		var ey = browser.findMouseYInPage(e);
		var ex = browser.findMouseXInPage(e);
		var y = parseInt(eXo.core.DOMUtil.getStyle(o.root, "top"));
		var x = parseInt(eXo.core.DOMUtil.getStyle(o.root, "left"));
		if(isNaN(x)) x=0;		if(isNaN(y)) y=0;
		var nx, ny;
		nx = x + (ex - o.lastMouseX);
		ny = y + (ey - o.lastMouseY);
		obj.root.style["left"] = nx + "px";
		obj.root.style["top"] = ny + "px";
		obj.lastMouseX = ex;
		obj.lastMouseY = ey;

		obj.root.onDrag(nx, ny, ex, ey, e);
		return false;
	};
	
	DragDrop2.prototype.end = function(e) {
		e = eXo.core.DragDrop2.fixE(e);
		document.onmousemove = null;
		document.onmouseup = null;
		document.onmouseout = null;
		obj.root.onDragEnd( parseInt(obj.root.style["left"]), 
		parseInt(obj.root.style["top"]), e.clientX, e.clientY);
		obj = null;
	};
	
	DragDrop2.prototype.cancel = function(e) {
		if(obj.root.onCancel) obj.root.onCancel(e);
	};
	
	DragDrop2.prototype.fixE = function(e) {
		if (typeof e == 'undefined') e = window.event;
		if (typeof e.layerX == 'undefined') e.layerX = e.offsetX;
		if (typeof e.layerY == 'undefined') e.layerY = e.offsetY;
		return e;
	};
};

eXo.core.DragDrop2 = new DragDrop2();