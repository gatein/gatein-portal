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
 * This class adds a scroll functionnality to elements when there is not enough space to show them all
 * Use : create a manager with the function newScrollManager
 *     : create a load and an init function in your js file
 *     : the load function sets all the base attributes, the init function recalculates the visible elements
 *       (e.g. when the window is resized)
 *     : create a callback function if necessary, to add specific behavior to your scroll
 *       (e.g. if an element must be always visible)
 */
function ScrollManager(id) {
	if (typeof (id) == "string") id = document.getElementById(id);
	this.mainContainer = id; // The HTML DOM element that contains the tabs, the arrows, etc	
	this.elements = new Array(); // the array containing the elements
	this.firstVisibleIndex = 0; // the index in the array of the first visible element
	this.lastVisibleIndex = -1; // the index in the array of the last visible element
	this.axis = 0; // horizontal scroll : 0 , vertical scroll : 1
	this.currDirection = null; // the direction of the current scroll; left or up scroll : 0, right or down scroll : 1
	this.callback = null; // callback function when a scroll is done
	this.leftArrow = null; // the left arrow dom node
	this.rightArrow = null; // the right arrow dom node
	this.arrowsContainer = null // The HTML DOM element that contains the arrows
	var scroll = this;
	this.refresh = setTimeout(function() {scroll.checkResize()}, 700);
};

/**
 * Initializes the scroll manager, with some default parameters
 */
ScrollManager.prototype.init = function() {
	this.maxSpace = 0;
	this.firstVisibleIndex = 0;
	this.lastVisibleIndex = -1;
	
	if(!this.arrowsContainer)  {
		// Adds the tab elements to the manager
		var arrowsContainer = $(this.mainContainer).find(".ScrollButtons");
		if (arrowsContainer.length) {
			this.arrowsContainer = arrowsContainer[0];			
			// Configures the arrow buttons
			var arrowButtons = arrowsContainer.find("a");
			if (arrowButtons.length == 2) {
				this.initArrowButton(arrowButtons[0], "left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton");
				this.initArrowButton(arrowButtons[1], "right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton");
			}					
		}
	}
	
	// Hides the arrows by default
	if(this.arrowsContainer)  {
		this.arrowsContainer.style.display =  "none";
		this.arrowsContainer.space = null;
	}			
};

/**
 * Loads the tabs in the scroll manager, depending on their css class
 * If clean is true, calls cleanElements to remove the space property of each element
 */
ScrollManager.prototype.loadElements = function(elementClass, clean) {
	if (clean) this.cleanElements();
	this.elements = $(this.mainContainer).find("." + elementClass);	
};

/**
 * Initializes the arrows with :
 *  . mouse listeners
 *  . css class and other parameters
 */
ScrollManager.prototype.initArrowButton = function(arrow, dir, normalClass, overClass, disabledClass) {
	if (arrow) {
		arrow = $(arrow);
		arrow[0].direction = dir; // "left" or "right" (up or down)
		arrow[0].overClass = overClass; // the css class for mouse over event
		arrow[0].disabledClass = disabledClass; // the css class for a disabled arrow
		arrow[0].styleClass = normalClass; // the css class for an enabled arrow, in the normal state
		arrow[0].scrollMgr = this; // an easy access to the scroll manager
		arrow.on("mouseover", this.mouseOverArrow);
		arrow.on("mouseout", this.mouseOutArrow);
		arrow.on("click", this.scroll);
		if (dir == "left") this.leftArrow = arrow[0];
		else if (dir == "right") this.rightArrow = arrow[0];
	}
};

/**
 * Disables or enables the arrow
 */
ScrollManager.prototype.enableArrow = function(arrow, enabled) {
	if (arrow && !enabled) { // disables the arrow
		arrow.className = arrow.disabledClass;
	} else if (arrow && enabled) { // enables the arrow
		arrow.className = arrow.styleClass;
	}
};
/**
 * Sets the mouse over css style of the arrow (this)
 * only if it is enabled
 */
