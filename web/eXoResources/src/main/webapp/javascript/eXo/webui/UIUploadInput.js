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

function UIUploadInput() {
  this.listUpload = new Array();
  this.refreshTime = 1000;
  this.delayTime = 5000;
  this.progressURL = eXo.env.server.context + "/upload?action=progress&uploadId=";
  this.uploadURL = eXo.env.server.context + "/upload?action=upload&uploadId=" ;
  this.abortURL = eXo.env.server.context + "/upload?action=abort&uploadId=" ;
  this.deleteURL = eXo.env.server.context + "/upload?action=delete&uploadId=" ;
};

/**
 * Initialize upload and create a upload request to server
 * @param {String} uploadId identifier upload
 * @param {boolean} isAutoUpload auto upload or none
 */
UIUploadInput.prototype.initUploadEntry = function(uploadId, isAutoUpload) {
  for(var i = 0; i < uploadId.length; i++) {
    var url = this.progressURL + uploadId[i] ;
	var responseText = ajaxAsyncGetRequest(url, false);
	
	var response;
    try {
      eval("response = "+responseText);
    } catch(err) {
      return;  
    }

	  if(response.upload[uploadId[i]] == undefined || response.upload[uploadId[i]].percent == undefined) {
		  this.createEntryUpload(uploadId[i], isAutoUpload);
	  } else if(response.upload[uploadId[i]].percent == 100)  {
		  this.showUploaded(uploadId[i], response.upload[uploadId[i]].fileName);
	  } 
  }
};

UIUploadInput.prototype.createEntryUpload = function(id, isAutoUpload) {
  var div = document.getElementById('UploadInput' + id);
    
  var inputHTML = "<input id='file" +id+ "' class='file' name='file' type='file' onkeypress='return false;'";

  if(isAutoUpload) {
    inputHTML += " onchange='eXo.webui.UIUploadInput.upload(\"" +id+ "\", " +isAutoUpload+ ");' >";    
  } else {
    inputHTML += " >"
  }
  
  div.style.display = 'block';
  div.innerHTML = inputHTML; 

	if(!isAutoUpload) {
		var prefixId = id.substring(0, id.indexOf('-'));
		var img = document.getElementById('IconUpload' + prefixId + '-0');
		img.style.display = 'block';
	}
};

UIUploadInput.prototype.displayUploadButton = function(id) {
	var flag = true;
	if(id instanceof Array) {
		var img = document.getElementById('IconUpload' + id[0]);
		for(var i = 0; i < id.length; i++) {
			var input = document.getElementById('file' + id[i]);
			if(input == null) flag = true;
			else if(input.value == null || input.value == '') flag = false;
		}
		if(flag) img.style.display = 'none';
	} else return;
};

UIUploadInput.prototype.showUploaded = function(id, fileName) {
  this.listUpload.remove(id);
  var container = parent.document.getElementById('UploadInputContainer' + id);
  var element = document.getElementById('ProgressIframe' + id);
  element.innerHTML =  "<span></span>";
  
  var UploadInput = eXo.core.DOMUtil.findDescendantById(container, 'UploadInput' + id);
  UploadInput.style.display = "none";

  var progressIframe = eXo.core.DOMUtil.findDescendantById(container, 'ProgressIframe' + id);
  progressIframe.style.display = "none";
    
  var selectFileFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
  selectFileFrame.style.display = "block" ;

  var fileNameLabel = eXo.core.DOMUtil.findFirstDescendantByClass(selectFileFrame, "div", "FileNameLabel") ;
  if(fileName != null) fileNameLabel.innerHTML = decodeURIComponent(fileName);

  var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "none" ;
};

