(function() {

  // An abstract model for Application and Container (Zone) in a layout
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
    getId : function() {
      return this.get('id') || this.cid;
    },

    // Return the parent object
    getParent : function() {
      return this.get('_parent');
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

  // The Application model presents a component (window) in the layout which is be able to drag & drop
  var Application = LayoutComponent.extend({
    initialize : function(attributes) {
      LayoutComponent.prototype.initialize.apply(this, arguments);

      var attributes = attributes || {};
      this.set({
        draggable : true,
        name : attributes.name || '',
        applicationName : attributes.applicationName || '',
        title : attributes.title || '',
        content: attributes.content || "loading....",
        logo : '/portal/assets/org/gatein/portal/web/assets/images/DefaultPortlet.png'
      });
      if(attributes.id) {
        this.set('id', attributes.id);
      }
    },

    setId: function(id) {
      this.set('id', id);
    },
    getName : function() {
      return this.get('name');
    },
    getApplicationName: function() {
      return this.get("applicationName");
    },
    getTitle: function() {
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
      $.ajax({
        url : url,
        dataType : "json",
        success: function(result) {
          _this.setContent(result.content);
        }
      });
    },
    clone: function() {
      return new Application({
        id: this.getId(),
        name : this.getName(),
        applicationName : this.getApplicationName(),
        title : this.getTitle(),
        logo : this.getLogo(),
        content: this.getContent()
      });
    },
    // Return the JSON object that contains metadata information
    toJSON : function() {
      return {
        id : this.getId(),
        type : "application",
        name : this.getName(),
        applicationName: this.getApplicationName()
      };
    },

    // Return a JSON object for rendering phase
    // TODO: should it be merged with #toJSON() method ?
    toJSONForRenderer : function() {
      return {
        id : this.getId(),
        type : "application",
        name : this.getName(),
        title : this.getTitle(),
        content : this.getContent(),
        logo : this.getLogo()
      };
    }
  });

  // The abstract model of a container
  // In a container, we have special/reserved attributes following:
  // 'children' : is a Backbone.Collection which contains its children
  var AbstractContainer = LayoutComponent.extend({
    initialize : function() {
      LayoutComponent.prototype.initialize.apply(this, arguments);

      this.set({
        draggable : true,
        droppable : true,
        // Should not access directly to those internal attributes
        // TODO: The children collection should contain a specific model
        // and it would be able to pass collection object at initializing
        _children: new Backbone.Collection()
      });

      this.get('_children').on('add', function(child) {
        this.trigger('container.addChild', child, this);
      }, this);

      this.get('_children').on('remove', function(child) {
        this.trigger('container.removeChild', child, this);
      }, this);
    },

    getDescendant : function(id) {
      var child = this.getChild(id);
      if (!child) {
        var cont = this.get('_children').find(function(elem) {
          if ($.isFunction(elem.getDescendant)) {
            return elem.getDescendant.call(elem, id);
          }
        });
        cont != null && (child = cont.getChild(id));
      }
      return child;
    },

    /*
     * methods on childrens
     */
    addChild : function(child, options) {
      child = typeof child == 'string' ? this.getRoot().getDescendant(child) : child;

      if (child && this.isAllowDropping(child)) {
        var _this = this;
        options = options || {};
        var oldParent = child.getParent();
        if (oldParent && oldParent.getId() != this.getId()) {
          oldParent.removeChild(child, {
            silent : options.silent
          });
        }
        child.set('_parent', this);
        // collection in backbone ignore move action in same container
        // need to remove then re-add
        this.get('_children').remove(child, {
          silent : true
        });
        this.get('_children').add(child, {
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
        this.get('_children').remove(child, {
          silent : options.silent
        });
        child.set('_parent', null);
      }
      return this;
    },

    isEmpty : function() {
      return this.get('_children').isEmpty();
    },
    getChildren : function() {
      return this.get('_children').toArray();
    },
    getChild : function(id) {
      return this.get('_children').get(id);
    },
    indexOf : function(child) {
      child = typeof child == 'string' ? this.getChild(child) : child;
      return this.get('_children').indexOf(child);
    },
    at : function(idx) {
      return this.get('_children').at(idx);
    }
  });

  // The Container model presents a Zone in the layout which contains the Application
  var Container = AbstractContainer.extend({
    initialize : function() {
      AbstractContainer.prototype.initialize.apply(this, arguments);
    },

    isAllowDropping : function(dragObj) {
      // Check for supported types
      // TODO: Instead of hardcoding "Application". The type should be checked from children collection's model
      if (dragObj && dragObj.constructor == Application) {
        return this.get('droppable');
      } else {
        return false;
      }
    },
    
    // Return the JSON object that contains metadata information
    toJSON : function() {
      var data = {
        id : this.getId(),
        type : "container",
        childrens : []
      };
      this.get('_children').each(function(elem) {
        data.childrens.push(elem.toJSON());
      });
      return data;
    }
  });

  // 
  var PageContainer = AbstractContainer.extend({
    initialize : function() {
      AbstractContainer.prototype.initialize.apply(this, arguments);
      this.set({
        layout_id : ''
      });
    },

    isAllowDropping : function(dragObj) {
      // Check for supported types
      // TODO: Instead of hardcoding "Container". The type may be checked from children collection's model
      if (dragObj && dragObj.constructor == Container) {
        return this.get('droppable');
      } else {
        return false;
      }
    },

    setLayoutId : function(layoutId) {
      this.set('layout_id', layoutId);
    },

    getLayoutId : function() {
      return this.get('layout_id');
    },

    switchLayout : function(newContainer) {
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
      $.each(conts, function() {
        var apps = this.getChildren();
        var id = this.id;
        $(apps).each(function() {
          var newCont = newContainer.getChild(id);
          var newApp = this.clone();
          if (newCont) {
            newCont.addChild(newApp);
          } else {
            // Add applications into last container
            var lastId = newContainer.get("_children").length;
            newCont = newContainer.getChild(lastId);
            newCont.addChild(newApp);
          }
        });
      });
    },

    // Return the JSON object that contains metadata information
    toJSON : function() {
      var data = {
        id : this.getId(),
        layout_id : this.getLayoutId(),
        type : 'pagecontainer',
        childrens : []
      };
      this.get('_children').each(function(elem) {
        data.childrens.push(elem.toJSON());
      });
      return data;
    }
  });

  /**
   * 
   */
  var ComposerContainer = Container.extend({
    initialize : function() {
      Container.prototype.initialize.apply(this, arguments);
    },
    findChildByName : function(name) {
      return this.get('_children').where({name: name});
    },

    fetch : function() {
      var _this = this;
      $.ajax({
        url : this.url,
        dataType : "json",
        success : function(result) {
          if (result.code != 200) {
            alert("error on fetch portlets");
            return;
          }

          var portlets = result.data.portlets;
          $(portlets).each(function(i, portlet) {
            _this.addChild(new Application({
              name : portlet.name,
              applicationName: portlet.applicationName,
              title: portlet.title
            }));
          });
        }
      });
    }
  });

  var layoutDef = {
    'LayoutComponent' : LayoutComponent,
    'Application' : Application,
    'Container' : Container,
    'PageContainer' : PageContainer,
    'ComposerContainer' : ComposerContainer
  };
  if (typeof window.require === "function" && window.require.amd) {
    return layoutDef;
  } else {
    return $.extend(window, layoutDef);
  }
})();