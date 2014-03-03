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

/***********************************************************************************************
 * Portal  Ajax  Response Data Structure
 * {PortalResponse}
 *      |
 *      |--->{PortletResponse}
 *      |
 *      |--->{PortletResponse}
 *      |          |-->{portletId}
 *      |          |
 *      |          |-->{PortletResponseData}
 *      |                 |
 *      |                 |--->{BlockToUpdate}
 *      |                 |         |-->{BlockToUpdateId}
 *      |                 |         |-->{BlockToUpdateData}
 *      |                 |
 *      |                 |--->{BlockToUpdate}
 *      |
 *      |--->{PortalResponseData}
 *      |      |
 *      |      |--->{BlockToUpdate}
 *      |      |         |-->{BlockToUpdateId}
 *      |      |         |-->{BlockToUpdateData}
 *      |      |
 *      |      |--->{BlockToUpdate}
 *      |--->{PortalResponseScript}
 *
 **************************************************************************************************/

/*
* This object is wrapper on the value of each HTML block
* returned by an eXo Portal AJAX call. 
*
* This includes:
*    - the portle ID
*    - the portlet title
*    - the portlet mode
*    - the portlet state
*    - the portlet content
*    - the updated scripts to dynamically load in the browser
*
* Then each block to update within the portlet are place in a object
* which is itself placed inside an array to provide an OO view of the
* AJAX response
*/

