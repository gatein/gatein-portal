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

  // The Application model presents a component (window) in the layout which is be able to drag & drop
  var Application = LayoutComponent.extend({
    initialize : function(options) {
      LayoutComponent.prototype.initialize.call(this);

      var options = options || {};
      this.set({
        draggable : true,
        name : options.name || '',
        applicationName : options.applicationName || '',
        title : options.title || '',
        content: options.content || "loading....",
        logo : '/portal/assets/org/gatein/portal/web/assets/images/DefaultPortlet.png'
      });
      if(options.id) {
        this.set('id', options.id);
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

  // The Container model presents a Zone in the layout which contains the Application
  var Container = LayoutComponent.extend({
    initialize : function() {
      LayoutComponent.prototype.initialize.call(this);
      this.set({
        draggable : true,
        droppable : true,

        // Should not access directly to those internal attributes
        _childrens : new Backbone.Collection()
      });

      var _this = this;
      this.get('_childrens').on('add', function(child) {
        _this.trigger('addChild.eXo.Container', child, _this);
      });
      this.get('_childrens').on('remove', function(child) {
        _this.trigger('removeChild.eXo.Container', child, _this);
      });
    },

    isAllowDropping : function(dragObj) {
      // Check for supported types
      if (dragObj && (dragObj.constructor == Container || dragObj.constructor == Application)) {
        return this.get('droppable');
      } else {
        return false;
      }
    },

    /*
     * methods on childrens
     */
    addChild : function(child, options) {
      child = typeof child == 'string' ? this.getRoot().getDescendant(child) : child;

      if (child && this.isAllowDropping(child)) {
        var _this = this;

        this.listenTo(child, 'addChild.eXo.Container', function(addedChild, container) {
          _this.trigger('addChild.eXo.Container', addedChild, container);
        });
        this.listenTo(child, 'removeChild.eXo.Container', function(addedChild, container) {
          _this.trigger('removeChild.eXo.Container', addedChild, container);
        });

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
        this.get('_childrens').remove(child, {
          silent : true
        });
        this.get('_childrens').add(child, {
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
        this.get('_childrens').remove(child, {
          silent : options.silent
        });
        child.set('_parent', null);
        this.stopListening(child);
      }
      return this;
    },
    isEmpty : function() {
      return this.get('_childrens').isEmpty();
    },
    getChildrens : function() {
      return this.get('_childrens').toArray();
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
            return elem.getDescendant.call(elem, id);
          }
        });
        cont != null && (child = cont.getChild(id));
      }
      return child;
    },

    //
    set : function(data, options) {
      var options = options || {};
      var merge = !(options.merge === false);
      var add = !(options.add === false);
      var remove = !(options.remove === false);

      var childrens = data.childrens;
      delete data.childrens;

      Backbone.Model.prototype.set.call(this, data, options);

      var _this = this;
      if (childrens) {
        $.each(childrens, function(idx, elem) {
          var tmp;
          if (tmp = _this.getChild(elem.id)) {
            merge && tmp.set(elem, options);
          } else {
            tmp = elem.childrens ? new Container() : new Application();
            tmp.set(elem);

            options.at = idx;
            add && _this.addChild(tmp, options);
          }
        });

        if (remove && this.get('_childrens')) {
          var tmp = this.get('_childrens').filter(function(elem) {
            var found = false;
            $.each(childrens, function(idx, child) {
              return !(found = child.id == elem.getId());
            });
            return !found;
          });

          $.each(tmp, function(idx, elem) {
            _this.removeChild(elem.id, options);
          });
        }
      }
    },

    // Return the JSON object that contains metadata information
    toJSON : function() {
      var data = {
        id : this.getId(),
        type : "container",
        childrens : []
      };
      this.get('_childrens').each(function(elem) {
        data.childrens.push(elem.toJSON());
      });
      return data;
    },

    //
    switchLayout : function(newContainer) {
      $(this.get("_childrens").models).each(function() {
        var apps = this.get("_childrens").models;
        var id = this.id;
        var cont = this;
        $(apps).each(function() {
          var newCont = newContainer.getChild(id);
          var newApp = this.clone();
          if (newCont) {
            newCont.addChild(newApp);
          } else {
            // Add applications into last container
            var lastId = newContainer.get("_childrens").length;
            newCont = newContainer.getChild(lastId);
            newCont.addChild(newApp);
          }
        });
      });
    }
  });

  // 
  var PageContainer = Container.extend({
    initialize : function() {
      Container.prototype.initialize.call(this);

      this.set({
        layout_id : ''
      });
    },

    setLayoutId : function(layoutId) {
      this.set('layout_id', layoutId);
    },
    getLayoutId : function() {
      return this.get('layout_id');
    },

    // Return the JSON object that contains metadata information
    toJSON : function() {
      var data = {
        id : this.getId(),
        layout_id : this.getLayoutId(),
        type : 'container',
        childrens : []
      };
      this.get('_childrens').each(function(elem) {
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
      Container.prototype.initialize.call(this);
    },
    findChildByName : function(name) {
      return this.get('_childrens').where({name: name});
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