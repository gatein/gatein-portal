(function() {
  /**
   * 
   */
  var EditorView = Backbone.View.extend({
    el : '.editing',

    events : {
      "click .switch" : "switchLayout",
      "click #saveLayout" : "saveLayout"
    },

    initialize : function() {
      if (this.$el.hasClass("editing")) {
        //
        this.layoutView = new LayoutView({
          editUrl : this.$el.attr('data-editURL')
        });

        // Composer
        var composerRoot = this.$("#composers");
        this.composerView = new ComposerView({
          fetchPortletURL : composerRoot.attr("data-url")
        });
        // End composer
      }
    },

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

  /**
   * 
   */
  var LayoutView = Backbone.View.extend({
    el : '.pageBody',

    events : {
      "click .close" : "deleteApp",
    },

    initialize : function(options) {
      this.setupDnD();

      var options = options || {};
      this.editUrl = options.editUrl;

      // Build model from current DOM
      var model = this.buildModel();
      // Setup model
      this.setModel(model);
      this.snapshotModel = this.model;
    },

    setModel : function(model) {
      // Stop listening on old model
      this.stopListening();

      this.model = model;
      // Listen on model changes
      this.listenTo(this.model, 'addChild.eXo.Container', this.onAddChild);
      this.listenTo(this.model, 'removeChild.eXo.Container', this.onRemoveChild);
      return this;
    },

    // Setup dragDrop
    setupDnD : function() {
      this.$(".sortable").sortable({
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
     * Listen to model changes
     */
    onAddChild : function(child, container) {
      var $cont = $('#' + container.getId());
      var $app = $('#' + child.getId());
      var prev = container.at(child.getIndex() - 1);
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
    },

    /*
     * Listen to DOM event
     */
    // Drag and drop
    dropApp : function(event, ui) {
      var dragObj = $(ui.item);
      var cont = this.model.getDescendant(dragObj.closest('.sortable').attr('id'));

      var prev = dragObj.prev('.portlet');
      var idx = 0;
      if (prev.length) {
        idx = $('#' + cont.getId() + ' > .portlet').index(prev.get(0)) + 1;
      }

      // Modify the model
      cont.addChild(ui.item.attr('id'), {
        at : idx
      });

      // Update snapshot
      this.snapshotModel = this.model;
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

    // Clicked on delete button of window
    deleteApp : function(e) {
      var appId = $(e.target).closest('div.portlet').attr('id');
      var containerId = $(e.target).closest('div.sortable').attr('id');
      var container = this.model.getDescendant(containerId);
      container.removeChild(appId);

      // Update snapshot
      this.snapshotModel = this.model;
    },

    /*
     * 
     */
    switchLayout : function(layoutData) {
      // temporary hide the old layout
      this.$el.each(function() {
        var id = $(this).attr('id');
        $(this).attr('id', id + '-old');
      });
      var tmp = $('<div ></div>').html(this.$el.html());
      $('body').append(tmp.hide());

      // Apply the new layout template
      this.$el.html(layoutData.html);

      // retrieve this before building the new model
      var snapshot = this.snapshotModel;

      // Build new model according to new layout
      var model = this.buildModel();
      this.setModel(model);
      if (layoutData.layout_id) {
        this.model.setLayoutId(layoutData.layout_id);
      }
      this.setupDnD();

      // Start switching layout
      snapshot.switchLayout(this.model);

      // remove old layout
      tmp.remove();
      return this;
    },

    // Build model from DOM
    buildModel : function() {
      var model = new PageContainer({
        id : 'layoutId'
      }, {
        url : this.editUrl
      });

      this.$el.find('.sortable').each(function() {
        var cont = new Container({
          id : this.id
        });
        $(this).children('.portlet').each(function() {
          var app = new Application({
            'id' : this.id
          });
          cont.addChild(app);
        });
        model.addChild(cont);
      });
      return model;
    }
  });

  /**
   * 
   */
  var ApplicationView = Backbone.View.extend({
    tagName : "li",
    initialize : function() {
    },
    render : function() {
      this.template = _.template($("#portlet-template").html());
      this.$el.html(this.template(this.model.toJSONForRenderer()));
      return this;
    }
  });

  /**
   * 
   */
  var ComposerView = Backbone.View.extend({
    el : $("#composers"),
    initialize : function(options) {
      var options = options || {};

      this.model = new ComposerContainer(null, {
        url : options.fetchPortletURL
      });
      this.listenTo(this.model, 'addChild.eXo.Container', this.onAddChild);
      this.model.fetch();
    },

    onAddChild : function(child) {
      var container = $('#portlet-list');
      var view = new ApplicationView({
        model : child
      }).render();
      container.append(view.$el);
    }
  });

  // Bootstrap view and model of the editor
  $(function() {
    window.editorView = new EditorView();
  });
})();
