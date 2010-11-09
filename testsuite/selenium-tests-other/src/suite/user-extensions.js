/***************************************************************************************************************/
/** Exo User Extensions ***/
/***************************************************************************************************************/

Selenium.prototype.doGetExoExtensionVersion = function(){
	return "1.0";
};

/**
  * This function allows to use a specific Contextual menu
  * Usage:
  * - Locator : Element to rightclick on
  *
  * For more information see the following URL:
  * - Manually Fire event : http://www.howtocreate.co.uk/tutorials/javascript/domevents#domevld1
  * - initMouseEvent properties : http://www.quirksmode.org/js/events_properties.html
  *
  * store in exo-int/qa/selenium
  **/ 
Selenium.prototype.doComponentExoContextMenu = function(locator){
	
	var element = this.page().findElement(locator);
    if (element.fireEvent && element.ownerDocument && element.ownerDocument.createEventObject) { // IE
        var evt = createEventObject(element, false, false, false, false);
        evt.button = 2;
        element.fireEvent('onmousedown', evt);
    } else {
    	var evObj = document.createEvent('MouseEvents');
		evObj.initMouseEvent( 'mousedown', true, true, window, 1, 12, 345, 7, 220, false, false, false, false, 2, null );
		element.dispatchEvent(evObj);
    }

};

/**
  * This function allows to use a specific Contextual menu
  * Usage:
  * - Locator : Element to doubleclick on
	  
  * For more information see the following URL:
  * - Manually Fire event : http://www.howtocreate.co.uk/tutorials/javascript/domevents#domevld1
  * - initMouseEvent properties : http://www.quirksmode.org/js/events_properties.html
  * 
  * store in exo-int/qa/selenium
  **/ 
Selenium.prototype.doComponentExoDoubleClick = function(locator){
	
	var element = this.page().findElement(locator);
    if (element.fireEvent && element.ownerDocument && element.ownerDocument.createEventObject) { // IE
        var evt = createEventObject(element, false, false, false, false);
        evt.button = 0;
        element.fireEvent('ondblclick', evt);
    } else {
    	var evObj = document.createEvent('MouseEvents');
		evObj.initMouseEvent( 'dblclick', true, true, window, 1, 12, 345, 7, 220, false, false, false, false, 0, null );
		element.dispatchEvent(evObj);
    }

};