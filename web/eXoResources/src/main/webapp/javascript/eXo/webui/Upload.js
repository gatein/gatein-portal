/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

(function($, base) {
	var uiUploadInput = {
	  listUpload : [],
	  refreshTime : 1000,
	  delayTime : 5000,  
	
	  /**
	   * Initialize upload and create a upload request to server
	   * 
	   * @param {String}
	   *          uploadId identifier upload
	   */
	  initUploadEntry : function(uploadId, isDynamicMode) {
	    if (isDynamicMode && uploadId.length > 1) {
	      isDynamicMode = true;
	    } else {
	      isDynamicMode = false;
	    }
	    
	    if (!this.progressURL) {
	    	var context = eXo.env.server.context;
	        this.progressURL = context + "/upload?action=progress&uploadId=";
	        this.uploadURL = context + "/upload?action=upload&uploadId=";
	        this.abortURL = context + "/upload?action=abort&uploadId=";
	        this.deleteURL = context + "/upload?action=delete&uploadId=";
	    }
	    
	    for ( var i = 0; i < uploadId.length; i++) {
	      var url = this.progressURL + uploadId[i];
	      var responseText = ajaxAsyncGetRequest(url, false);
	      try {        
	    	  eval("var response = " + responseText);
	      } catch (err) {
	        return;
	      }
	      
	      var uploadCont = $("#UploadInputContainer" + uploadId[i]);
	      uploadCont.on("click", ".DeleteFileLable, .Abort, .RemoveFile", (function(id) {
	    		  return function() {
	    			  if ($(this).hasClass("RemoveFile")) {
	    				  uiUploadInput.deleteUpload(id, isDynamicMode && uploadId.length > 1);    			      			      		 
	    			  } else {
	    				  uiUploadInput.abortUpload(id, isDynamicMode);
	    			  }
	    		  };
	      })(uploadId[i]));
	      
	      uploadCont.on("change", ".file", (function(id) {
	    	  return function() {
	    		  uiUploadInput.upload(id);    		  
	    	  };
	      })(uploadId[i]));
	      
	      if (response.upload[uploadId[i]] == undefined
	          || response.upload[uploadId[i]].percent == undefined) {
	        this.createEntryUpload(uploadId[i], isDynamicMode);
	      } else if (response.upload[uploadId[i]].percent == 100) {
	        this.showUploaded(uploadId[i], response.upload[uploadId[i]].fileName);
	      }
	    }
	  }, 
	
	  createEntryUpload : function(id, isDynamicMode) {	
	    var div = document.getElementById('UploadInput' + id);
	    var url = document.getElementById('RemoveInputUrl' + id).value;
	    var label = document.getElementById('RemoveInputLabel').value;
	    var inputHTML = "<input id='file" + id
	        + "' class='file' name='file' type='file' onkeypress='return false;'";
	    inputHTML += "/>";
	    if (isDynamicMode) {
	      inputHTML += "<a class='ActionLabel' href='javascript:void(0)' onclick=\""
	          + url + "\">" + label + "</a>";
	    }
	    div.style.display = 'block';
	    div.innerHTML = inputHTML;
	  },
	
	  displayUploadButton : function(id) {
	    var flag = true;
	    if (id instanceof Array) {
	      var img = document.getElementById('IconUpload' + id[0]);
	      for ( var i = 0; i < id.length; i++) {
	        var input = document.getElementById('file' + id[i]);
	        if (input == null)
	          flag = true;
	        else if (input.value == null || input.value == '')
	          flag = false;
	      }
	      if (flag)
	        img.style.display = 'none';
	    } else
	      return;
	  },
	
	  showUploaded : function(id, fileName) {
	    this.remove(id);
	    var container = parent.document.getElementById('UploadInputContainer' + id);
	    var element = document.getElementById('ProgressIframe' + id);
	    element.innerHTML = "<span></span>";
	
	    jCont = $(container);
	    var UploadInput = jCont.find('#UploadInput' + id);
	    UploadInput.hide();
	
	    var progressIframe = jCont.find('#ProgressIframe' + id);
	    progressIframe.hide();
	
	    var selectFileFrame = jCont.find(".SelectFileFrame").first();
	    selectFileFrame.show();
	
	    var fileNameLabel = selectFileFrame.find(".FileNameLabel").first();
	    if (fileName.length)
	      fileNameLabel.html(decodeURIComponent(fileName));
	
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.hide();
	  },
	
	  refreshProgress : function(uploadId) {
	    var list = this.listUpload;
	    if (list.length < 1)
	      return;
	    var url = this.progressURL;
	
	    for ( var i = 0; i < list.length; i++) {
	      url = url + "&uploadId=" + list[i];
	    }
	    var responseText = ajaxAsyncGetRequest(url, false);
	    if (this.listUpload.length > 0) {
	      setTimeout(
	          function() {uiUploadInput.refreshProgress(uploadId);},
	          this.refreshTime);
	    }
	
	    try {
	    	eval("var response = " + responseText);
	    } catch (err) {
	      return;
	    }
	
	    for (id in response.upload) {
	      var container = parent.document.getElementById('UploadInputContainer'
	          + id);
	      var jCont = $(container);
	      if (response.upload[id].status == "failed") {
	        this.abortUpload(id);
	        var message = jCont.children(".LimitMessage").first().html();
	        message = message.replace("{0}", response.upload[id].size);
	        message = message.replace("{1}", response.upload[id].unit);
	        alert(message);
	        continue;
	      }
	      var element = document.getElementById('ProgressIframe' + id);
	      var percent = response.upload[id].percent;
	      var progressBarMiddle = jCont.find(".ProgressBarMiddle").first();
	      var blueProgressBar = progressBarMiddle.children(".BlueProgressBar").first();
	      var progressBarLabel = blueProgressBar.children(".ProgressBarLabel").first();
	      blueProgressBar.css("width", percent + "%");
	      progressBarLabel.html(percent + "%");
	
	      if (percent == 100) {
	        this.showUploaded(id, response.upload[id].fileName);
	      }
	    }
	
	    if (this.listUpload.length < 1)
	      return;
	
	    if (element) {
	      element.innerHTML = "Uploaded " + percent + "% "
	          + "<span class='Abort'>Abort</span>";
	    }
	  },
	
	  deleteUpload : function(id, isDynamicMode) {
	    var url = this.deleteURL + id;
	    ajaxRequest('GET', url, false);
	    
	    var container = parent.document.getElementById('UploadInputContainer' + id);
	    var selectFileFrame = $(container).find(".SelectFileFrame").first();
	    selectFileFrame.hide();
	
	    this.createEntryUpload(id, isDynamicMode);
	  },
	
	  abortUpload : function(id, isDynamicMode) {
	    this.remove(id);
	    var url = this.abortURL + id;
	    ajaxRequest('GET', url, false);
	    
	    var container = parent.document.getElementById('UploadInputContainer' + id);
	    var jCont = $(container);
	    var progressIframe = jCont.find('#ProgressIframe' + id);
	    progressIframe.hide();
	
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.hide();
	
	    this.createEntryUpload(id, isDynamicMode);
	  },
	
	  /**
	   * Start upload file
	   * 
	   * @param {Object}
	   *          clickEle
	   * @param {String}
	   *          id
	   */
	  doUpload : function(id) {
	    var container = parent.document.getElementById('UploadInputContainer' + id);
	    var jCont = $(container);
	    this.displayUploadButton(id);
	    if (id instanceof Array) {
	      for ( var i = 0; i < id.length; i++) {
	        this.doUpload(id[i]);
	      }
	    } else {
	      var file = document.getElementById('file' + id);
	      if (file == null || file == undefined)
	        return;
	      if (file.value == null || file.value == '')
	        return;
	      var temp = file.value;
	
	      var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	      progressBarFrame.show();
	
	      var progressBarMiddle = jCont.find(".ProgressBarMiddle").first();
	      var blueProgressBar = progressBarMiddle.children(".BlueProgressBar").first();
	      var progressBarLabel = blueProgressBar.children(".ProgressBarLabel").first();
	      blueProgressBar.css("width", "0%");
	      progressBarLabel.html("0%");
	
	      var uploadAction = this.uploadURL + id;
	      var formHTML = "<form id='form" + id
	          + "' class='UIUploadForm' style='margin: 0px; padding: 0px' action='"
	          + uploadAction
	          + "' enctype='multipart/form-data' target='UploadIFrame" + id
	          + "' method='post'></form>";
	      var div = document.createElement("div");
	      div.innerHTML = formHTML;
	      var form = div.firstChild;
	
	      form.appendChild(file);
	      document.body.appendChild(div);
	      form.submit();
	      document.body.removeChild(div);
	
	      if (this.listUpload.length == 0) {
	        this.listUpload.push(id);
	        setTimeout(function() {uiUploadInput.refreshProgress(id);},
	            this.refreshTime);
	      } else {
	        this.listUpload.push(id);
	      }
	
	      var UploadInput = jCont.find('#UploadInput' + id);
	      UploadInput.hide();
	    }
	  },
	
	  upload : function(id) {
	    setTimeout(function() {uiUploadInput.doUpload(id)}, this.delayTime);
	  }, 
	  
	  remove : function(id) {
	  	var idx = $.inArray(id, uiUploadInput.listUpload);
	  	if (idx !== -1) {
	  		uiUploadInput.listUpload.splice(idx, 1);  		
	  	}
	  }
	};
	
	var uiUpload = {
	  listUpload : [],
	  
	  //This attribute should be persisted belong to particular upload component
	  isAutoUpload : false,
	  // this.listLimitMB = new Array();
	  /**
	   * Initialize upload and create a upload request to server
	   * 
	   * @param {String}
	   *          uploadId identifier upload
	   * @param {boolean}
	   *          isAutoUpload auto upload or none
	   */
	  initUploadEntry : function(uploadId, isAutoUpload) {
	    var url = eXo.env.server.context + "/upload?";
	    url += "action=progress&uploadId=" + uploadId;
	    var responseText = ajaxAsyncGetRequest(url, false);
	    
	    try {
	      eval("var response = " + responseText);
	    } catch (err) {
	      return;
	    }
	    
	    var uploadInput = $("#" + uploadId);
	    uploadInput.find(".DeleteFileLable, .Abort").on("click", function() {
	    	uiUpload.abortUpload(uploadId);
	    });
	    uploadInput.find(".RemoveFile").on("click", function() {
	    	uiUpload.deleteUpload(uploadId);
	    });
	    
	    this.isAutoUpload = isAutoUpload;
	    if (response.upload[uploadId] == undefined
	        || response.upload[uploadId].percent == undefined) {
	      // eXo.webui.UIUpload.listLimitMB.push();
	      this.createUploadEntry(uploadId, isAutoUpload);
	    } else if (response.upload[uploadId].percent == 100) {
	      this.showUploaded(uploadId, (response.upload[uploadId].fileName));
	    }
	  },
	
	  createUploadEntry : function(uploadId, isAutoUpload) {
	    var iframe = document.getElementById(uploadId + 'uploadFrame');
	    var idoc = iframe.contentWindow.document;
	    var uploadAction = eXo.env.server.context + "/upload?";
	    uploadAction += "uploadId=" + uploadId + "&action=upload";
	
	    var uploadHTML = "";
	    uploadHTML += "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' 'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>";
	    uploadHTML += "<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='"
	        + base.I18n.lang + "' dir='" + base.I18n.dir + "'>";
	    uploadHTML += "<head>";
	    uploadHTML += "<style type='text/css'>";
	    uploadHTML += ".UploadButton {width: 20px; height: 20px; cursor: pointer; vertical-align: bottom;";
	    uploadHTML += " background: url('/eXoResources/skin/DefaultSkin/webui/component/UIUpload/background/UpArrow16x16.gif') no-repeat left; }";
	    uploadHTML += "</style>";
	    uploadHTML += "</head>";
	    uploadHTML += "<body style='margin: 0px; border: 0px;'>";
	    uploadHTML += "  <form id='" + uploadId
	        + "' class='UIUploadForm' style='margin: 0px; padding: 0px' action='"
	        + uploadAction + "' enctype='multipart/form-data' method='post'>";
	    if (isAutoUpload) {
	      uploadHTML += "    <input type='file' name='file' id='file' value='' onchange='parent.require(\"SHARED/upload\").UIUpload.upload(this, \""
	          + uploadId + "\")' onkeypress='return false;' />";
	    } else {
	      uploadHTML += "    <input type='file' name='file' id='file' value='' onkeypress='return false;' />";
	      uploadHTML += "    <img class='UploadButton' style='width: 20px; height: 20px; cursor: pointer; vertical-align: bottom; background: url(\"/eXoResources/skin/DefaultSkin/webui/component/UIUpload/background/UpArrow16x16.gif\") no-repeat left;' " +
	      		"onclick='parent.require(\"SHARED/upload\").UIUpload.upload(this, \"" + uploadId + "\")' alt='' src='/eXoResources/skin/sharedImages/Blank.gif'/>";
	    }
	    uploadHTML += "  </form>";
	    uploadHTML += "</body>";
	    uploadHTML += "</html>";
	
	    if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
	      // workaround for Chrome
	      // When submit in iframe with Chrome, the iframe.contentWindow.document
	      // seems not be reconstructed correctly
	      idoc.open();
	      idoc.close();
	      idoc.documentElement.innerHTML = uploadHTML;
	    } else {
	      idoc.open();
	      idoc.write(uploadHTML);
	      idoc.close();
	    }
	  },
	
	  /**
	   * Refresh progress bar to update state of upload progress
	   * 
	   * @param {String}
	   *          elementId identifier of upload bar frame
	   */
	  refreshProgress : function(elementId) {
	    var list = uiUpload.listUpload;
	    if (list.length < 1)
	      return;
	    var url = eXo.env.server.context + "/upload?";
	    url += "action=progress";
	    // var url = eXo.env.server.context + "/upload?action=progress";
	    for ( var i = 0; i < list.length; i++) {
	      url = url + "&uploadId=" + list[i];
	    }
	    var responseText = ajaxAsyncGetRequest(url, false);
	    if (list.length > 0) {
	      setTimeout(function() {
	    	  uiUpload.refreshProgress(elementId);
	      }, 1000);
	    }
	
	    try {
	      eval("var response = " + responseText);
	    } catch (err) {
	      return;
	    }
	
	    for (id in response.upload) {
	      var container = parent.document.getElementById(elementId);
	      var jCont = $(container);
	      if (response.upload[id].status == "failed") {
	        this.abortUpload(id);
	        var message = jCont.children(".LimitMessage").first().html();
	        message = message.replace("{0}", response.upload[id].size);
	        message = message.replace("{1}", response.upload[id].unit);
	        alert(message);
	        continue;
	      }
	      var element = document.getElementById(id + "ProgressIframe");
	      var percent = response.upload[id].percent;
	      var blueProgressBar = jCont.find(".ProgressBarMiddle .BlueProgressBar").first();
	      var progressBarLabel = blueProgressBar.children(".ProgressBarLabel").first();
	      blueProgressBar.css("width", percent + "%");
	
	      progressBarLabel.html(percent + "%");
	      if (percent == 100) {
	        this.showUploaded(id, response.upload[id].fileName);
	      }
	    }
	
	    if (uiUpload.listUpload.length < 1)
	      return;
	
	    if (element) {
	      element.innerHTML = "Uploaded " + percent + "% "
	          + "<span class='Abort'>Abort</span>";
	    }
	  },
	  /**
	   * Show uploaded state when upload has just finished a file
	   * 
	   * @param {String}
	   *          id uploaded identifier
	   * @param {String}
	   *          fileName uploaded file name
	   */
	  showUploaded : function(id, fileName) {
	    uiUpload.remove(id);
	    var container = parent.document.getElementById(id);
	    var jCont = $(container);
	    var element = document.getElementById(id + "ProgressIframe");
	    element.innerHTML = "<span></span>";
	
	    var uploadIframe = jCont.find("#" + id + "UploadIframe");
	    uploadIframe.hide();
	    var progressIframe = jCont.find("#" + id + "ProgressIframe");
	    progressIframe.hide();
	
	    var selectFileFrame = jCont.find(".SelectFileFrame").first();
	    selectFileFrame.show();
	    var fileNameLabel = selectFileFrame.find(".FileNameLabel").first();
	    if (fileName != null)
	      fileNameLabel.html(decodeURIComponent(fileName));
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.hide();
	    var tmp = element.parentNode;
	    var temp = tmp.parentNode;
	    // TODO: dang.tung - always return true even we reload browser
	    var input = parent.document.getElementById('input' + id);
	    input.value = "true";
	  },
	  /**
	   * Abort upload process
	   * 
	   * @param {String}
	   *          id upload identifier
	   */
	  abortUpload : function(id) {
	    uiUpload.remove(id);
	    var url = eXo.env.server.context + "/upload?";
	    url += "uploadId=" + id + "&action=abort";
	    // var url = eXo.env.server.context + "/upload?uploadId="
	    // +id+"&action=abort" ;
	    ajaxRequest('GET', url, false);
	
	    var container = parent.document.getElementById(id);
	    var jCont = $(container);
	    var uploadIframe = jCont.find("#" + id + "UploadIframe");
	    uploadIframe.show();
	    uiUpload.createUploadEntry(id, uiUpload.isAutoUpload);
	    var progressIframe =jCont.find("#" + id + "ProgressIframe");
	    progressIframe.hide();
	
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.hide();
	    var selectFileFrame = jCont.find(".SelectFileFrame").first();
	    selectFileFrame.hide();
	
	    var input = parent.document.getElementById('input' + id);
	    input.value = "false";
	  },
	  /**
	   * Delete uploaded file
	   * 
	   * @param {String}
	   *          id upload identifier
	   */
	  deleteUpload : function(id) {
	    var url = eXo.env.server.context + "/upload?";
	    url += "uploadId=" + id + "&action=delete";
	    // var url = eXo.env.server.context + "/upload?uploadId="
	    // +id+"&action=delete" ;
	    ajaxRequest('GET', url, false);
	    
	    var container = parent.document.getElementById(id);
	    var jCont = $(container);
	    var uploadIframe = jCont.find("#" + id + "UploadIframe");
	    uploadIframe.show();
	    uiUpload.createUploadEntry(id, this.isAutoUpload);
	    var progressIframe = jCont.find("#" + id + "ProgressIframe");
	    progressIframe.hide();
	
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.hide();
	    var selectFileFrame = jCont.find(".SelectFileFrame").first();
	    selectFileFrame.hide();
	
	    var input = parent.document.getElementById('input' + id);
	    input.value = "false";
	  },
	
	  /**
	   * Start upload file
	   * 
	   * @param {Object}
	   *          clickEle
	   * @param {String}
	   *          id
	   */
	  upload : function(clickEle, id) {
	    var container = parent.document.getElementById(id);
	    var uploadFrame = parent.document.getElementById(id + "uploadFrame");
	    var form = uploadFrame.contentWindow.document.getElementById(id);
	
	    var file = $(form).find("#file");
	    if (file.attr("value") == null || file.attr("value") == '')
	      return;
	
	    var jCont = $(container);
	    var progressBarFrame = jCont.find(".ProgressBarFrame").first();
	    progressBarFrame.show();
	    var blueProgressBar = jCont.find(".ProgressBarMiddle .BlueProgressBar").first();
	    var progressBarLabel = blueProgressBar.children(".ProgressBarLabel").first();
	    blueProgressBar.css("width", "0%");
	    progressBarLabel.html("0%");
	
	    var input = parent.document.getElementById('input' + id);
	    input.value = "true";
	
	    var uploadIframe = jCont.find("#" + id + "UploadIframe");
	    uploadIframe.hide();
	    var progressIframe = jCont.find("#" + id + "ProgressIframe");
	    progressIframe.hide();
	
	    form.submit();
	
	    var list = uiUpload.listUpload;
	    if (list.length == 0) {
	      uiUpload.listUpload.push(form.id);
	      setTimeout(function() {uiUpload.refreshProgress(id);}, 1000);
	    } else {
	      uiUpload.listUpload.push(form.id);
	    }
	  },
	  
	  remove : function(id) {
	  	var idx = $.inArray(id, uiUpload.listUpload);
	  	if (idx !== -1) {
	  		uiUpload.listUpload.splice(idx, 1);  		
	  	}
	  }
	};
	
	return {
		UIUpload : uiUpload,
		UIUploadInput : uiUploadInput
	};
})($, base);