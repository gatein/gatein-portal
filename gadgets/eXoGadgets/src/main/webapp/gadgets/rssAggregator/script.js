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

function RssAggregator() {
  this.feed = {};
}

RssAggregator.prototype.getFavicon = function(feedurl) {
    var favicon = feedurl.match( /:\/\/(www\.)?([^\/:]+)/ );
    favicon = favicon[2]?favicon[2]:'';
    favicon = "http://"+favicon+"/favicon.ico";
    return favicon;
}

RssAggregator.prototype.toggleDescription = function(elmnt_id) {
    if (_gel('more_'+elmnt_id).style.display == 'none') {
        _gel('more_'+elmnt_id).style.display = '';
        _gel('item_'+elmnt_id).className = 'item descriptionHighlight';
    } else {
        _gel('more_'+elmnt_id).style.display = 'none';
        _gel('item_'+elmnt_id).className = 'item';
    }
    gadgets.window.adjustHeight();
}

RssAggregator.prototype.timeToPrettyString = function(B) {
    if (isNaN(B)) {
        return "an indeterminate amount of time ago"
    }
    time = (new Date().getTime()*1000 - B) / 1000;
    return (new Date(B).toLocaleString());
}

RssAggregator.prototype.renderFeed = function(feedObj) {
  if(feedObj.rc != 200 && feedObj.data == undefined) {
    document.write("the url: " + feedurl + " is down or invalid");
    return;
  }
    this.feed = feedObj.data;
    gadgets.window.setTitle("RSS: " + this.feed.Title);
    var feedEl = _gel("feedContainer");
	var bullet = "<img src='" + this.getFavicon(feedurl) + "' alt='' border=0 align='absmiddle' style='height:16;width:16;' onerror='this.style.visibility=\"hidden\";'>&nbsp;&nbsp;";

    if (this.feed != null) {
        // Access the data for a given entry
        if (this.feed.Entry) {
            for (var i = 0; i < this.feed.Entry.length; i++) {
                var itemEl = document.createElement('div');
                var item_title = document.createElement('div');
                var item_more = document.createElement('div');
                var item_desc = document.createElement('div');
                var item_date = document.createElement('div');
                var item_link = document.createElement('div');

                itemEl.id = 'item_'+i;
                item_title.id = 'title_'+i;
                item_more.id = 'more_'+i;
                item_more.style.display='none';
                item_desc.id = 'desc_'+i;
                item_date.id = 'date_'+i;
                item_link.id = 'link_'+i;


				itemEl.className = 'item';
                item_title.className = 'title';
                item_more.className = 'more';
                item_desc.className = 'desc';
                item_date.className = 'date';
                item_link.className = 'link';

                item_title.innerHTML = bullet + "<a id='link_title_"+i+"' class='titlelink' href='" + this.feed.Entry[i].Link + "' onclick='rssAggregator.toggleDescription("+i+");return false;'>" + this.feed.Entry[i].Title + "</a>";
				item_date.innerHTML = this.timeToPrettyString(this.feed.Entry[i].Date);

				item_desc.innerHTML = this.feed.Entry[i].Summary;

                item_link.innerHTML = this.generateLinkContent(i);


                item_more.appendChild(item_date);
                item_more.appendChild(item_desc);
                item_more.appendChild(item_link);


                itemEl.appendChild(item_title);
                itemEl.appendChild(item_more);

                feedEl.appendChild(itemEl);
            }
		}
    } else {
        document.write("No feed found at " + feedurl);
    }
    gadgets.window.adjustHeight();
}

RssAggregator.prototype.generateLinkContent = function(i) {
  return "<a href='" + this.feed.Entry[i].Link + "' target='_blank'>view link &raquo;</a>";
}

RssAggregator.prototype.refreshFeed = function() {
  var params = {};  
  params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.FEED;  
  params[gadgets.io.RequestParameters.NUM_ENTRIES] = entries;
  params[gadgets.io.RequestParameters.GET_SUMMARIES] = true; 
  gadgets.io.makeRequest(prefs.getString("rssurl"), function(feedObj) {rssAggregator.renderFeed(feedObj);}, params);
}

