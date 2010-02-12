/***************************************************************************************************************/
/** Exo User Extensions ***/
/***************************************************************************************************************/

Selenium.prototype.doGetExoExtensionVersion = function(){
	return "1.0";
};

/**
  * This function allows to enter text in a FCK field
  * Usage:
  *  - locator  the locator that points to the parent iframe (the one that contains both the toolbar and the text area)
  *  - text     the text to enter into the field
  * Run selenium rc server from maven with the userExtension parameter. 
  * 
  * For more information see the following URL:
  * - http://mojo.codehaus.org/selenium-maven-plugin/start-server-mojo.html#userExtensions
  *
  * store in exo-int/qa/selenium
  **/ 
Selenium.prototype.doTypeFCKEditor = function(locator, text) {
	    // All locator-strategies are automatically handled by "findElement"
	    var editor = this.page().findElement(locator);
	    
	    // TODO: use contentWindow instead of contentDocument for IE
	    var innerEditor = null;
	    if (editor.contentDocument)
	    	innerEditor = editor.contentDocument.getElementsByTagName("iframe")[0];
	    else if (editor.contentWindow)
	    	innerEditor = editor.contentWindow.document.getElementsByTagName("iframe")[0];
	    
	    if (innerEditor)
	      innerEditor.contentDocument.body.innerHTML = text;
	    // Replace the element text with the new text
	    // this.page().replaceText(element, valueToType);
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
	
	var fireOnThis = this.page().findElement(locator);
    var evObj = document.createEvent('MouseEvents');
    evObj.initMouseEvent( 'mousedown', true, true, window, 1, 12, 345, 7, 220, false, false, false, false, 2, null );
    fireOnThis.dispatchEvent(evObj);

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
	
	var fireOnThis = this.page().findElement(locator);
    var evObj = document.createEvent('MouseEvents');
    evObj.initMouseEvent( 'dblclick', true, true, window, 1, 12, 345, 7, 220, false, false, false, false,0, null );
    fireOnThis.dispatchEvent(evObj);

};