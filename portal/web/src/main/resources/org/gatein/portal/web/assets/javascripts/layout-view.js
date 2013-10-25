(function() {

  // An item in composer
  var ApplicationFactoryView = Backbone.View.extend({
    tagName : "li",

    // TODO: Can this be removed, an empty method ?
    initialize : function() {
    },

    render : function() {
      this.template = _.template($("#portlet-template").html());
      this.$el.html(this.template(this.model.toJSONForRenderer()));
      this.$el.attr("id", "");
      this.$el.attr("data-name", this.model.getName());
      return this;
    }
  });


  // Composer
  var ComposerView = Backbone.View.extend({
    initialize : function(options) {
      var options = options || {};

      this.model = new ComposerContainer(null, {
        url : options.fetchPortletURL
      });
      this.listenTo(this.model, 'container.addChild', this.onAddChild);
      this.model.fetch();
    },

    getModel: function() {
      return this.model;
    },

    onAddChild : function(child) {

      // TODO: rename to application-list
      var container = $('#portlet-list');

      var view = new ApplicationFactoryView({model : child});
      container.append(view.render().$el);

      //Enable draggable
      $(view.$el).draggable({
        connectToSortable: ".sortable",
        revert: "invalid",
        helper: "clone"
      });
    }
  });

  var ApplicationView = Backbone.View.extend({
    tagName: "div",

    // TODO: rename to "window"
    className: "portlet",
    initialize: function() {
      this.model.on('change', this.updateContent, this);
      this.model.fetchContent();
    },
    render: function() {
      this.template = _.template($("#application-template").html());
      this.$el.html(this.template(this.model.toJSONForRenderer()));
      this.$el.attr("id", this.model.getId());
      return this;
    },
    updateContent: function() {
      var id = this.model.getId();
      var selector = "#" + id + " div";
      $(selector).html(this.model.getContent());
    }
  });

  var ContainerView = Backbone.View.extend({
    events : {
      "click .close" : "deleteApp"
    },

    initialize : function(options) {

      // Listen to add/remove events on the new model
      this.listenTo(this.model, 'container.addChild', this.onAddChild);
      this.listenTo(this.model, 'container.removeChild', this.onRemoveChild);

      var domId = "#" + this.model.getId();
      this.$el = $(domId);

      // Trigger adding D&D ability to Zone and Application elements
      this.setupDnD();
    },

    setupDnD : function() {
      this.$el.sortable({
        connectWith : ".sortable",
        tolerance : "pointer",
        placeholder : "portlet-placeholder",
        revert : true,
        update : (function(view) {
          return function() {
            view.dropApp.apply(view, arguments);
          };
        })(this)
      });
    },

    /*
     * Listen to DOM event
     */
    // Drag and drop
    dropApp : function(event, ui) {
      var $dragObj = $(ui.item);
      var targetContainerId = $dragObj.closest('.sortable').attr('id');
      var targetContainer = null;
      if(targetContainerId == this.model.getId()) {
        targetContainer = this.model;
      } else {
        var targetContainer = this.model.getParent().getChild(targetContainerId);
      }

      var prev = $dragObj.prev('.portlet');
      var idx = 0;
      if (prev.length) {
        idx = $('#' + targetContainer.getId() + ' > .portlet').index(prev.get(0)) + 1;
      }

      // Modify the model
      if(!$dragObj.attr("id")) {
        //Add new application
        var composerView = window.editorView.getComposerView();
        var application = composerView.getModel().findChildByName($dragObj.attr("data-name"))[0];

        //Clone and generate id for new application
        var newChild = application.clone();
        newChild.setId(newChild.getName() + new Date().getTime());
        targetContainer.addChild(newChild, {at: idx});

        //Remove dropped item
        $(ui.item).remove();
      } else {
        targetContainer.addChild(ui.item.attr('id'), {
          at : idx
        });
      }
    },
    // An event handler for deleting a window.
    // Find the target window ID and container ID
    // then use them to modify corresponding models
    deleteApp : function(e) {
      var appId = $(e.target).closest('div.portlet').attr('id');
      var containerId = $(e.target).closest('div.sortable').attr('id');
      var layoutView = editorView.layoutView;
      var container = layoutView.getModel().getChild(containerId);
      container.removeChild(appId);
    },
    /*
     * Listen to model changes
     */
    onAddChild : function(child, container) {
      var $cont = $('#' + container.getId());
      var $app = $('#' + child.getId());
      var prev = container.at(child.getIndex() - 1);

      if(!$app.html()) {
        //Create new view of application
        var appView = new ApplicationView({model: child});
        appView = appView.render();
        $app = $(appView.$el);
      }

      if (prev) {
        $app.insertAfter($('#' + prev.getId()));
      } else {
        $cont.prepend($app);
      }
      $cont.removeClass('emptyContainer');
    },
    onRemoveChild : function(child, container) {
      var $cont = $("#" + container.getId());
      var $app = $cont.children('#' + child.getId());
      $app.remove();

      if (container.isEmpty()) {
        $cont.addClass('emptyContainer');
      }
    }
  });
  //
  var LayoutView = Backbone.View.extend({
    initialize : function(options) {
      var options = options || {};
      this.editUrl = options.editUrl;

      // Build model from current DOM
      var model = this.buildModel();
      this.setModel(model);
    },

    setModel : function(model) {

      // Stop listening to events on the old model
      //this.stopListening();

      // Assign to new model
      this.model = model;
      return this;
    },

    // Listen to clicking on SAVE button
    save : function() {

      // Delegate to MODEL#save
      var view = this;
      this.model.save().done(function($data) {
        if ($data.code == 200) {
          // model saving success
          window.location.href = view.model.url;
        } else {
          // model saving error
          alert("error: " + data.message);
        }
      }).error(function($error) {
        // network error
        alert("error on connect to server");
      });
      return this;
    },

    switchLayout : function(layoutData) {

      // Temporarily hide the old layout
      this.$el.each(function() {
        var id = $(this).attr('id');
        $(this).attr('id', id + '-old');
      });
      var tmp = $('<div ></div>').html(this.$el.html());
      $('body').append(tmp.hide());

      // Apply the new layout template
      this.$el.html(layoutData.html);

      // Retrieve this before building the new model
      var _model = this.model;

      // Build new model according to new layout
      var model = this.buildModel();
      this.setModel(model);
      if (layoutData.layout_id) {
        this.model.setLayoutId(layoutData.layout_id);
      }

      // Start switching layout
      _model.switchLayout(this.model);

      // remove old layout
      tmp.remove();
      return this;
    },

    // Build model from DOM
    buildModel : function() {
      var _model = new PageContainer({id : 'layoutId'}, {url : this.editUrl});
      this.$el.find('.sortable').each(function() {
        var cont = new Container({
          id : this.id,
          children : new Backbone.Collection([Application])
        });
        $(this).children('.portlet').each(function() {
          var app = new Application({
            'id' : this.id
          });
          cont.addChild(app);
        });
        new ContainerView({model: cont});
        _model.addChild(cont);
      });
      return _model;
    },
    getModel: function() {
      return this.model;
    }
  });

  // The root container view of Layout Edition mode
  var EditorView = Backbone.View.extend({
    events : {
      "click .switch" : "switchLayout",
      "click #saveLayout" : "saveLayout"
    },

    initialize : function() {

      // Be sure that the element LAYOUT-EDITION has already been available in DOM
      if (this.el) {

        // Initialize LayoutView 
        this.layoutView = new LayoutView({
          el : '.pageBody',
          editUrl : this.$el.attr('data-editURL')
        });

        // Composer
        var composerRoot = this.$("#composers");
        this.composerView = new ComposerView({
          el : '#composers',
          fetchPortletURL : composerRoot.attr("data-url")
        });
        // End composer
      }
    },

    getComposerView: function() {
      return this.composerView;
    },
    getPageView: function() {
      return this.layoutView;
    },

    // Delegate to the LayoutView save
    saveLayout : function() {
      this.layoutView.save();
    },

    // Clicked on Swich layout button
    switchLayout : function(e) {
      var anchor = e.target;
      var href = $(anchor).attr('href');
      e.preventDefault();

      $.ajax({
        url : href,
        dataType : "json",
        success : function(result) {
          if (result.code != 200) {
            alert("change layout failure!");
            return false;
          }

          // Delegate to LayoutView
          var layoutView = window.editorView.layoutView;
          layoutView.switchLayout(result.data);
        }
      });
    }
  });

  // Trigger to initialize the LAYOUT EDITION mode
  $(function() {
    window.editorView = new EditorView({el : '.LAYOUT-EDITION'});
  });
})();
