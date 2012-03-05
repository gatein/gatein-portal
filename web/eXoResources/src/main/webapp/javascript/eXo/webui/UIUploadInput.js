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

eXo.webui.UIUploadInput = {
  listUpload : [],
  refreshTime : 1000,
  delayTime : 5000,
  progressURL : eXo.env.server.context + "/upload?action=progress&uploadId=",
  uploadURL : eXo.env.server.context + "/upload?action=upload&uploadId=",
  abortURL : eXo.env.server.context + "/upload?action=abort&uploadId=",
  deleteURL : eXo.env.server.context + "/upload?action=delete&uploadId=",

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
    for ( var i = 0; i < uploadId.length; i++) {
      var url = this.progressURL + uploadId[i];
      var responseText = ajaxAsyncGetRequest(url, false);
      try {        
    	  var response = eval("response = " + responseText);
      } catch (err) {
        return;
      }

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
    inputHTML += " onchange='eXo.webui.UIUploadInput.upload(\"" + id
        + "\");'/>";
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
    this.listUpload.remove(id);
    var container = parent.document.getElementById('UploadInputContainer' + id);
    var element = document.getElementById('ProgressIframe' + id);
    element.innerHTML = "<span></span>";

    jCont = xj(container);
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
          "eXo.webui.UIUploadInput.refreshProgress('" + uploadId + "');",
          this.refreshTime);
    }

    try {
    	var response = eval("response = " + responseText);
    } catch (err) {
      return;
    }

    for (id in response.upload) {
      var container = parent.document.getElementById('UploadInputContainer'
          + id);
      var jCont = xj(container);
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
          + "<span onclick='parent.eXo.webui.UIUploadInput.abortUpload(\""
          + uploadId + "\")'>Abort</span>";
    }
  },

  deleteUpload : function(id, isDynamicMode) {
    var url = this.deleteURL + id;
    ajaxRequest('GET', url, false);
    
    var container = parent.document.getElementById('UploadInputContainer' + id);
    var selectFileFrame = xj(container).find(".SelectFileFrame").first();
    selectFileFrame.hide();

    this.createEntryUpload(id, isDynamicMode);
  },

  abortUpload : function(id, isDynamicMode) {
    this.listUpload.remove(id);
    var url = this.abortURL + id;
    ajaxRequest('GET', url, false);
    
    var container = parent.document.getElementById('UploadInputContainer' + id);
    var jCont = xj(container); 
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
    var jCont = xj(container);
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
        setTimeout("eXo.webui.UIUploadInput.refreshProgress('" + id + "');",
            this.refreshTime);
      } else {
        this.listUpload.push(id);
      }

      var UploadInput = jCont.find('#UploadInput' + id);
      UploadInput.hide();
    }
  },

  upload : function(id) {
    setTimeout("eXo.webui.UIUploadInput.doUpload('" + id + "')", this.delayTime);
  }
}
