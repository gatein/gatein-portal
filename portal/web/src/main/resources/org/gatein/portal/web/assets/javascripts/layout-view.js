(function() {

  // TODO: We should change to use Backbone.Collection somehow
  var ComposerView = Backbone.View.extend({
    initialize : function(options) {
      var options = options || {};

      this.model = new ComposerContainer(null, {
        url : this.$el.attr("data-url")
      });

      // TODO: When changing to use collection, we don't need to rely on the container.addChild event
      this.listenTo(this.model, 'container.addChild', this.onAddChild);

      this.model.fetch({
        success : function(model, response, options) {
          if (response.code != 200) {
            alert("error on fetch portlets");
            return;
          }
  
          // TODO: We could directly render whole composer here
          var portlets = response.data.portlets;
          $(portlets).each(function(i, portlet) {
            model.addChild(new Application({
              name : portlet.name,
              applicationName: portlet.applicationName,
              title: portlet.title
            }));
          });
        }
      });
    },

    onAddChild : function(child) {
      var $container = $('#application-list');

      var html = _.template($("#portlet-template").html(), child.toJSON());
      var $html = $(html);
      $container.append($html);

      //Enable draggable
      $($html).draggable({
        connectToSortable: ".sortable",
        revert: "invalid",
        helper: "clone"
      });
    }
  });

  var ApplicationView = Backbone.View.extend({
    tagName: "div",

    className: "window",
    initialize: function() {
      this.model.on('change:content', this.updateContent, this);

      // TODO: fetching data should be done in render phase
      this.model.fetchContent();
    },

    render: function() {
      this.template = _.template($("#application-template").html());
      this.$el.html(this.template(this.model.toJSON()));
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

      var prev = $dragObj.prev('.window');
      var idx = 0;
      if (prev.length) {
        idx = $('#' + targetContainer.getId() + ' > .window').index(prev.get(0)) + 1;
      }

      // Modify the model
      if(!$dragObj.attr("id")) {
        //Add new application
        var composerView = window.editorView.getComposerView();
        var application = composerView.model.findChildByName($dragObj.attr("data-name"))[0];

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
      
      // Update snapshot
      var pageView = window.editorView.getPageView();
      pageView.resetModelSnapshot();
    },
    // An event handler for deleting a window.
    // Find the target window ID and container ID
    // then use them to modify corresponding models
    deleteApp : function(e) {
      var appId = $(e.target).closest('div.window').attr('id');
      var containerId = $(e.target).closest('div.sortable').attr('id');
      var layoutView = editorView.layoutView;
      var container = layoutView.model.getChild(containerId);
      container.removeChild(appId);
      
      // Update snapshot
      var pageView = window.editorView.getPageView();
      pageView.resetModelSnapshot();
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
      this.pageURL = this.$el.attr('data-pageURL');
      this.urlRoot = this.$el.attr("data-urlRoot");
      this.layoutId = this.$el.attr('data-layoutId');
      this.pageKey = this.$el.attr('data-pageKey');

      //TODO: remove /null at the end of url - this should be refactor later
      this.urlRoot = this.urlRoot.substring(0, this.urlRoot.length - 4);

      // Build model from current DOM
      this.model = this.buildModel();
      this.snapshotModel = this.model;
    },

    // Listen to clicking on SAVE button
    save : function() {

      // Delegate to MODEL#save
      var view = this;
      this.model.save().done(function($data) {
        if ($data.code == 200) {
          // model saving success
          window.location.href = view.pageURL;
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

    // Switch layout with data structure passed as the layoutData argument
    switchLayout : function(layoutData) {

      // Backup the current layout html for doing switch layout later
      this.$el.each(function() {
        var id = $(this).attr('id');
        $(this).attr('id', id + '-old');
      });
      var tmp = $('<div ></div>').html(this.$el.html());
      $('body').append(tmp.hide());

      // Apply the new layout template
      this.$el.html(layoutData.html);

      // Build new model according to new layout
      this.model = this.buildModel();
      if (layoutData.factoryId) {
        this.model.set('factoryId', layoutData.factoryId);
      }

      // Start switching layout
      var snapshot = this.snapshotModel;
      snapshot.switchLayout(this.model);

      // remove old layout
      tmp.remove();
      return this;
    },

    // Build model from DOM
    buildModel : function() {

      // TODO: Consider to initialize PageLayout model's url properly following Backbone standard
      var _model = new PageLayout({id : this.layoutId, pageKey: this.pageKey}, {urlRoot : this.urlRoot});
      this.$el.find('.sortable').each(function() {
        var cont = new Container({id : this.id});
        $(this).children('.window').each(function() {
          var app = new Application({'id' : this.id});
          cont.addChild(app);
        });

        new ContainerView({model: cont});
        _model.addChild(cont);
      });
      return _model;
    },
    resetModelSnapshot: function() {
      this.snapshotModel = this.model;
    }
  });

  // The root container view of Layout Edition mode
  var EditorView = Backbone.View.extend({
    events : {
      "click .switch" : "changeLayout",
      "click #saveLayout" : "saveLayout"
    },

    initialize : function() {

      // Be sure that the element LAYOUT-EDITION has already been available in DOM
      if (this.el) {

        // Initialize LayoutView 
        this.layoutView = new LayoutView({
          el : '.pageBody'
        });

        // Initialize ComposerView
        this.composerView = new ComposerView({el : '#composers'});
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
    changeLayout : function(e) {
      var anchor = e.target;
      var href = $(anchor).attr('href');
      e.preventDefault();

      // Make an ajax request to fetch the new layout data [layout_id, html_template]
      $.ajax({
        url : href,
        dataType : "json",
        success : function(result) {
          if (result.code != 200) {
            alert("change layout failure!");
            return false;
          }

          // Ask the layout view to switch layout with passed layout data
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