ScrollManager.prototype.mouseOverArrow = function(e) {
	var arrow = this;
	if (arrow.className == arrow.styleClass) {
		arrow.className = arrow.overClass;
	}
};
/**
 * Sets the mouse out css style of the arrow (this)
 * only if it is enabled
 */
ScrollManager.prototype.mouseOutArrow = function(e) {
	var arrow = this;
	if (arrow.className == arrow.overClass) {
		arrow.className = arrow.styleClass;
	}
};

/**
 * Calculates the available space for the elements, and inits the elements array like this :
 *  . maxSpace = space of mainContainer - space of arrowsContainer - a margin
 *  . browses the elements and add their space to elementsSpace, for each element compares elementsSpace with maxSpace
 *  . if elementsSpace le maxSpace : the current element is set visible, and its index becomes the lastVisibleIndex
 *  . if elementsSpace gt maxSpace : the current element is set hidden (isVisible = false)
 * At the end, each visible element has an isVisible property set to true, the other elements are set to false,
 * the firstVisibleIndex is 0, the lastVisibleIndex is the last element with isVisible to true
 */
ScrollManager.prototype.checkAvailableSpace = function() { // in pixels
	if (!this.maxSpace) {
		this.maxSpace = $(this.mainContainer).width() - this.getElementSpace(this.arrowsContainer);
	}
	var elementsSpace = 0, margin = 0;
	var length =  this.elements.length;
	if (!this.currDirection) {
		for (var i = this.firstVisibleIndex; i < length; i++) {
			elementsSpace += this.getElementSpace(this.elements[i]);
			if (elementsSpace  < this.maxSpace) {
				this.elements[i].isVisible = true;
				this.lastVisibleIndex = i;
			} else {
				this.elements[i].isVisible = false;
			}
		}
	} else {
		for (var i = this.lastVisibleIndex; i >= 0; i--) {
			elementsSpace += this.getElementSpace(this.elements[i]);
			if (elementsSpace  < this.maxSpace) {
				this.elements[i].isVisible = true;
				this.firstVisibleIndex = i;
			} else {
				this.elements[i].isVisible = false;
			}
		}
	}
};

/**
 * Calculates the space of the element passed in parameter
 * The calcul uses : (horizontal tabs | vertical tabs)
 *  . offsetWidth | offsetHeight
 *  . marginLeft and marginRight | marginTop and marginBottom
 *  . the space of the decorator associated with this element, if any
 * If the element is not rendered (display none), renders it, makes the calcul, and hides it again
 * The value of the space is stored in a property space of the element. In the function is called on
 * the same element again, this value is returned directly to avoid another calcul
 * To remove this value, use the cleanElements function, or set space to null manually
 */
ScrollManager.prototype.getElementSpace = function(element) {
	if (element && element.space) { return element.space; }
	var elementSpace = 0;
	if (element) {
		if (this.axis == 0) { // horizontal tabs
			elementSpace += $(element).outerWidth(true);
			// decorator is another element that is linked to the current element (e.g. a separator bar)
			if (element.decorator) elementSpace += this.getElementSpace(element.decorator);
		} else if (this.axis == 1) { // vertical tabs
			elementSpace += $(element).outerHeigth(true);
			if (element.decorator) elementSpace += this.getElementSpace(element.decorator);
		}
		// Store the calculated value for faster return on next calls. To recalculate, set element.space to null.
		element.space = elementSpace;
	}
	return elementSpace;
};

/**
 * Clean the elements of the array : set the space property to null
 */
ScrollManager.prototype.cleanElements = function() {
	for (var i = 0; i < this.elements.length; i++) {
		this.elements[i].space = null;
		if (this.elements[i].decorator) this.elements[i].decorator.space = null;
	}
};

/**
 * Function called when an arrow is clicked. Shows an additionnal element and calls the 
 * appropriate scroll function (left or right). Works like this :
 *  . shows the otherHiddenElements again
 *  . moves the firstVisibleIndex or lastVisibleIndex to the new index
 *  . clear the otherHiddenElements array
 *  . calls the appropriate scroll function (left or right)
 */
