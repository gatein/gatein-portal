(function(Backbone, $, _, editorView){
  
  var PagePropertiesModal = Backbone.View.extend({
    events : { 
      "click .cancel" : "cancel",
      "click .next" : "nextStep"
    },
    
    cancel : function() {
      this.$el.removeData('modal');
      $('#pagePropertiesModal').modal('hide');
      var editMode = editorView.model.get('editMode');
      if (editMode == editorView.EditorState.EDIT_NEW_PAGE) {
        editorView.model.set('editMode', editorView.EditorState.NORMAL);
      }
    },
    
    render : function() {
      var _this = this;
      var editMode = editorView.model.get('editMode');
      if (editMode == editorView.EditorState.EDIT_NEW_PAGE) {
        $.ajax({
          url : this.$el.attr('data-parentLinks'),
          dataType : 'json',
          success : function(data) {  
            var template = $("#page-properties-modal-template").html();
            var html = _.template(template, {parentLinks: data.parentLinks});
            _this.$el.find('.modal-body').html(html);
          }
        });
      } else if (editMode == editorView.EditorState.EDIT_CURRENT_PAGE) {
        var template = $("#page-properties-modal-template").html();
        var pageModel = editorView.getPageView().model;
        var html = _.template(template, {parentLinks: [pageModel.get('parentLink')]});
        _this.$el.find('.modal-body').html(html);
        $(".modal-body input[name='pageName']").val(pageModel.get('pageName')).prop('disabled', true);
        $(".modal-body input[name='pageDisplayName']").val(pageModel.get('pageDisplayName'));
        $(".modal-body select[name='parentLink']").val(pageModel.get('parentLink')).prop('disabled', true);
        $(".modal-body select[name='factoryId']").val(pageModel.get('factoryId'));
      }
    },
    
    bindToPageModel : function(pageModel) {
      pageModel.set("id", "newpage"); 
      pageModel.set("factoryId", $(".modal-body select[name='factoryId']").val()); 
      pageModel.set("pageKey", "portal::classic::" + $(".modal-body input[name='pageName']").val()); 
      pageModel.set("pageName", $(".modal-body input[name='pageName']").val());
      pageModel.set("pageDisplayName", $(".modal-body input[name='pageDisplayName']").val());
      pageModel.set("parentLink", $(".modal-body select[name='parentLink']").val());
    },
    
    nextStep : function() {
      var _this = this;
      var editMode = editorView.model.get('editMode');
      if (editMode == editorView.EditorState.EDIT_CURRENT_PAGE) {
        var pageModel = editorView.getPageView().model;
        pageModel.set('pageDisplayName', $(".modal-body input[name='pageDisplayName']").val());
        pageModel.set('factoryId', $(".modal-body select[name='factoryId']").val());
        editorView.getComposerView().setFactoryId();
        this.$el.modal('hide');
      } else if (editMode == editorView.EditorState.EDIT_NEW_PAGE) {
        var pageNameInput = _this.$el.find("input[name='pageName']");
        if (_this.verifyPageName(pageNameInput)) {
          $.ajax({
            url : _this.$el.attr('data-checkpage-url'),
            dataType : "json",
            data : {
              pageName : $(pageNameInput).val()
            },
            success : function(data) {
              if (data.pageExisted) {
                _this.message("Page is existed");
                $(pageNameInput).select();
              } else {
                require(['layout-view', 'composer-view'], function(LayoutView, ComposerView){
                  editorView.switchMode(LayoutView, ComposerView);
                  var pageView = editorView.getPageView();
                  var pageModel = pageView.model;
                  _this.bindToPageModel(pageModel);
                  //clear apps
                  var containers = pageModel.getChildren();
                  $(containers).each(function() {
                    if (!this.isEmpty()) {
                      var container = this;
                      var apps = this.getChildren();
                      $(apps).each(function() {
                        container.removeChild(this);
                      });
                    }
                  });
                  //
                  $('#pagePropertiesModal').modal('hide');
                });
              }
            }
          });
        }
      }
    },
    
    verifyPageName : function(input) {
      var regex = new RegExp('^[a-zA-Z0-9._-]{3,120}$');
      var pageName = $(input).val();
      if (!pageName) {
        setTimeout(function(){
          $(input).select();
        }, 0);
        return false;
      }
      if (!regex.test(pageName)) {
        this.message("Only alpha, digit, dash and underscore characters (3 - 120) allowed for page name.");
        //workaround to select input
        setTimeout(function(){
          $(input).select();
        }, 0);
        return false;
      }
      return true;
    },
    
    message : function(msg) {
      var alertBox = $("<div class='alert alert-error'></div>")
      alertBox.text(msg);
      this.$el.find('.modal-body .alert').remove();
      this.$el.find('.modal-body').prepend(alertBox);
    }
  });
  
  return PagePropertiesModal;
})(Backbone, $, _, editorView);