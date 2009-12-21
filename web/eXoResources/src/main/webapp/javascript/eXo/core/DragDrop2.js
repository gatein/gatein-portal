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

var count = 1 ;
eXo.core.DragDrop2 = {
	obj : null,
	
	init : function(o, oRoot) {
		o.onmousedown = eXo.core.DragDrop2.start;

		o.root = oRoot && oRoot != null ? oRoot : o ;
		o.root.onmousedown = function() {
			this.style.zIndex = ++count ;
		}
		
		o.root.onDragStart = new Function();
		o.root.onDragEnd = new Function();
		o.root.onDrag = new Function();
	},
	
	start : function(e)	{
		if (!e) e = window.event;
		if(((e.which) && (e.which == 2 || e.which == 3)) || ((e.button) && (e.button == 2)))	{
			return;
		}
		var o = eXo.core.DragDrop2.obj = this;
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
	},
	
	drag : function(e) {
		e = eXo.core.DragDrop2.fixE(e);
		var o = eXo.core.DragDrop2.obj;
		var ey = eXo.core.Browser.findMouseYInPage(e);
		var ex = eXo.core.Browser.findMouseXInPage(e);
		var y = parseInt(eXo.core.DOMUtil.getStyle(o.root, "top"));
		var x = parseInt(eXo.core.DOMUtil.getStyle(o.root, "left"));
		if(isNaN(x)) x=0;		if(isNaN(y)) y=0;
		var nx, ny;
		nx = x + (ex - o.lastMouseX);
		ny = y + (ey - o.lastMouseY);
		eXo.core.DragDrop2.obj.root.style["left"] = nx + "px";
		eXo.core.DragDrop2.obj.root.style["top"] = ny + "px";
		eXo.core.DragDrop2.obj.lastMouseX = ex;
		eXo.core.DragDrop2.obj.lastMouseY = ey;

		eXo.core.DragDrop2.obj.root.onDrag(nx, ny, ex, ey, e);
		return false;
	},
	
	end : function(e) {
		e = eXo.core.DragDrop2.fixE(e);
		document.onmousemove = null;
		document.onmouseup = null;
		document.onmouseout = null;
		eXo.core.DragDrop2.obj.root.onDragEnd( parseInt(eXo.core.DragDrop2.obj.root.style["left"]), 
		parseInt(eXo.core.DragDrop2.obj.root.style["top"]), e.clientX, e.clientY);
		eXo.core.DragDrop2.obj = null;
	},
	
	cancel : function(e) {
		if(eXo.core.DragDrop2.obj.root.onCancel) eXo.core.DragDrop2.obj.root.onCancel(e);
	},
	
	fixE : function(e) {
		if (typeof e == 'undefined') e = window.event;
		if (typeof e.layerX == 'undefined') e.layerX = e.offsetX;
		if (typeof e.layerY == 'undefined') e.layerY = e.offsetY;
		return e;
	}
	
};