ScrollManager.prototype.scroll = function(e) {
	var src = this;
	if (src.className !== src.disableClass) {
		if (src.direction == "left") src.scrollMgr.scrollLeft();
		else if (src.direction == "right") src.scrollMgr.scrollRight();		
	}
	return false;
};

ScrollManager.prototype.scrollLeft = function() { // Same for scrollUp
	if (this.firstVisibleIndex > 0) {
		this.currDirection = 0;		
		this.firstVisibleIndex--;		
		this.renderElements();
	}
};

ScrollManager.prototype.scrollUp = function() {
	if (this.scrollMgr) this.scrollMgr.scrollLeft();
};
/**
 * Scrolls right (or down) :
 *  . sets the current first visible element hidden
 *  . increments firstVisibleIndex
 *  . increments lastVisibleIndex
 *  . set the new last visible element to visible
 * Simulates a move to the right of the tabs
 */
ScrollManager.prototype.scrollRight = function() { // Same for scrollDown
	if (this.lastVisibleIndex < this.elements.length-1) {
		this.currDirection = 1;		
		this.lastVisibleIndex++;				
		this.renderElements();
	}
};

ScrollManager.prototype.scrollDown = function() {
	if (this.scrollMgr) this.scrollMgr.scrollRight();
};

ScrollManager.prototype.renderElements = function() {
	this.checkAvailableSpace();
	
	for (var i = 0; i < this.elements.length; i++) {
		if (this.elements[i].isVisible) { // if the element should be rendered...
			this.elements[i].style.display = "block";
		} else { // if the element must not be rendered...
			this.elements[i].style.display = "none";
			this.arrowsContainer.style.display = "block";
		}
	}
	if (this.arrowsContainer.style.display == "block") {
		this.renderArrows();
	}
	
	if (typeof(this.callback) == "function") this.callback();
};

/**
 * Renders the arrows. If we reach the end of the tabs, this end arrow is disabled
 */
ScrollManager.prototype.renderArrows = function() {
	// Enables/Disables the arrow buttons depending on the elements to show
	if (this.firstVisibleIndex == 0) this.enableArrow(this.leftArrow, false);
	else this.enableArrow(this.leftArrow, true);
	
	if (this.lastVisibleIndex == this.elements.length-1) this.enableArrow(this.rightArrow, false);
	else this.enableArrow(this.rightArrow, true);
};

/**
 * Calculates the space of the elements between indexStart and indexEnd
 * If these parameters are null, calculates the space for all the elements of the array
 * Uses the getElementSpace function
 */
ScrollManager.prototype.getElementsSpace = function(indexStart, indexEnd) {
	if (indexStart == null && indexEnd == null) {
		indexStart = 0;
		indexEnd = this.elements.length-1 ;
	}
	var elementsSpace = 0;
	if (indexStart >= 0 && indexEnd <= this.elements.length-1) {
		for (var i = indexStart; i <= indexEnd; i++) {
			elementsSpace += this.getElementSpace(this.elements[i]);
		}
	}
	return elementsSpace;
};

ScrollManager.prototype.checkResize = function() {
	if (this.mainContainer) {
		var tmp = $("#" + this.mainContainer.id);
		if (!tmp.length) {
			clearTimeout(this.refresh);
			return;
		}
		this.mainContainer = tmp[0];
		this.mainContainer.space = null; 
		this.arrowsContainer.space = null;
		var curr = $(this.mainContainer).width() - this.getElementSpace(this.arrowsContainer);		
		if (this.maxSpace && this.maxSpace !== curr) {
			var mgrParent = tmp.closest(".UIWindow");
			// if the tabs exist on the page
			// in desktop mode, checks that the UIWindow containing the tabs is
			// visible (display block) 
			if (mgrParent.length == 0 || mgrParent.css("display") == "block") {				
				this.init();
				this.renderElements();
			}
		}		
	}
	var scroll = this;
	this.refresh = setTimeout(function() {scroll.checkResize()}, 700);
};