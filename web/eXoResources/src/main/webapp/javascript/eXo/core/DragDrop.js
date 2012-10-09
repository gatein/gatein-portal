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

(function($) {	
	function DragDrop() {
		var obj = null;
		
		DragDrop.prototype.init = function(o, oRoot) {
			var jObj = $(o);
			jObj.off("mousedown");
			jObj.on("mousedown", eXo.core.DragDrop.start);
	
			o.root = oRoot && oRoot != null ? oRoot : o ;
			
			o.root.onDragStart = new Function();
			o.root.onDragEnd = new Function();
			o.root.onDrag = new Function();
		};
		
		DragDrop.prototype.start = function(e)	{
			var o = obj = this;
			var jRoot = $(o.root);
			
			if((e.which && e.which != 1) || jRoot.data("dragging"))	{
				return false;
			}
			var position = jRoot.position();
			o.lastMouseX = e.pageX;
			o.lastMouseY = e.pageY;
			o.root.onDragStart(position.left, position.top, o.lastMouseX, o.lastMouseY, e);
			$(document).on({"mousemove" : eXo.core.DragDrop.drag,
				"mouseup" : eXo.core.DragDrop.end,
				"keydown" : eXo.core.DragDrop.onKeyDownEvt,
				"mouseout" : eXo.core.DragDrop.cancel});
			jRoot.data("dragging", true);
			return false;
		};
		
		DragDrop.prototype.drag = function(e) {
			var o = obj;
			var ey = e.pageY;
			var ex = e.pageX;
			
			var jRoot = $(o.root);
			var y = parseInt(jRoot.css("top"));
			var x = parseInt(jRoot.css("left"));
	
			var nx, ny;
			nx = x + (ex - o.lastMouseX);
			ny = y + (ey - o.lastMouseY);
			obj.root.style["right"] = "";
			obj.root.style["left"] = nx + "px";
			obj.root.style["top"] = ny + "px";
			obj.lastMouseX = ex;
			obj.lastMouseY = ey;
	
			obj.root.onDrag(nx, ny, ex, ey, e);
			return false;
		};
		
		DragDrop.prototype.end = function(e) {
			$(document).off("mousemove mouseup mouseout keydown");
			
			var jRoot = $(obj.root);
			var position = jRoot.position();
			var y = position.top;
			var x = position.left;
			
			obj.root.onDragEnd( position.left, position.top, e.clientX, e.clientY, e);
			obj = null;
			jRoot.removeData("dragging");
			return false;
		};
		
		DragDrop.prototype.cancel = function(e) {
			if(obj.root.onCancel) obj.root.onCancel(e);
			return false;
		};
		
		DragDrop.prototype.onKeyDownEvt = function(e) {
			if(e.which === 27) eXo.core.DragDrop.end(e) ;
			return false;
		}
	};
	
	eXo.core.DragDrop = new DragDrop();
	return {DragDrop : eXo.core.DragDrop};
})($);