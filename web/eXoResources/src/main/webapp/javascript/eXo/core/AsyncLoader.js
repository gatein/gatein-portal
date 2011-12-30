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
  jsPending : -1,
  registered : [],
  loaded : [],
  
  loadJS : function(urls, callback, params, context) {		  
	if (!urls || !urls.length) return;
	urls = typeof urls === 'string' ? [urls] : urls;	
		
	var reg = new this.Registration(urls, new this.CallbackItem(callback, params, context), this.registered.length);
	this.registered.push(reg);
	reg.load();		
  },    
 
  Registration : function(urls, callback, index) {
    this.urls = urls;		  
	this.callback = callback;
	this.index = index;
	this.pending = [];
	for (var i = 0; i < urls.length; i++) {
		if (!eXo.core.AsyncLoader.isRegistered(urls[i])) {
			this.pending.push(urls[i]);
		}
	}
	this.load = function() {
		var loader = eXo.core.AsyncLoader, nodes = [];				

		
		if (!loader.env.async) {
			if (loader.jsPending != -1) return;
			loader.jsPending = this.index;
			if (!this.pending.length) {
				this.finish(this);
			} else {
				nodes.push(this.createNode(this.pending[0], loader.env));				
			}			
		} else {
			if (!this.pending.length) {
				this.finish(this);
			} else {
				for (var i = 0; i < this.pending.length; i++) {
					var node = this.createNode(this.pending[i], loader.env);
					nodes.push(node);
				}
			}
		}        

        for (i = 0; i < nodes.length; ++i) {
          loader.head.appendChild(nodes[i]);
        }
	};
	this.finish = function(reg) {		
		var loader = eXo.core.AsyncLoader;
		
		if (reg.pending.length) {
			loader.loaded.push(reg.pending.shift());					
		}
		if (reg.pending.length == 0) {
			for (var i = 0; i < reg.urls.length; i++) {
				if (!loader.isLoaded(reg.urls[i])) {
					setTimeout(function() {reg.finish(reg);}, 250);
					return;
				}
			}
			reg.callback.invoke();
		}
		if (!loader.env.async) {
			loader.jsPending = -1;
			if (reg.pending.length > 0) {
				loader.head.appendChild(reg.createNode(reg.pending[0], loader.env));							
			} else if (reg.index < loader.registered.length - 1) {
				loader.registered[reg.index + 1].load();
			}
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
    				me.finish(me);
    			}
    		};          
    	} else {
    		node.onload = node.onerror = function() {me.finish(me);};
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