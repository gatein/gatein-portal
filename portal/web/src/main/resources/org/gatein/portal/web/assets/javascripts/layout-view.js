(function() {

  var ComposerView = Backbone.View.extend({
    initialize : function(options) {

      // Initialize a Backbone.Collection to hold a list of Application models
      this.apps = new Backbone.Collection([], {
        model: Application,
        url : this.$el.attr("data-url")
      });

      this.listenTo(this.apps, 'reset', this.render);
      this.apps.fetch({reset: true});
    },

    render: function() {
      var $container = $('#application-list');
      $container.html();

      //For each to render app
      //This should be done in template but currently underscore-template conflict with juzu-template
      _.each(this.apps.toJSON(), function(app){
        var html = _.template($("#portlet-template").html(), app);
        var $html = $(html);
        $container.append($html);

        //Enable draggable
        $html.draggable({
          connectToSortable: ".sortable",
          revert: "invalid",
          helper: "clone"
        });
      });
    }
  });

  var ApplicationView = Backbone.View.extend({
    tagName: "div",

    className: "window",

    events : {
      "click .close" : "delete"
    },

    initialize: function() {

      // Bind the callback 'updateContent' to the 'change' event of the Application model
      // The callback will be executed in this ApplicationView object context
      this.model.on('change:content', this.updateContent, this);
    },

    // Render the application frame from template
    render: function() {
      var template = _.template($("#application-template").html());
      this.$el.html(template(this.model.toJSON()));
      this.$el.attr("id", this.model.getId());
      return this;
    },

    // Update the content from Application model to DOM
    updateContent: function() {
      var id = this.model.getId();
      var selector = "#" + id + " div";
      $(selector).html(this.model.get("content"));
    },

    delete: function() {
      this.model.getParent().removeChild(this.model);

      // Update snapshot
      var pageView = window.editorView.getPageView();
      pageView.model.updateSnapshot();
    }
  });

  var ContainerView = Backbone.View.extend({

    initialize : function(options) {

      // Listen to add/remove events on the new model
      this.listenTo(this.model, 'container.addChild', this.onAddChild);
      this.listenTo(this.model, 'container.removeChild', this.onRemoveChild);

      var domId = "#" + this.model.getId();
      this.$el = $(domId);

      this.setupDnD();
    },

    // Adding DnD ability to Zone and Application
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

      // If this is a new application dragged from Composer
      if(!$dragObj.attr("id")) {

        //Add new application
        var composerView = window.editorView.getComposerView();
        var application = composerView.apps.findWhere({ 'name' : $dragObj.attr("data-name")});

        // Clone and generate id for new application
        // TODO: It should NOT force assigning an ID value for a transient model 
        var newChild = application.clone();
        targetContainer.addChild(newChild, {at: idx});
        newChild.fetchContent();

        // Remove dropped item
        $(ui.item).remove();
      } else {
        targetContainer.addChild(ui.item.attr('id'), {
          at : idx
        });
      }
      
      // Update snapshot
      var pageView = window.editorView.getPageView();
      pageView.model.updateSnapshot();
    },

    // A callback for the 'container.addChild' event of Container model
    onAddChild : function(child, container) {
      var $cont = $('#' + container.getId());
      var $app = $('#' + child.getId());
      var prev = container.at(child.getIndex() - 1);

      // If it is an existing application
      if(!$app.html()) {

        // Create new view of application
        var appView = new ApplicationView({model: child});
        $app = appView.render().$el;
      }

      if (prev) {
        $app.insertAfter($('#' + prev.getId()));
      } else {

        // Insert at beginning of container element
        $cont.prepend($app);
      }
      $cont.removeClass('emptyContainer');
    },

    // A callback for the 'container.removeChild' event of Container model.
    // It removes the child element from DOM
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
    },

    // Listen to clicking on SAVE button
    save : function() {

      // Delegate to MODEL#save
      var view = this;
      this.model.save({}, {
        parse: false,
        success: function(model, resp, options) {
          window.location.href = view.pageURL;
        },
        error: function(model, xhr, options) {
          //TODO: need to define a unified error handler on UI
          alert("error when save");
        }
      });
      return this;
    },

    // Switch layout with data structure passed as the layoutData argument
    switchLayout : function(layoutData) {

    	var layout = new Object();
    	layout.id = layoutData.factoryId;
    	layout.html = layoutData.html;
    	var containers = [];
    	$(layout.html).find('.sortable').each(function() {
    		containers.push(this.id);
    	});
    	layout.containers = containers;
    	
      // Start switching layout
      this.model.switchLayout(layout);
      
      // Update current DOM by layout template
      this.$el.html(layoutData.html);
      this.render();
      
      // Update ContainerView
      $(this.model.getChildren()).each(function() {
      	new ContainerView({model : this});
      })
      
      return this;
    },
        
    render : function() {
      var containers = this.model.getChildren();
      $(containers).each(function() {
        var id = this.id;
        var apps = this.getChildren();
        $(apps).each(function() {
          var appView = new ApplicationView({model : this});
          var $app = appView.render().$el;
          $('#' + id).append($app);
        });
      });
    },

    // Build model from existing DOM
    buildModel : function() {

      // TODO: Consider to initialize PageLayout model's url properly following Backbone standard
      var model = new PageLayout(
          {id : this.layoutId, pageKey: this.pageKey}, 
          {urlRoot : this.urlRoot, _children : new Backbone.Collection([], {model : Container})});

      // Loop through all Zone and Application
      this.$el.find('.sortable').each(function() {
        var container = new Container({id : this.id}, {_children : new Backbone.Collection([], {mode : Application})});

        $(this).children('.window').each(function() {
          var content = $(this).find('.content').html();
          var title = $(this).find('.title').text();
          var app = new Application({
            'id' : this.id,
            'content' : content,
            'title' : title
          });

          container.addChild(app);
        });

        new ContainerView({model: container});
        model.addChild(container);
      });
      
      model.updateSnapshot();
      
      return model;
    },
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

    // Clicked on Switch layout button
    changeLayout : function(e) {
      var anchor = e.target;
      var href = $(anchor).attr('href');
      e.preventDefault();

      // Make an ajax request to fetch the new layout data [layout_id, html_template]
      $.ajax({
        url : href,
        dataType : "json",
        success : function(result) {
          // Ask the layout view to switch layout with passed layout data
          var layoutView = window.editorView.layoutView;
          layoutView.switchLayout(result);
        }
      });
    }
  });

  // Trigger to initialize the LAYOUT EDITION mode
  $(function() {
    window.editorView = new EditorView({el : '.LAYOUT-EDITION'});
  });
})();
