/**
 * Copyright (C) 2011 eXo Platform SAS.
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
eXo.core.AsyncLoader = {
  env : {
	  async : document.createElement('script').async === true,
	  webkit : /AppleWebKit\//.test(navigator.userAgent),
	  gecko : /Gecko\//.test(navigator.userAgent),
	  ie : /MSIE/.test(navigator.userAgent)
  },
  head : document.head || document.getElementsByTagName('head')[0],
  registered : [],
  loaded : [],
  
  loadJS : function(urls, callback, params, context) {		  
	if (!urls || !urls.length) return;
	urls = typeof urls === 'string' ? [urls] : urls;	
		
	var reg = new this.JSReg(urls, new this.CallbackItem(callback, params, context));
	this.registered.push(reg);
	reg.load();		
  },    
 
  JSReg : function(urls, callback) {
    this.urls = urls;		  
	this.callback = callback;
	this.pending = [];
	for (var i = 0; i < urls.length; i++) {
		if (!eXo.core.AsyncLoader.isRegistered(urls[i])) {
			this.pending.push(urls[i]);
		}
	}
	this.load = function() {
		var nodes = [], loader = eXo.core.AsyncLoader;				
		
		if (!this.pending.length) {
			this.finish();
			return;
		}
		if (loader.env.async) {
			for (var i = 0; i < this.pending.length; i++) {
				var node = this.createNode(this.pending[i], loader.env);
				nodes.push(node);
			}
		} else {						
			nodes.push(this.createNode(this.pending[0], loader.env));				
		}        

        for (i = 0; i < nodes.length; ++i) {
          loader.head.appendChild(nodes[i]);
        }
	};
	this.finish = function(url) {		
		var loader = eXo.core.AsyncLoader;
		
		if (this.pending.length && url) {		
			this.pending.splice(loader.indexOf(this.pending, url), 1);
			loader.loaded.push(url);					
		}		
		if (this.pending.length == 0) {
			for (var i = 0; i < this.urls.length; i++) {
				if (!loader.isLoaded(this.urls[i])) {					
					var me = this;
					setTimeout(function() {me.finish();}, 250);
					return;
				}
			}			
			this.callback.invoke();
		}
		if (!loader.env.async && this.pending.length > 0) {
			this.load();							
		} 
	};
	this.createNode = function(url, env) {
		var  me = this, node = document.createElement("script");
		node.src = url;
		node.async = false;
		if (env.ie) {
    		node.onreadystatechange = function () {
    			if (/loaded|complete/.test(node.readyState)) {
    				node.onreadystatechange = null;
    				me.finish(url);
    			}
    		};          
    	} else {
    		node.onload = node.onerror = function() {me.finish(url);};
    	}		
		return node;
	};
  },
	
  isRegistered : function(url) {
	for (var i = 0; i < this.registered.length; i++) {
		for (j = 0; j < this.registered[i].urls.length; j++) {
			if (url === this.registered[i].urls[j]) return true;
		}
	}
	return false;
  },
  
  isLoaded : function(url) {
	for (var i = 0; i < this.loaded.length; i++) {
		if (url === this.loaded[i]) return true;
	}
	return false;
  },
  
  indexOf : function(array, obj) {
    for (var i = 0; i < array.length; i++) {
    	if (obj === array[i]) return i;
	}
	return -1;
  },
  
  CallbackItem : function(_callback, _params, _context) {
    this.callback = _callback;
    this.context = _context;
    this.params = _params;
    this.invoke = function() {
	  if (!this.callback) return;
  	  var ctx = this.context ? this.context : {};
  	  if(this.params && typeof(this.params) != "string" && this.params.length) this.callback.apply(ctx, this.params);
  	  else this.callback.call(ctx, this.params) ;
    };
  }
};