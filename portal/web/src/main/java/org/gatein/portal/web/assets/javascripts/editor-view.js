(function(Backbone, $) {
  var EditorState = Backbone.Model.extend({
    defaults : {
      editMode : 0      
    }
  }, {
    NORMAL: 0,
    EDIT_CURRENT_PAGE: 1,
    EDIT_NEW_PAGE: 2,
    EDIT_SITE: 3
  });

  // The root container view of Layout Edition mode
  var EditorView = Backbone.View.extend({
    events : {
      'click #saveLayout' : 'saveLayout',
      'click .editLayout' : 'startEditCurrentPage',
      'click .cancelEditLayout' : 'cancelEdit',
      'click .newPage' : 'addNewPage',
      'click .pageProperties' : 'changePageProperties'
    },

    startEditCurrentPage : function() {
      this.model.set('editMode', EditorState.EDIT_CURRENT_PAGE);
      var _this = this;
      require(["layout-view", "composer-view"], function(LayoutView, ComposerView) {
        _this.switchMode(LayoutView, ComposerView);
      });
    },
    
    addNewPage : function() {
      this.model.set('editMode', EditorState.EDIT_NEW_PAGE);
      var _this = this;
      require(['page-properties-view'], function(PagePropertiesModal) {
        _this.pageModal = new PagePropertiesModal({ el : "#pagePropertiesModal"})
        _this.pageModal.render();
      });
    },
    
    changePageProperties : function() {
      if (this.pageModal == undefined) {
        var _this = this;
        require(['page-properties-view'], function(PagePropertiesModal) {
          _this.pageModal = new PagePropertiesModal({ el : "#pagePropertiesModal"})
          _this.pageModal.render();
        });
      } else {
        this.pageModal.render();
      }
    },
    
    cancelEdit : function() {
      this.model.set('editMode', EditorState.NORMAL);
      window.location.reload();
    },
    
    switchMode : function(LayoutView, ComposerView) {
      this.$el.toggleClass('LAYOUT-EDITION');
      if (this.model.get('editMode') > EditorState.NORMAL) {
        // Initialize LayoutView
        this.layoutView = new LayoutView({
          el : '.pageBody'
        });
        // Initialize ComposerView
        this.composerView = new ComposerView({el : '#composers'});
        this.layoutView.render();
      } else {
        delete this.layoutView;
        delete this.composerView;
      }
      this.trigger('eXo.portal.switchMode', this.model.get('editMode'), this);
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
    }
  });

  // Trigger to initialize the LAYOUT EDITION mode
  var editorView = new EditorView({el : 'body > .container', model: new EditorState()});
  editorView.EditorState = EditorState;
  return editorView;
})(Backbone, $);