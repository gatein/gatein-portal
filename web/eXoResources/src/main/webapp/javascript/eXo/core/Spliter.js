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

eXo.core.Spliter = {	
	exeRowSplit = function(e , markerobj) {
		_e = (window.event) ? window.event : e ;
		this.posY = _e.clientY; 
		var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
		this.beforeArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div") ;
		this.afterArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "div") ;	
		this.beforeArea.style.height = this.beforeArea.offsetHeight + "px" ;
		this.afterArea.style.height = this.afterArea.offsetHeight + "px" ;	
		this.beforeY = this.beforeArea.offsetHeight ;
		this.afterY = this.afterArea.offsetHeight ;
		document.onmousemove = eXo.core.Spliter.adjustHeight ;	
		document.onmouseup = eXo.core.Spliter.clear ;
	},
	
	adjustHeight = function(evt) {
		evt = (window.event) ? window.event : evt ;
		var Spliter = eXo.core.Spliter ;
		var delta = evt.clientY - Spliter.posY ;
		var afterHeight = (Spliter.afterY - delta) ;
		var beforeHeight = (Spliter.beforeY + delta) ;
		if (beforeHeight <= 0  || afterHeight <= 0) return ;
		Spliter.beforeArea.style.height =  beforeHeight + "px" ;
		Spliter.afterArea.style.height =  afterHeight + "px" ;	
	},
	
	clear = function() {
		document.onmousemove = null ;
	}
} ;