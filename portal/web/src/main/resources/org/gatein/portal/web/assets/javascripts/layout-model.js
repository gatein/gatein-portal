(function() {

  // An abstract model for Application and Container (Zone) in a layout
  var LayoutComponent = Backbone.Model.extend({

    //Return available model's ID
    //if it's transient, return cid
    getId : function() {
      return this.id || this.cid;
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
      type : 'application',
      draggable : true,

      title: '',
      contentId: '',
      contentType: '',
      description: '',
      content : 'Loading the application...'
    },

    // Fetch application content and update the 'content' attribute in the success callback
    // TODO: It should be moved to View component, specifically in render view
    fetchContent: function(pagePath) {
      //TODO: fetchContentURL should be set on init
      //var url = "/portal/getContent?javax.portlet.contentId=" + this.get('contentId') + "&javax.portlet.contentType=" + this.get('contentType') + "&javax.portlet.path=" + pagePath;
      var url = '/portal/window/' + this.get('contentType') + '/' + this.get('contentId') + '?javax.portlet.url='+ encodeURIComponent(window.location.href);

      //Delegate to Model#fetch
      this.fetch({url: url});
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

      if (!options || !options.model) {
        this._children = new Backbone.Collection();
      } else if (options.model){
        this._children = new Backbone.Collection([], {model : options.model});
      }
      
      //TODO: it seems to me that using listenTo makes this code more clear
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
      if (dragObj && dragObj instanceof this._children.model) {
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

    // TODO: Add documentation comment for this method
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

      // Serialize its children collection
      // then bind it to the returned JSON object
      data.children = this._children.toJSON();
      return data;
    }
  });

  var ComposerTab = Container.extend({
    defaults : {
      type : 'container',
      droppable : false,

      displayName: '',
      tagName: '',
      value: ''
    },

    initialize : function(attributes, options) {
      Container.prototype.initialize.apply(this, arguments);
      if(attributes.contents) {
        this._children.reset(attributes.contents);
      }
    },

    //When fetch data from server method #set() is always called before #initialize()
    set: function(data, options) {
      var tmp = data.contents;
      delete data.contents;
      //_children is not an attribute
      //it'll be set on initialize method, not the set method
      Container.prototype.set.apply(this, arguments);
      data.contents = tmp;
    },
    findByContentId: function(contentId) {
      return this._children.findWhere({contentId: contentId});
    }
  });
  
  var Composer = Container.extend({
    defaults: {
      filterValue: ''
    },
    
    initialize: function(attributes, options) {
      Container.prototype.initialize.apply(this, arguments);

      this._children.url = options.urlRoot;
      this.listenTo(this._children, 'sync', (function(composer) {
        return function() {composer.trigger('sync', this, arguments)}
      })(this));
    },
    
    fetch: function(options) {
      this._children.fetch.apply(this._children, arguments);
    },
    
    toJSON: function() {
      return this._children.toJSON();
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
    
    initialize : function(attributes, options) {
      Container.prototype.initialize.apply(this, arguments);
      this.snapshot = this._children;
    },

    isDroppable : function(dragObj) {
      if (dragObj && dragObj instanceof this._children.model) {
        return this.get('droppable');
      } else {
        return false;
      }
    },

    /** 
      * The method represents to change structure of tree children by a layout metadata. 
      * The layout metadata is an object javascript which contains "id", "containers" attributes.
      * The "id" attribute is "factoryId" of new layout which need to switch
      * The "containers" is array what is set of zone id of layout
      *
      * @layout: is a metadata object pass from view
      */
    switchLayout : function(layout) {
      //Restore container children by snapshot
      this._children = this.snapshot;
      
      //Sort containers by id
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
      
      //Create a set of container by layout metadata
      var newContainers = new Backbone.Collection(null, {model : Container});
      $(layout.containers).each(function() {
        var containerId = this[0];
        var container = new Container({
          id : containerId
        });
        container._parent = pageLayout;
        newContainers.add(container);
      });

      //Add current applications into new containers
      $.each(conts, function() {
        var container = newContainers.get(this.id);
        var apps = this.getChildren();
        $(apps).each(function() {
          var app = this.clone();
          if (container) {
            container.addChild(app);
          } else {
            // Add applications into last container
            var lastId = newContainers.length;
            container = newContainers.get(lastId);
            container.addChild(app);
          }
        });
      });
      
      //Update page's children container
      this._children = newContainers;
    },
    
    updateSnapshot: function() {
      this.snapshot = this._children;
    }
  });

  var layoutDef = {
    'LayoutComponent' : LayoutComponent,
    'Application' : Application,
    'Container' : Container,
    'ComposerTab': ComposerTab,
    'Composer': Composer,
    'PageLayout' : PageLayout
  };
  if (typeof window.require === "function" && window.require.amd) {
    return layoutDef;
  } else {
    return $.extend(window, layoutDef);
  }
})();