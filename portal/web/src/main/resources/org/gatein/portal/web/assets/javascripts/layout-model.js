(function() {

  // An abstract model for Application and Container (Zone) in a layout
  var LayoutComponent = Backbone.Model.extend({

    // TODO: we should rely on the Model.id property somehow
    getId : function() {
      return this.get('id') || this.cid;
    },

    // Return the parent object
    getParent : function() {
      return this._parent;
    },

    // Return the root object
    getRoot : function() {
      var parent = this, root;
      do {
        root = parent;
        parent = parent.getParent();
      } while (parent);
      return root;
    },

    // Return the index of the object in its parent
    getIndex : function() {
      if (this.getParent()) {
        return this.getParent().indexOf(this);
      } else {
        return -1;
      }
    }
  });

  /**
   * The Application model presents a component (window) in the layout which is be able to drag & drop
   * The following are built-in/pre-defined attributes:
   * - 'name': The name of application
   * - 'applicationName':
   * - 'title': The application title
   */
  var Application = LayoutComponent.extend({
    defaults : {
      'type' : 'application',
      'draggable' : true,
      'content' : 'Loading the application...',
      'logo' : '/portal/assets/org/gatein/portal/web/assets/images/DefaultPortlet.png'
    },

    // TODO: Why don't we rely on the Model.id property instead ?
    setId: function(id) {
      this.set('id', id);
    },

    getName : function() {
      return this.get('name');
    },
    getApplicationName: function() {
      return this.get("applicationName");
    },
    getTitle : function() {
      return this.get("title");
    },
    getLogo : function() {
      return this.get('logo');
    },
    setContent: function(content) {
      this.set("content", content);
    },
    getContent: function(){
      return this.get("content");
    },

    fetchContent: function() {
      //TODO: need to refactor
      var contentId = this.getApplicationName() + "/" + this.getName();
      var url = "/portal/getContent?javax.portlet.content=" + contentId;
      var _this = this;
      Backbone.sync("read", this, {
    	  url : url,
    	  dataType : "json",
    	  success : function(result) {
    		  _this.setContent(result.content);  
    	  }
      });
    }
  });

  // The Container model presents a Zone in the layout which contains the Application
  // In a container, we have special/reserved attributes following:
  // - 'children' : is a Backbone.Collection which contains its children
  var Container = LayoutComponent.extend({

    defaults : {
      type : 'container',
      droppable : true
    },

    initialize : function(attributes, options) {
      LayoutComponent.prototype.initialize.apply(this, arguments);

      // A Backbone.Collection object which contains its children
      // TODO: The children collection should contain a specific model
      // and it would be able to pass collection object at initializing
      this._children = new Backbone.Collection();

      this._children.on('add', function(child) {
        this.trigger('container.addChild', child, this);
      }, this);

      this._children.on('remove', function(child) {
        this.trigger('container.removeChild', child, this);
      }, this);
    },
    
    /**
     * Return true if the dragObj is allowed to drop into the container.
     * Otherwise, return false
     */
    isDroppable : function(dragObj) {
      // Check for supported types
      // TODO: Instead of hardcoding "Application". The type should be checked from children collection's model
      if (dragObj && dragObj.constructor == Application) {
        return this.get('droppable');
      } else {
        return false;
      }
    },

    getDescendant : function(id) {
      var child = this.getChild(id);
      if (!child) {
        var cont = this._children.find(function(elem) {
          if ($.isFunction(elem.getDescendant)) {
            return elem.getDescendant.call(elem, id);
          }
        });

        // TODO: What is this statement for ?
        cont != null && (child = cont.getChild(id));
      }
      return child;
    },

    /*
     * methods on children
     */
    // TODO: Add documentation comment for this function
    addChild : function(child, options) {
      child = typeof child == 'string' ? this.getRoot().getDescendant(child) : child;

      if (child && this.isDroppable(child)) {
        var _this = this;
        options = options || {};
        var oldParent = child.getParent();
        if (oldParent && oldParent.getId() != this.getId()) {
          oldParent.removeChild(child, {
            silent : options.silent
          });
        }
        child._parent = this;
        // collection in backbone ignore move action in same container
        // need to remove then re-add
        this._children.remove(child, {
          silent : true
        });
        this._children.add(child, {
          at : options.at,
          silent : options.silent
        });
      }

      return this;
    },

    //
    removeChild : function(child, options) {
      child = typeof child == 'string' ? this.getChild(child) : child;

      if (child && child.getParent().getId() === this.getId()) {
        options = options || {};
        this._children.remove(child, {
          silent : options.silent
        });
        child._parent = null;
      }
      return this;
    },

    isEmpty : function() {
      return this._children.isEmpty();
    },
    getChildren : function() {
      return this._children.toArray();
    },
    getChild : function(id) {
      return this._children.get(id);
    },
    indexOf : function(child) {
      child = typeof child == 'string' ? this.getChild(child) : child;
      return this._children.indexOf(child);
    },
    at : function(idx) {
      return this._children.at(idx);
    },

    // Return the JSON object that contains metadata information
    toJSON : function() {
      var data = Backbone.Model.prototype.toJSON.apply(this, arguments);

      // Serialize its children collection to the returned JSON object
      data.children = this._children.toJSON();
      return data;
    }
  });

  // 
  var PageLayout = Container.extend({

    defaults : {
      pageKey: '',
      factoryId: '',
      type : 'layout',
      droppable : true
    },

    isDroppable : function(dragObj) {
      // Check for supported types
      // TODO: Instead of hardcoding "Container". The type may be checked from children collection's model
      if (dragObj && dragObj.constructor == Container) {
        return this.get('droppable');
      } else {
        return false;
      }
    },

    // layout: is a metadata object pass from view
    switchLayout : function(layout) {
      var conts = this.getChildren();
      conts.sort(function(m1, m2) {
        var i1 = parseInt(m1.getId());
        var i2 = parseInt(m2.getId());
        if ($.isNumeric(i1) && $.isNumeric(i2)) {
          return i1 - i2;
        } else {
          return 0;
        }
      });

      var pageLayout = this;
      pageLayout.set('factoryId', layout.id);
      var newContainers = new Backbone.Collection();
      $(layout.containers).each(function() {
        var containerId = this[0];
        var container = new Container({
          id : containerId
        });
        container._parent = pageLayout;
        newContainers.add(container);
      });

      $.each(conts, function() {
        var container = newContainers.get(this.id);
        var apps = this.getChildren();
        $(apps).each(function() {
          if (container) {
            container.addChild(this);
          } else {
            // Add applications into last container
            var lastId = newContainers.length;
            container = newContainers.get(lastId);
            container.addChild(this);
          }
        });
      });
      this._children = newContainers;
    }
  });

  var layoutDef = {
    'LayoutComponent' : LayoutComponent,
    'Application' : Application,
    'Container' : Container,
    'PageLayout' : PageLayout
  };
  if (typeof window.require === "function" && window.require.amd) {
    return layoutDef;
  } else {
    return $.extend(window, layoutDef);
  }
})();