UIUploadInput.prototype.refreshProgress = function(uploadId, isAutoUpload) {
  var list =  this.listUpload;
  if(list.length < 1) return;
  var url = this.progressURL;

  for(var i = 0; i < list.length; i++){
    url = url + "&uploadId=" + list[i];
  }
  var responseText = ajaxAsyncGetRequest(url, false);
  if(this.listUpload.length > 0) {
    setTimeout("eXo.webui.UIUploadInput.refreshProgress('" +uploadId+ "', " +isAutoUpload+ ");", this.refreshTime); 
  }
    
  var response;
  try {
    eval("response = "+responseText);
  } catch(err) {
    return;  
  }
  
  for(id in response.upload) {
    var container = parent.document.getElementById('UploadInputContainer' + id);
  	if (response.upload[id].status == "failed") {
  		this.abortUpload(id, isAutoUpload);
  		var message = eXo.core.DOMUtil.findFirstChildByClass(container, "div", "LimitMessage").innerHTML ;
  		message = message.replace("{0}", response.upload[id].size);
  		message = message.replace("{1}", response.upload[id].unit);
  		alert(message);
  		continue;
  	}
    var element = document.getElementById('ProgressIframe' + id);
    var percent = response.upload[id].percent;
    var progressBarMiddle = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarMiddle") ;
    var blueProgressBar = eXo.core.DOMUtil.findFirstChildByClass(progressBarMiddle, "div", "BlueProgressBar") ;
    var progressBarLabel = eXo.core.DOMUtil.findFirstChildByClass(blueProgressBar, "div", "ProgressBarLabel") ;
    blueProgressBar.style.width = percent + "%" ;
    progressBarLabel.innerHTML = percent + "%" ;
    
    if(percent == 100) {
       this.showUploaded(id, response.upload[id].fileName);
    }
  }
  
  if(this.listUpload.length < 1) return;

  if (element) {
    element.innerHTML = "Uploaded "+ percent + "% " +
                        "<span onclick='parent.eXo.webui.UIUploadInput.abortUpload("+uploadId+", "+isAutoUpload+")'>Abort</span>";
  }
};

UIUploadInput.prototype.deleteUpload = function(id, isAutoUpload) {
  var url = this.deleteURL + id;
  var request =  eXo.core.Browser.createHttpRequest();
  request.open('GET', url, false);
  request.setRequestHeader("Cache-Control", "max-age=86400");
  request.send(null);

  var container = parent.document.getElementById('UploadInputContainer' + id);
  var selectFileFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "SelectFileFrame") ;
  selectFileFrame.style.display = "none" ;

  this.createEntryUpload(id, isAutoUpload);
};

UIUploadInput.prototype.abortUpload = function(id, isAutoUpload) {
  this.listUpload.remove(id);
  var url = this.abortURL + id;
  var request =  eXo.core.Browser.createHttpRequest();
  request.open('GET', url, false);
  request.setRequestHeader("Cache-Control", "max-age=86400");
  request.send(null);
  
  var container = parent.document.getElementById('UploadInputContainer' + id);
  var progressIframe = eXo.core.DOMUtil.findDescendantById(container, 'ProgressIframe' + id);
  progressIframe.style.display = "none";

  var progressBarFrame = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame") ;
  progressBarFrame.style.display = "none" ;

  this.createEntryUpload(id, isAutoUpload);
};

/**
 * Start upload file
 * @param {Object} clickEle
 * @param {String} id
 */

UIUploadInput.prototype.doUpload = function(id, isAutoUpload) {
  var DOMUtil = eXo.core.DOMUtil;  
  var container = parent.document.getElementById('UploadInputContainer' + id);  
  this.displayUploadButton(id);
  if(id instanceof Array) {
    for(var i = 0; i < id.length; i++) {
      this.doUpload(id[i], isAutoUpload);    
    }
  } else {
    var file = document.getElementById('file' + id);
    if (file == null || file == undefined) return;
    if (file.value == null || file.value == '') return;
    var temp = file.value;

    var progressBarFrame = DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarFrame");
    progressBarFrame.style.display = "block";
  
    var progressBarMiddle = DOMUtil.findFirstDescendantByClass(container, "div", "ProgressBarMiddle");
    var blueProgressBar = DOMUtil.findFirstChildByClass(progressBarMiddle, "div", "BlueProgressBar");
    var progressBarLabel = DOMUtil.findFirstChildByClass(blueProgressBar, "div", "ProgressBarLabel");
    blueProgressBar.style.width = "0%";
    progressBarLabel.innerHTML = "0%";
  
    var uploadAction = this.uploadURL + id; 
    var formHTML = "<form id='form" +id+ "' class='UIUploadForm' style='margin: 0px; padding: 0px' action='" 
		+ uploadAction + "' enctype='multipart/form-data' target='UploadIFrame" + id + "' method='post'></form>";   
    var div = document.createElement("div");
    div.innerHTML = formHTML;
    var form = div.firstChild;      
    
    form.appendChild(file);
    document.body.appendChild(div);
    form.submit();
    document.body.removeChild(div);
    
    if(this.listUpload.length == 0) {
      this.listUpload.push(id);
      setTimeout("eXo.webui.UIUploadInput.refreshProgress('" + id + "', " + isAutoUpload + ");", this.refreshTime);
    } else {
      this.listUpload.push(id);
    }
  }
};

UIUploadInput.prototype.upload = function(id, isAutoUpload) {
  if(isAutoUpload) setTimeout("eXo.webui.UIUploadInput.doUpload('" + id + "', " + isAutoUpload + ")", this.delayTime);  
  else this.doUpload(id, isAutoUpload);
};

eXo.webui.UIUploadInput = new UIUploadInput();
