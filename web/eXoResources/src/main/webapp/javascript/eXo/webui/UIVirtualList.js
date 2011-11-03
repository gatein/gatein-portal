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

function UIVirtualList() {}

UIVirtualList.prototype.init = function(componentId, hasNext, autoAdjustHeight) {
  var uiVirtualList = document.getElementById(componentId);
  if (uiVirtualList == null) return;  
  
  if (!hasNext)
  {
    uiVirtualList.isFinished = true;
  }
  
  var virtualHeight = eXo.core.DOMUtil.getStyle(uiVirtualList, 'height', true);
  
  if (virtualHeight == 0)
  {
    virtualHeight = 300;
  }

  uiVirtualList.style.height = virtualHeight + "px";

  if (autoAdjustHeight)
  {
    uiVirtualList.autoAdjustHeight = autoAdjustHeight;
    eXo.core.Browser.fillUpFreeSpace(uiVirtualList);
  }

  uiVirtualList.scrollTop = 0;
  
  this.loadIfNeeded(uiVirtualList);
}

UIVirtualList.prototype.loadIfNeeded = function(uiVirtualList)
{
  if (uiVirtualList.clientHeight == uiVirtualList.scrollHeight)
  {
    if (uiVirtualList.isFinished)
    {
      if (uiVirtualList.autoAdjustHeight)
      {
        uiVirtualList.style.height = "auto";
      }
    }
    else
    {
      uiVirtualList.onscroll();
    }
  }
}

UIVirtualList.prototype.getFeedBox = function(componentId) {
  var DOMUtil = eXo.core.DOMUtil;
  var uiVirtualList = document.getElementById(componentId);
  var feedBox = DOMUtil.findFirstDescendantByClass(uiVirtualList, "div","FeedBox");
  if (feedBox == null) {
    feedBox = DOMUtil.findFirstDescendantByClass(uiVirtualList, "tbody","FeedBox");
  } 
  return feedBox;
}

UIVirtualList.prototype.onScroll = function(uiVirtualList, url) {
  if (uiVirtualList.isFinished || uiVirtualList.inProgress) return;
  var DOMUtil = eXo.core.DOMUtil; 
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
}

UIVirtualList.prototype.updateList = function(componentId, hasNext) {
  var DOMUtil = eXo.core.DOMUtil;
  var uiVirtualList = document.getElementById(componentId);
  if (uiVirtualList == null) return;
 
  if (!hasNext)
  {
    uiVirtualList.isFinished = true;
  }

  var feedBox = this.getFeedBox(uiVirtualList.id);
  var loadedContent = uiVirtualList.backupHTML;
  
  if (eXo.core.Browser.browserType != "ie") {
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

eXo.webui.UIVirtualList = new UIVirtualList();
