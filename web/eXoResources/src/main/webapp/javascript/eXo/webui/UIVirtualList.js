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

var virtualList = {

  init : function(componentId, hasNext, autoAdjustHeight, loadNextUrl) {
    var uiVirtualList = document.getElementById(componentId);
    if (uiVirtualList == null)
      return;

    $(uiVirtualList).on("scroll", function() {
    	_module.UIVirtualList.onScroll(this, loadNextUrl);
    });
    
    if (!hasNext) {
      uiVirtualList.isFinished = true;
    }

    var virtualHeight = $(uiVirtualList).height();

    if (virtualHeight == 0) {
      virtualHeight = 300;
    }

    uiVirtualList.style.height = virtualHeight + "px";

    if (autoAdjustHeight) {
      uiVirtualList.autoAdjustHeight = autoAdjustHeight;
      base.Browser.fillUpFreeSpace(uiVirtualList);
    }

    uiVirtualList.scrollTop = 0;

    this.loadIfNeeded(uiVirtualList);
  },

  loadIfNeeded : function(uiVirtualList) {
    if (uiVirtualList.clientHeight == uiVirtualList.scrollHeight) {
      if (uiVirtualList.isFinished) {
        if (uiVirtualList.autoAdjustHeight) {
          uiVirtualList.style.height = "auto";
        }
      } else {
        $(uiVirtualList).trigger("scroll");
      }
    }
  },

  getFeedBox : function(componentId) {
    var uiVirtualList = $("#" + componentId);
    var feedBox = uiVirtualList.find("div.FeedBox");
    if(!feedBox || feedBox.length < 1)
    {
      feedBox = uiVirtualList.find("tbody.FeedBox");
    }
    return feedBox[0];
  },

  onScroll : function(uiVirtualList, url) {
    if (uiVirtualList.isFinished || uiVirtualList.inProgress)
      return;
    var componentHeight = uiVirtualList.offsetHeight;
    var scrollPosition = uiVirtualList.scrollTop;
    var scrollerHeight = uiVirtualList.scrollHeight;
    var scrollable_gap = scrollerHeight - (scrollPosition + componentHeight);
    // if scrollbar reaches bottom
    if (scrollable_gap <= 1) {
      var feedBox = this.getFeedBox(uiVirtualList.id);
      var html = feedBox.innerHTML;
      uiVirtualList.backupHTML = html;
      uiVirtualList.inProgress = true;
      ajaxGet(url);
    }
  },

  updateList : function(componentId, hasNext) {
    var uiVirtualList = document.getElementById(componentId);
    if (uiVirtualList == null)
      return;

    if (!hasNext) {
      uiVirtualList.isFinished = true;
    }

    var feedBox = this.getFeedBox(uiVirtualList.id);
    var loadedContent = uiVirtualList.backupHTML;

    if (!base.Browser.isIE()) {
      feedBox.innerHTML = loadedContent + feedBox.innerHTML;
    } else {
      var index = uiVirtualList.innerHTML.indexOf(feedBox.className);
      index = uiVirtualList.innerHTML.indexOf(">", index) + 1;
      var firstSec = uiVirtualList.innerHTML.substring(0, index);
      var secondSec = uiVirtualList.innerHTML.substring(index);
      uiVirtualList.innerHTML = firstSec + loadedContent + secondSec;
    }
    uiVirtualList.inProgress = false;

    this.loadIfNeeded(uiVirtualList);
  }
};

_module.UIVirtualList = virtualList;