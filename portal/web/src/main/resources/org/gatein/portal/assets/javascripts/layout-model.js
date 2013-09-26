(function() {
	/**
	 * 
	 */
	var LayoutComponent = Backbone.Model.extend({
		initialize : function() {
			this.set({
				_parent : null,
				draggable : false,
				droppable : false
			});
		},
		isAllowDropping : function(dragObj) {
			return false;
		},
		getType : function() {
			return 'LayoutComponent';
		},
		getId : function() {
			return this.get('id') || this.cid;
		},
		getParent : function() {
			return this.get('_parent');
		},
		getRoot : function() {
			var parent = this, root;
			do {
				root = parent;
				parent = parent.getParent();
			} while (parent);			
			return root;
		},
		getIndex : function() {
			if (this.getParent()) {
				return this.getParent().indexOf(this);
			} else {
				return -1;
			}
		}
	});

	/**
	 * 
	 */
	var Application = LayoutComponent.extend({
		initialize : function() {
			LayoutComponent.prototype.initialize.call(this);
			this.set({
				draggable : true
			});
		},
		getType : function() {
			return 'Application';
		}
	});

	/**
	 * 
	 */
	var Container = LayoutComponent.extend({
		initialize : function() {
			LayoutComponent.prototype.initialize.call(this);
			this.set({
				draggable : true,
				droppable : true,

				// Should not access directly to those internal attributes
				_childrens : new Backbone.Collection(),
				// Allow type 'Application' and 'Container'to be dropped
				_childTypes : [ Application.prototype.getType(), 'Container' ]
			});

			var _this = this;
			this.get('_childrens').on('add', function(child) {
				_this.trigger('addChild.eXo.Container', child, _this);
			});
			this.get('_childrens').on('remove', function(child) {
				_this.trigger('removeChild.eXo.Container', child, _this);
			});
		},

		isChildSupported : function(dragObj) {
			// Check for supported types
			if (dragObj && $.isFunction(dragObj.getType)) {
				var type = $.trim(dragObj.getType());
				return this.get('droppable') && $.inArray(type, this.get('_childTypes')) != -1;
			} else {
				return false;
			}
		},
		getType : function() {
			return 'Container';
		},

		/**
		 * methods on childrens
		 */
		//
		addChild : function(child, idx) {
			child = typeof child == 'string' ? this.getChild(child) : child;

			if (child && this.isChildSupported(child)) {
				var _this = this;
				this.listenTo(child, 'addChild.eXo.Container', function(addedChild, container) {
					_this.trigger('addChild.eXo.Container', addedChild, container);
				});
				this.listenTo(child, 'removeChild.eXo.Container', function(addedChild, container) {
					_this.trigger('removeChild.eXo.Container', addedChild, container);
				});

				if (child.getParent() != null) {
					child.getParent().removeChild(child, true);
				}
				child.set('_parent', this);
				this.get('_childrens').add(child, {
					at : idx
				});
			}

			return this;
		},
		//
		removeChild : function(child, silent) {
			child = typeof child == 'string' ? this.getChild(child) : child;

			if (child && child.getParent() === this) {
				child.set('_parent', null);
				this.get('_childrens').remove(child, {'silent' : silent});
				this.stopListening(child);
			}
			return this;
		},
		getChild : function(id) {
			return this.get('_childrens').get(id);
		},
		indexOf : function(child) {
			child = typeof child == 'string' ? this.getChild(child) : child;
			return this.get('_childrens').indexOf(child);
		},
		at : function(idx) {
			return this.get('_childrens').at(idx);
		},
		getDescendant : function(id) {
			var child = this.getChild(id);
			if (!child) {
				var cont = this.get('_childrens').find(function(elem) {
					if ($.isFunction(elem.getDescendant)) {
						return  elem.getDescendant.call(elem, id);
					}
				});
				cont != null && (child = cont.getChild(id));
			}
			return child;
		},

		//
		addChildType : function(childType) {
			var type = $.trim(childType);
			var types = this.get('_childTypes');

			if ($.inArray(type, types) == -1) {
				types.put(type);
				this.trigger('addChildType.eXo.Container', type);
			}
			return this;
		},
		//
		removeChildType : function(childType) {
			var type = $.trim(childType);
			var types = this.get('_childTypes');
			var idx = $.inArray(type, types);

			if (idx != -1) {
				types.splice(idx, 1);
				this.trigger('removeChildType.eXo.Container', type);
			}
			return this;
		}
	});

	var layoutDef = {
		'LayoutComponent' : LayoutComponent,
		'Application' : Application,
		'Container' : Container
	};
	if (typeof window.require === "function" && window.require.amd) {
		return layoutDef;
	} else {
		return $.extend(window, layoutDef);
	}
})();