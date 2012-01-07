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
  PENDING : "pending",
  LOADED : "loaded",
  JS : [],  
  
  loadJS : function(scripts, callback, params, context) {		  
	if (!scripts) return;	
	scripts = typeof scripts === "string" || scripts.length == undefined ? [scripts] : scripts;
		
	var tmp = [];
	for (var i = 0; i < scripts.length; i++) {
		if (scripts[i]) {
			//filter undefined
			tmp.push(scripts[i]);
			//filter duplicated
			if (!this.getScript(scripts[i]))
				this.JS.push({script: scripts[i], status: ""});
		}
	}	
	var reg = new this.JSReg(tmp, new this.CallbackItem(callback, params, context));
	reg.load();		
  },    
 
  JSReg : function(scripts, callback) {
	var loader = eXo.core.AsyncLoader;
	this.scripts = scripts;
    this.pending = [];
    for (var i = 0; i < this.scripts.length; i++) {
    	if (!loader.env.async || loader.getScript(this.scripts[i]).status === "") {
    		this.pending.push(this.scripts[i]);
    	}
    }
	this.callback = callback;	
	this.load = function() {
		var nodes = [];				
		
		if (this.pending.length) {
			var len = loader.env.async ? this.pending.length : 1;
			for (var i = 0; i < len; i++) {
				var registered = loader.getScript(this.pending[i]); 
				if (registered.status === "") {
					registered.status = loader.PENDING;
					nodes.push(this.createNode(this.pending[i]));
				} else if (registered.status === loader.PENDING && !loader.env.async) {
					//Sync mode: wait to make sure the order of scripts
					setTimeout(function(reg) { return function() {reg.load();};}(this), 250);
					return;						
				}						
			}

	        for (i = 0; i < nodes.length; ++i) {
	          var node = nodes[i];
	          loader.head.appendChild(node);
	          if (!node.src) {
	        	  this.finish(node);
	          }
	        }
		}
		if (!nodes.length) this.finish();
	};
	this.finish = function(script) {		
		if (script) {
			loader.getScript(script).status = loader.LOADED;								
		}
		
		if (this.pending.length) {
			var tmp = this.pending.slice(0);
			for (var i = 0; i < tmp.length; i++) {
				if (loader.getScript(tmp[i]).status === loader.LOADED) {
					this.pending.splice(loader.indexOf(this.pending, tmp[i]), 1);						
				}
			}					
		}
		if (this.pending.length == 0) {
			for (var i = 0; i < this.scripts.length; i++) {
				if (loader.getScript(this.scripts[i]).status === loader.PENDING) {					
					setTimeout(function(reg) { return function() {reg.finish();};}(this), 250);
					return;
				}
			}			
			this.callback.invoke();
		}
		if (!loader.env.async && this.pending.length > 0) {
			this.load();							
		} 
	};
	this.createNode = function(script) {		
		var node = script, me = this;
		if (typeof script === "string") {
			node = document.createElement("script");
			node.src = script;			
		}
		node.type = "text/javascript";
		node.async = false;
		if (node.src) {
			if (loader.env.ie) {
				node.onreadystatechange = function () {
					if (/loaded|complete/.test(node.readyState)) {
						node.onreadystatechange = null;
						me.finish(script);
					}
				};          
			} else {
				node.onload = node.onerror = function() {me.finish(script);};
			}								
		}
		return node;
	};
  },	  
  
  getScript : function(script) {
	  for (var i = 0; i < this.JS.length; i++) {
		if (script === this.JS[i].script) return this.JS[i];
	  }
	  return null;   
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