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

function UINotification(){
	this.timerlen = 5;
	this.slideAniLen = 1000;
	this.timerID = new Array();
	this.startTime = new Array();
	this.object = new Array();
	this.endHeight = new Array();
	this.moving = new Array();
	this.endSlideUpCallback = new Array();
	this.dir = new Array();	
	this.importantNoti = new Array();
	this.flagNoti = new Array();
	this.totalCurrentMessage = 0;
	this.numberMessageRecepted = 0;
	this.numImptNoti = 0;
	if (eXo.core.Topic != null) {
		eXo.core.Topic.subscribe("/eXo/portal/notification", function(event){
			eXo.webui.UINotification.addMessage(event.message);
		})
	}
}
/**
 * Display notification on sliding down
 * @param {String} objectName identifier of notification object
 */
UINotification.prototype.slideDown = function(objectName){
  if(this.moving[objectName]) return;        
  if(document.getElementById(objectName).style.display != "none") return; 
  this.moving[objectName] = true;
  this.dir[objectName] = "down";
  this.startSlide(objectName);      
}
/**
 * Set sliding down and up
 */
UINotification.prototype.slideDownUp = function(objectName, endSlideUpCallback){
	this.slideDown(objectName);
	this.endSlideUpCallback[objectName] = endSlideUpCallback;
	if(this.flagNoti[objectName]) setTimeout("eXo.webui.UINotification.slideUp('" + objectName + "')", 3000);	
}
/**
 * Event when user click "x" close button on the notification
 * If notify content is important, immediately it will not be close and uses closing timeout
 */
UINotification.prototype.closeNotification = function() {
	for(var i = 0; i < this.importantNoti.length; i ++) {
		this.flagNoti[this.importantNoti[i]] = true;
		setTimeout("eXo.webui.UINotification.slideUp('" + this.importantNoti[i] + "')", 100);
	}
}
/**
 * Display (hide) notification as sliding up
 */
UINotification.prototype.slideUp = function(objectName){
  if(this.moving[objectName]) return;        
  if(document.getElementById(objectName).style.display == "none") return;   
  this.moving[objectName] = true;
  this.dir[objectName] = "up";
  this.startSlide(objectName);
}
/**
 * Start slide animation
 * @param {String} objectName identifier of notification object
 */
UINotification.prototype.startSlide = function(objectName){
  this.object[objectName] = document.getElementById(objectName);
  this.endHeight[objectName] = parseInt(this.object[objectName].style.height);
  this.startTime[objectName] = (new Date()).getTime();        
  if(this.dir[objectName] == "down"){
          this.object[objectName].style.height = "1px";
  }
  this.object[objectName].style.display = "block";
  this.timerID[objectName] = setInterval('eXo.webui.UINotification.slideTick(\'' + objectName + '\');',this.timerlen);
}
/**
 * Calculate height property every tick time
 * @param {String} objectName name of object
 */
UINotification.prototype.slideTick = function(objectName){
  var elapsed = (new Date()).getTime() - this.startTime[objectName];		
  if (elapsed > this.slideAniLen)
    this.endSlide(objectName);
  else {
    var d =Math.round(elapsed / this.slideAniLen * this.endHeight[objectName]);
    if(this.dir[objectName] == "up")
            d = this.endHeight[objectName] - d;
    this.object[objectName].style.height = d + "px";
  }
  return;
}
/**
 * Remove notification from page body
 */
UINotification.prototype.destroyUINotification = function(){	
	var UINotification = document.getElementById("UINotification");		
	document.getElementsByTagName("body")[0].removeChild(UINotification);	
}
/**
 * End sliding, clean and destroy resource
 */
UINotification.prototype.endSlide = function(objectName){
  clearInterval(this.timerID[objectName]);
  if(this.dir[objectName] == "up") {
  	this.object[objectName].style.display = "none";
		if(this.endSlideUpCallback[objectName]) {
			this.endSlideUpCallback[objectName](objectName);
			this.totalCurrentMessage --;				
			if(this.totalCurrentMessage == 0) {	
			  this.destroyUINotification();
			  return;		
			}
		}
	}
  this.object[objectName].style.height = this.endHeight[objectName] + "px";
  delete(this.moving[objectName]);
  delete(this.timerID[objectName]);
  delete(this.startTime[objectName]);
  delete(this.endHeight[objectName]);
  delete(this.object[objectName]);
  delete(this.dir[objectName]);		
  delete(this.flagNoti[objectName]);		
  return;
}
/**
 * Remove object from document
 * @param {String} objectName identifier of Object 
 */
UINotification.prototype.deleteBox = function(objectName) {
	var el = document.getElementById(objectName);
	el.parentNode.removeChild(el);
}
/**
 * Create frame that contains whole notification content
 * @return {String} htmlString html string
 */
UINotification.prototype.createFrameForMessages = function() {
	var htmlString = "";		
	htmlString += 	"<div class=\"UIPopupNotification\">";
	htmlString += 		"<div class=\"TLPopupNotification\">";
	htmlString += 			"<div class=\"TRPopupNotification\">";
	htmlString += 				"<div class=\"TCPopupNotification\" ><span></span></div>";
	htmlString += 			"</div>";
	htmlString += 		"</div>";
	htmlString += 		"<div class=\"MLPopupNotification\">";
	htmlString += 			"<div class=\"MRPopupNotification\">";
	htmlString += 				"<div class=\"MCPopupNotification\">";
	htmlString += 					"<div class=\"TitleNotification\">";
	htmlString += 						"<a class=\"ItemTitle\" href=\"#\">Notification</a>";
	htmlString += 						"<a class=\"Close\" href=\"#\" onclick=\"eXo.webui.UINotification.closeNotification();\"><span></span></a>";
	htmlString += 					"</div>";
	htmlString += 					"<div id=\"UINotificationContent\">";
	htmlString += 					"</div>";			
	htmlString += 				"</div>";
	htmlString += 			"</div>";
	htmlString += 		"</div>";
	htmlString += 		"<div class=\"BLPopupNotification\">";
	htmlString += 			"<div class=\"BRPopupNotification\">";
	htmlString += 				"<div class=\"BCPopupNotification\"><span></span></div>";
	htmlString += 			"</div>";
	htmlString += 		"</div>";
	htmlString += 	"</div>";
	return htmlString;
}
/**
 * Add message content to notification
 * @param {String} messageContent content to notify
 * @param {boolean} flag
 */
UINotification.prototype.addMessage = function(messageContent, flag) {
	var currMessageBoxId = "UIMessageBox_" + this.numberMessageRecepted++;
	var UIMessageContent = document.createElement('div');
	this.totalCurrentMessage++;	
	
	this.flagNoti[currMessageBoxId] = !flag;
	if(flag) {
		this.importantNoti[this.numImptNoti] = currMessageBoxId;
		this.numImptNoti++;
	}
	UIMessageContent.id = currMessageBoxId;
	UIMessageContent.style.height = "35px";
	UIMessageContent.style.display = "none";
	UIMessageContent.className = "Item";
	UIMessageContent.innerHTML = "<div>" + messageContent + "</div>";	
	var UINotification = document.getElementById("UINotification");
	if (UINotification == null) {
		document.body.appendChild(document.createElement('div')).id = "UINotification";
		UINotification = document.getElementById("UINotification");	
		UINotification.className = 'UINotification';
		UINotification.innerHTML=this.createFrameForMessages();
	} 
	var msPanel = document.getElementById("UINotificationContent");	
	msPanel.appendChild(UIMessageContent);	
	eXo.webui.UINotification.slideDownUp(currMessageBoxId, this.deleteBox);
}