(function($, base, uiMaskLayer, msg) {
	function PortletResponse(responseDiv) {
	  var div = $(responseDiv).children("div");
	  this.portletId =  div[0].innerHTML;
	  this.portletData =  div[1].innerHTML;
	  
	  this.blocksToUpdate = null;
	  var blocks = $(div[1]).children(".BlockToUpdate");
	  
	  if(blocks.length > 0 ) {
	    var updates = this.blocksToUpdate = new Array() ;
	    
	    blocks.each(function() {
	    	var div = $(this).children("div");
	    	updates[updates.length] = {blockId : div[0].innerHTML, data : div[1]};
	        updates[updates.length - 1].scripts = $(div[1]).find("script");
	    });
	  } else {
	    /*
	    * If there is no block to update it means we are in a JSR 286 / 168 portlet
	    * In that case we need to find all the script tags and dynamically execute them.
	    *
	    * Indeed, when being in an AJAX call that return some <script> tag, local functions are
	    * lost when calling the eval() methods. When the code is written by eXo we use 
	    * global scoped funstions like "instance.myFunction = function(arguments)".
	    *
	    * But when the code is provided by a third party portlet, it is not possible to
	    * force that good practise. Hence we have to dynamically reference the embedded
	    * script in the head tag
	    */
	    
	    this.scripts = $(div[1]).find("script");
	  }
	};
	
	/*****************************************************************************************/
	/*
	* This object is an OO wrapper on top of the returning HTML included in the PortalResponse
	* tag. 
	*
	* It allows to split in two different arrays the portletResponse blocks and the one and the
	* PortalResponseData one
	*
	* It also extract from the HTML the javascripts script to then be dynamically evaluated
	*/
	function PortalResponse(responseDiv) {
	  //jquery always remove script tag from the source
	  var temp =  document.createElement("div") ;
	  temp.innerHTML = responseDiv;
	  var div = $(temp).children().first(), portalResp = this;
	  
	  portalResp.portletResponses = new Array() ;
	  //Portlet Response
	  div.children("div.PortletResponse").each(function() {
		  portalResp.portletResponses[portalResp.portletResponses.length] = new PortletResponse(this);
	  });
	  //Portal Response
	  div.children("div.PortalResponseData").each(function() {
		  portalResp.data = this;
		  portalResp.blocksToUpdate = new Array();
		  $(this).children(".BlockToUpdate").each(function() {
			var dataBlocks = $(this).children("div");
			var i = portalResp.blocksToUpdate.length;
			portalResp.blocksToUpdate[i] = {blockId : dataBlocks[0].innerHTML, data : dataBlocks[1]}; 
			
			/*
	         * handle embedded javascripts to dynamically add them to the page head
	         *
	         * This is needed when we refresh an entire portal page that contains some 
	         * standard JSR 168 / 286 portlets with embeded <script> tag
	         */
			portalResp.blocksToUpdate[i].scripts = $(dataBlocks[1]).find("script");
		  });
	  });
	  //Extra Markup Header
	  div.children("div.MarkupHeadElements").each(function() {
		  portalResp.markupHeadElements = new MarkupHeadElements(this);
	  });
	  //Loading Scripts
	  div.children("div.LoadingScripts").each(function() {
		  portalResp.loadingScripts = new LoadingScripts(this);
	  });
	  //Portal Response Script
	  div.children("div.PortalResponseScript").each(function() {
		  portalResp.script = this.innerHTML;
		  $(this).css("display", "none");
	  });  
	};
	
	function MarkupHeadElements(fragment) {
		var jObj = $(fragment);
		this.titles = jObj.find("title");
		this.bases = jObj.find("base") ;
		this.links = jObj.find("link") ;
		this.metas = jObj.find("meta") ;
		this.scripts = jObj.find("script") ;           
		this.styles = jObj.find("style") ;
	  //Recreate tag to make sure browser will execute it
		for (var i = 0; i < this.scripts.length; i++) {
			this.scripts[i] = createScriptNode(this.scripts[i]);
		}
	}
	
	function LoadingScripts(fragment) {
		this.immediateScripts = [];
		var jFragment = $(fragment);
		var headers = jFragment.children(".ImmediateScripts").first().html();
		headers = headers.replace(/^\s*/, '').split(",");
		for (var i = 0; i < headers.length; i++) {
			if (headers[i] !== "") {
				this.immediateScripts.push(headers[i]);
			}
		}	
	}
	
	/*
	* This function is used to dynamically append a script to the head tag
	* of the page
	*/
	function appendScriptToHead(scriptId, scriptElement) {
	  var head = $("head");
	  head.find("#" + scriptId).remove();
	  var script = $(scriptElement);
	  script.attr("id", scriptId);
	  head[0].appendChild(script[0]);
	};
	
	function createScriptNode(elem) {
		var script = document.createElement('script');	
		script.type = 'text/javascript';
		script.className = elem.className;
		if (elem.defer) {
			script.defer = elem.defer;
		}
		
		//check if contains source attribute
		if(elem.src) {
		  script.src = elem.src;
		} else {
		  script.text = elem.innerHTML;
		}
		
		return script;
	};
	
	/*****************************************************************************************/
	/*
	* This is the main object that acts both as a field wrapper and a some status method wrapper
	*
	* It is also the object that has the reference to the XHR request thanks to a reference to 
	* the eXo.core.Browser object
	*/
	function AjaxRequest(method, url, queryString) {	
		var instance = new Object() ;
		
		instance.request = null;
		
		instance.timeout = 80000 ;
		instance.aborted = false ;
		
		if(method != null) instance.method = method; else	instance.method = "GET" ;
		if(url != null) instance.url = url; else instance.url = window.location.href ;
		if(queryString != null) instance.queryString = queryString; else instance.queryString = null ;
		
		instance.responseReceived = false ;
	
		instance.status = null ;
		instance.statusText = null ;
		
		instance.responseText = null ;
		instance.responseXML = null ;
		
		instance.onTimeout = null ;
		instance.onLoading = null ;
		instance.onComplete = null ;
		instance.onSuccess = null ;
		instance.callBack = null ;
	
		instance.onError = null;
		
		instance.isAsynchronize = function() {		
			var isASync = false;
			var name = "ajax_async";
			name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]"); 
			var regexS = "[\\?&]"+name+"=([^&#]*)"; 
			var regex = new RegExp( regexS );		
			var results = regex.exec( instance.url );		
			if( results != null ) {			
				isASync = (results[1] == "true") ? true : false;			
			}
			return isASync;
		};	
		
		instance.onLoadingInternalHandled = false ;
		instance.onCompleteInternalHandled = false ;		
		
	    /*
	    * This method is executed only if the boolean "onLoadingInternalHandled" is set to false
	    * The method delegate the call to the ajaxLoading() method of the HttpResponseHandler
	    */
		instance.onLoadingInternal = function() {
			if (instance.onLoadingInternalHandled) return ; 
	
			if (typeof(instance.onLoading) == "function") instance.onLoading(instance) ;
			instance.onLoadingInternalHandled = true ;
		};
	
		/**
		 * evaluate the response and return an object
		 */
	  	instance.evalResponse = function() {
			try {
			  	return eval((instance.responseText || ''));
			} catch (e) {
			  	throw (new Error('Cannot eval the response')) ;
			}
		};
		
		
	    /*
	    * This method is executed only if the boolean "onCompleteInternalHandled" is set to false
	    * The method delegate the call to the ajaxResponse() method of the HttpResponseHandler after
	    * calling the onSuccess() method of the current object
	    *
	    * During the processof this method, all the instance fields are filled with the content coming 
	    * back from the AJAX call. Once the ajaxResponse() is called then the callback object is called
	    * if not null
	    */	
		instance.onCompleteInternal = function(xhr, statusText) {
			if (instance.onCompleteInternalHandled || instance.aborted) return;
			
			if (statusText == "timeout") {
				instance.onTimeoutInternal();
			} else {
				try {
					instance.responseReceived = true;
					instance.status = xhr.status;
					instance.statusText = statusText;
					instance.responseText = xhr.responseText;
					instance.responseXML = xhr.responseXML;
				} catch(err) {
					instance.status = 0;
				}
				
				if(typeof(instance.onComplete) == "function") instance.onComplete(instance);
				
				if (statusText == "success" && typeof(instance.onSuccess) == "function") {
					instance.onSuccess(instance) ;
					instance.onCompleteInternalHandled = true ;
					if (typeof(instance.callBack) == "function") {
						instance.callBack(instance) ;
					} else if (instance.callBack) { // Modified by Uoc Nguyen: allow user use custom javascript code for callback
						try {
							eval(instance.callBack) ;
						}
						catch (e) {
							throw (new Error('Can not execute callback...')) ;
						}
					}
				} else if (typeof(instance.onError) == "function") {
					instance.onError(instance) ;
					instance.onCompleteInternalHandled = false ;
				}				
			}
		};
			
	    /*
	    * This method is executed only if the boolean "onLoadingInternalHandled" is set to false
	    * The method delegate the call to the ajaxTimeout() method of the HttpResponseHandler
	    */
		instance.onTimeoutInternal = function() {
			if (instance == null || instance.request == null || instance.onCompleteInternalHandled) return;
			instance.aborted = true;
			
			if (typeof(instance.onTimeout) == "function") instance.onTimeout(instance);
		} ;
		
		/*
		* This method is directly called from the doRequest() method. It opens a connection to the server,
		* set up the handlers and sends the query to it. Status methods are then called on the request object
		* during the entire lifecycle of the call
		*
		* It also sets up the time out and its call back to the method of the current instance onTimeoutInternal()
		*/
		instance.process = function() {		
			var contentType;
			if (instance.method == "POST") {
				contentType = "application/x-www-form-urlencoded; charset=UTF-8";
			} else {
				contentType =  "text/plain;charset=UTF-8";
			}
			
			instance.request = $.ajax(instance.url, {
				type : instance.method,
				beforeSend : instance.onLoadingInternal,
				complete : instance.onCompleteInternal,
				contentType : contentType,
				data : instance.queryString,
				timeout : instance.timeout,
				cache : false
			});
		};
		
		return instance ;
	} ;
	
	/*****************************************************************************************/
	/*
	* This object is also a wrapper object on top of several methods: 
	*   - executeScript
	*   - updateBlocks
	*   - ajaxTimeout
	*   - ajaxResponse
	*   - ajaxLoading
	* 
	* Those methods are executed during the process of the AJAX call
	*/
	function HttpResponseHandler() {
		var instance = new Object() ;
		/*
		 * instance.to stores a timeout object used to postpone the display of the loading popup
		 * the timeout is defined later in the instance.ajaxLoading function
		 */
		instance.to = null;
		/*
		* This internal method is used to dynamically load JS scripts in the 
		* browser by using the eval() method;
		*/
		instance.executeScript = function(script) {
		  if(script == null || script == "") return ;
		  try {
			eval($("<div />").html(script).text());
		    return;
		  } catch(err) {
		  }
		  var elements = script.split(';') ;
		  if(elements != null && elements.length > 0) {
			  for(var i = 0; i < elements.length; i++) {
			    try {
			      eval(elements[i]) ;
			    } catch(err) {
			      alert(err +" : "+elements[i] + "  -- " + i) ;
			    }
			  }
		  }
		} ;
	
		instance.updateHtmlHead = function(response, callback) {
			if (!response) return;      
			cleanHtmlHead(response);
			var head = $("head");
			var markupHeadElements = response.markupHeadElements;
			if (!markupHeadElements) return;
			
			if (markupHeadElements.titles && markupHeadElements.titles.length != 0) {
				var oldTitle = head.children("title");
				var newTitle = markupHeadElements.titles[markupHeadElements.titles.length - 1];
				if (oldTitle.length) {
					oldTitle.replaceWith(newTitle);
				} else {
					head[0].appendChild(newTitle);
				}
			}                       
	
			appendElementsToHead(head, markupHeadElements.metas);
			appendElementsToHead(head, markupHeadElements.bases);
			appendElementsToHead(head, markupHeadElements.links);                         
			appendElementsToHead(head, markupHeadElements.styles);
			
			var that = this;
			var appendScript = function() {
				if (!markupHeadElements.scripts.length) {
					callback.apply(that);
				} else {
					var tmp = markupHeadElements.scripts.splice(0, 1);
					appendElementsToHead(head, $(tmp));				
					if (!tmp[0].src) {
						appendScript.apply(that);
					}
				} 
			};
					
			for (var i = 0; i < markupHeadElements.scripts.length; i++) {
				var script = markupHeadElements.scripts[i];
				
				if (script.src) {
					if (script.onreadystatechange !== undefined) {
						script.onreadystatechange = (function(sci) {
							return function () {
								if (!sci.readyState ||  /loaded|complete/.test(sci.readyState)) {
									sci.onreadystatechange = null;
									appendScript.apply(that);
								}
							};
						})(script);
					} else {
						script.onload = (function(sci) {
							return function() {sci.onload = null; appendScript.apply(that);};
						})(script);
					}
					script.onerror = (function(sci) {
						return function() {sci.onerror = null; appendScript.apply(that);};
					})(script);
				}
			}
			appendScript.apply(that);
		};
	
	  function cleanHtmlHead(response)
	  {
	    var head = $("head");
	    if (response)
	    {
	      var portletResponses = response.portletResponses;
	      if (portletResponses)
	      {
	        for (var i = 0; i < portletResponses.length; i++)
	        {
	          head.find(".ExHead-" + portletResponses[i].portletId + ":not(title)").remove();
	        }
	      }
	
	      if (response.data)
	      {
	        $(response.data).find(".PORTLET-FRAGMENT").each(function()
	        {
	          head.find(".ExHead-" + this.parentNode.id.replace("EditMode-", "") + ":not(title)").remove();
	        });
	      }
	    }
	    else 
	    {
	    	//This code will be run after we've finished update html
	    	var portlets = $("body .PORTLET-FRAGMENT");
	    	var exHeads = head.find("[class^='ExHead-']:not(title)");
	    	exHeads.each(function()
			{
	    		var portletId = this.className.substring(7);
	    		var del = true;
	    		portlets.each(function() {
	    			if (this.parentNode.id.replace("EditMode-", "") === portletId) {
	    				del = false;
	    			}
	    		});
	    		if (del)
	    		{
	    			$(this).remove();
	    		}
			});    	
	    }
	  }
	
	  function appendElementsToHead(head, elements) {
			if (!elements) return;
			elements.each(function() {
				head[0].appendChild(this);
			});		
		}
		
		/*
		* This methods will replace some block content by new one. 
		* This is the important concept in any AJAX call where JS is used to dynamically
		* refresh a part of the page.
		* 
		* The first argument is an array of blocks to update while the second argument is 
		* the id of the html component that is the parent of the block to update
		* 
		* Each block in the array contains the exact id to update, hence a loop is executed 
		* for each block and the HTML is then dynamically replaced by the new one
		*/
		instance.updateBlocks = function(blocksToUpdate, parentId) {
		  if(!blocksToUpdate) return;
		  var parentBlock = null;
		  if(parentId && parentId != "") {
		    parentBlock =  $("#" + parentId);
		    
		    // Workaround: In the case of the Portlet is being rendered/displayed in Edit Layout mode
		    if (parentBlock.length == 0) {
		      parentBlock = $("#UIPortlet-" + parentId);
		    }
		  }
		  parentBlock = !parentBlock ? $(document) : parentBlock;
		  
		  $.each(blocksToUpdate, function() {
	      var blockToUpdate = this;
			  var target = parentBlock.find("#" + blockToUpdate.blockId);
			  if(target.length == 0) alert(msg.getMessage("TargetBlockNotFound", new Array (blockToUpdate.blockId))) ;		  
			  var newData = $(blockToUpdate.data).find("#" + blockToUpdate.blockId);
			  if(newData.length == 0) alert(msg.getMessage("BlockUpdateNotFound", new Array (blockToUpdate.blockId))) ;
	//		    target.parentNode.replaceChild(newData, target);
			  target.html(newData.html());
			  //update embedded scripts
			  if(blockToUpdate.scripts) {
				  for(var k = 0 ; k < blockToUpdate.scripts.length; k++) {
					  var encodedName = 'script_' + k + '_' +  blockToUpdate.blockId;
					  appendScriptToHead(encodedName, blockToUpdate.scripts[k]);
				  }
			  }
		  });	  
		};
		
		/*
		* This method is called when the AJAX call was too long to be executed
		*/
		instance.ajaxTimeout = function(request) {
		  uiMaskLayer.removeMasks(eXo.portal.AjaxRequest.maskLayer) ;
		  eXo.portal.AjaxRequest.maskLayer = null ;
		  eXo.portal.CurrentRequest = null ;
		  window.location.reload() ;
		};
		
		/*
		* This method is called when the AJAX call is completed and that the request.responseText
		* has been filled with the returning HTML. Hence the goal of this method is to update the
		* diffent blocks dynamically.
		*
		* 1) Create a temporary div element and set the response HTML text to its innerHTML variable of the
		     temp object
		* 2) Use the DOMUtil.findFirstDescendantByClass() method to get the div with the Id "PortalResponse" 
		*    out of the returned HTML
		* 3) Create the PortalResponse object by passing the previous DOM element as an argumen, it will 
	    *    provide an OO view of the PortletResponse and other portal response blocks to update
		* 4) Each portlet response block is the updated using the naming convention "UIPortlet-" + portletId;
		*    and then the script are loaded
		* 5) Then it is each portal block which is updated and the assocaited scripts are evaluated
		*/
		instance.ajaxResponse = function(request, response) {
		  var that = this;
		  if (!response) {
			  var response = new PortalResponse(that.responseText) ;		          
		  }
		  
		  var loadingScripts = response.loadingScripts;
		  var immediateScripts = loadingScripts ? loadingScripts.immediateScripts : [];	    
		  if (immediateScripts.length) {		  
			  window.require(immediateScripts, function() {
				  immediateScripts.length = 0;
				  instance.ajaxResponse.apply(that, [request, response]);
			  });
			  return;
		  }
		  
		  if (response.markupHeadElements)	 {
			  instance.updateHtmlHead(response, function() {
				  response.markupHeadElements  = null;
				  instance.ajaxResponse.apply(that, [request, response]);
			  });
			  return;
		  }
	
		  //Handle the portlet responses
		  var portletResponses =  response.portletResponses ;
		  if(portletResponses) {
			$.each(portletResponses, function() {
	      var portletResponse = this;
				if(!portletResponse.blocksToUpdate) {
			        /*
			        * This means that the entire portlet fragment is included in the portletResponse.portletData
			        * and that it does not contain any finer block to update. Hence replace the innerHTML inside the
			        * id="PORTLET-FRAGMENT" block
			        */
			        var parentBlock =  $("#" + portletResponse.portletId) ;
			        var target = $("#" + portletResponse.portletId + " .PORTLET-FRAGMENT").first();
			        target.html(portletResponse.portletData);
			        
			        //update embedded scripts 
			        if(portletResponse.scripts) {
			        	portletResponse.scripts.each(function(index) {
			        		var encodedName = 'script_' + index + '_' +  portletResponse.portletId;
				            appendScriptToHead(encodedName, this);
			        	});        
			        }	            
		        } else {
		          /*
		          * Else updates each block with the portlet
		          */
		          instance.updateBlocks(portletResponse.blocksToUpdate, portletResponse.portletId) ;
		        }
			});
		  }	
		  if(!response.blocksToUpdate && request.responseText !== "") {
		  	if(confirm(msg.getMessage("SessionTimeout"))) instance.ajaxTimeout(request) ;
		  }
		  try {	    
			  //Handle the portal responses
			  instance.updateBlocks(response.blocksToUpdate) ;
			  //After handle html response. We need to remove extra markup header of removed portlets
			  cleanHtmlHead();
			  instance.executeScript(response.script);		  
			  /**
			   * Clears the instance.to timeout if the request takes less time than expected to get response
			   * Removes the transparent mask so the UI is available again, with cursor "auto"
			   */
			  clearTimeout(instance.to);
			  uiMaskLayer.removeMasks(eXo.portal.AjaxRequest.maskLayer) ;
			  
			  eXo.portal.AjaxRequest.maskLayer = null ;
			  eXo.portal.CurrentRequest = null ;		 
	      } catch (error) {
	             alert(error.message) ;
	      }	  
		};
		
		/*
		  * This method is called when doing an AJAX call, it will put the "Loading" image in the
		  * middle of the page for the entire call of the request
		  */
		instance.ajaxLoading = function(request) {
			if (request.isAsynchronize()) return;
			/**
			 * Waits 2 seconds (2000 ms) to display the loading popup
			 * if the response comes before this timeout, the loading popup won't appear at all
			 * Displays a transparent mask with the "wait" cursor to tell the user something is processing
			 * 
			 * Modified by Truong LE to avoid double click problem
			 */		
			 
			if(eXo.portal.AjaxRequest.maskLayer == null ){
				eXo.portal.AjaxRequest.maskLayer = uiMaskLayer.createTransparentMask();
			}
			instance.to = setTimeout(function() {
				if(eXo.portal.AjaxRequest.maskLayer != null) {
					uiMaskLayer.showAjaxLoading(eXo.portal.AjaxRequest.maskLayer);			   
				}
			}, 2000);
		};
		
		return instance ;
	}
	
	/*
	* The doRequest() method takes incoming request from GET and POST calls
	* The second argument is the URL to target on the server
	* The third argument is the query string object which is created out of
	* a form element, this value is not null only when there is a POST request.
	*
	* 1) An AjaxRequest object is instanciated, it holds the reference to the
	*    XHR method
	* 2) An HttpResponseHandler object is instantiated and its methods like
	*    ajaxResponse, ajaxLoading, ajaxTimeout are associated with the one from
	*    the AjaxRequest and will be called by the XHR during the process method 
	*/
	window.doRequest = function(method, url, queryString, callback) {
      // URL parameter can be passed in strict encoded format, AjaxRequest() expects a plain url param
      var urlPlain = url.replace(/&amp;/g, "&");
	  request = new AjaxRequest(method, urlPlain, queryString) ;
	  handler = new HttpResponseHandler() ;
	  request.onSuccess = handler.ajaxResponse ;
	  request.onLoading = handler.ajaxLoading ;
	  request.onTimeout = handler.ajaxTimeout ;
	  request.callBack = callback ;
	  eXo.portal.CurrentRequest = request ;
	  request.process() ;
	  eXo.session.startItv();
	}
	
	/**
	 * Abort an ajax request
	 * @return
	 */
	window.ajaxAbort = function() {	
	  uiMaskLayer.removeMasks(eXo.portal.AjaxRequest.maskLayer) ;
	  eXo.portal.AjaxRequest.maskLayer = null ;	  
	
	  eXo.portal.CurrentRequest.request.abort() ;  
	  eXo.portal.CurrentRequest.aborted = true ;
	  eXo.portal.CurrentRequest = null ;
	}
	
	/**
	 * Create an ajax GET request
	 * @param {String} url - Url
	 * @param {boolean} async - asynchronous or none
	 * @return {String} response text if request is not async
	 */
	window.ajaxAsyncGetRequest = function(url, async) {
		return ajaxRequest("GET", url, async);
	}
	
	/**
	 * Create an ajax request
	 * @param {String} method - GET, POST, etc
	 * @param {String} url - Url
	 * @param {boolean} async - asynchronous or none
	 * @return {String} response text if request is not async
	 */
	window.ajaxRequest = function(method, url, async, queryString) {
	  if(async == undefined) async = true ;
	  var resp;
	  $.ajax(url, {
		  type: method,
		  async : async,
		  data : queryString,
		  headers : {"Cache-Control" : "max-age=86400"},
		  complete : function(jqXHR) {
			  resp = jqXHR.responseText;
		  }
	  });
	  eXo.session.startItv();
	  if(!async) return resp ;
	}
	
	/**
	 * Redirect browser to url
	 * @param url
	 * @return
	 */
	window.ajaxRedirect = function(url) {
		url =	url.replace(/&amp;/g, "&") ;
		window.location.href = url ;
	}
	
	eXo.portal.AjaxRequest = AjaxRequest.prototype.constructor ;
})($, base, uiMaskLayer, msg);