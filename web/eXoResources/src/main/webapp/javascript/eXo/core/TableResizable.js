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

function TableResizable() {

} ;
TableResizable.prototype.init = function(evt, markerobj) {
	_e = (window.event) ? window.event : evt ;
	this.posX = _e.clientX ;
	var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
	this.beforeCol = eXo.core.DOMUtil.findAncestorByTagName(marker, "th") ;
	this.afterCol = eXo.core.DOMUtil.findNextElementByTagName(this.beforeCol, "th") ;
	var beforePaddingLeft = parseInt(eXo.core.DOMUtil.getStyle(this.beforeCol, "paddingLeft")) ;
	var afterPaddingLeft = parseInt(eXo.core.DOMUtil.getStyle(this.afterCol, "paddingLeft")) ;
	this.beforeCol.style.width = (this.beforeCol.offsetWidth - beforePaddingLeft - marker.offsetWidth) + "px" ;
	this.afterCol.style.width = (this.afterCol.offsetWidth - afterPaddingLeft - marker.offsetWidth) + "px" ;
	this.beforeColX = this.beforeCol.offsetWidth - beforePaddingLeft - marker.offsetWidth;
	this.afterColX = this.afterCol.offsetWidth - afterPaddingLeft - marker.offsetWidth;
	document.onmousemove = eXo.core.TableResizable.adjustWidth ;	
	document.onmouseup = eXo.core.TableResizable.clear ;
} ;

TableResizable.prototype.adjustWidth = function(evt) {
	_e = (window.event) ? window.event : evt ;
	var TableResizable = eXo.core.TableResizable ;
	var delta = _e.clientX - TableResizable.posX;
	var beforeWidth = TableResizable.beforeColX + delta ;
	var afterWidth = TableResizable.afterColX - delta ;
	if (beforeWidth <= 0  || afterWidth <= 0) return ;
	TableResizable.beforeCol.style.width = beforeWidth + "px" ;
	TableResizable.afterCol.style.width = afterWidth + "px" ;
} ;

TableResizable.prototype.clear = function() {
	document.onmousemove = null ;
} ;

eXo.core.TableResizable = new TableResizable() ;