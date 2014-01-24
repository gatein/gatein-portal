(function(Backbone, $, _, editorView){
  
  var PagePropertiesModal = Backbone.View.extend({
    events : { 
      "click .cancel" : "cancel",
      "click .next" : "nextStep",
      'click a.permission-nav': 'changePermissionTab',
      'change input[type="checkbox"].everyone': "changePermission",
      "submit form.form-permission": "addPermission",
      "click li.permission": "removePermission"
    },

    //TODO: need to refactor this
    accessPermissions: [],
    editPermissions: [],
    
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
            if (editorView.getPageView() != undefined) {
              var pageModel = editorView.getPageView().model;
              $(".modal-body input[name='pageName']").val(pageModel.get('pageName'));
              $(".modal-body input[name='pageDisplayName']").val(pageModel.get('pageDisplayName'));
              $(".modal-body select[name='parentLink']").val(pageModel.get('parentLink'));
              $(".modal-body select[name='factoryId']").val(pageModel.get('factoryId'));
              _this.accessPermissions = pageModel.get('accessPermissions');
              _this.editPermissions = pageModel.get('editPermissions');
            }
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
        _this.accessPermissions = pageModel.get('accessPermissions');
        _this.editPermissions = pageModel.get('editPermissions');
      }

      //Need load All group and membershipType
      var everyone = {
        //Default everyone can access to page
        access: (this.accessPermissions.length == 1 && this.accessPermissions[0] == 'Everyone') || (this.accessPermissions.length == 0),
        edit: (this.editPermissions.length == 1 && this.editPermissions[0] == 'Everyone')
      };
      this.accessPermissions = _.without(this.accessPermissions, 'Everyone');
      this.editPermissions = _.without(this.editPermissions, 'Everyone');

      $.ajax({
        url: this.$el.attr('data-allGroupAndMembershipType'),
        dataType: 'json',
        success: function(data) {
          var template = $("#page-properties-modal-permissions").html();
          var html = _.template(template, {
            groups: data.groups,
            membershipTypes: data.membershipTypes,
            accessPermissions: _this.accessPermissions,
            editPermissions: _this.editPermissions,
            everyone: everyone
          });
          _this.$el.find('.modal-permissions').html(html);
        }
      });
    },
    
    bindToPageModel : function(pageModel) {
      pageModel.set("id", "newpage"); 
      pageModel.set("factoryId", $(".modal-body select[name='factoryId']").val()); 
      pageModel.set("pageKey", "portal::classic::" + $(".modal-body input[name='pageName']").val()); 
      pageModel.set("pageName", $(".modal-body input[name='pageName']").val());
      pageModel.set("pageDisplayName", $(".modal-body input[name='pageDisplayName']").val());
      pageModel.set("parentLink", $(".modal-body select[name='parentLink']").val());
    },

    bindPermissionToPageModel: function(pageModel) {
      //Access permission
      if(this.$el.find('input[name="accessPermission"]').is(":checked")) {
        pageModel.set('accessPermissions', ['Everyone']);
      } else {
        pageModel.set('accessPermissions', this.accessPermissions);
      }

      //Edit permission
      if(this.$el.find('input[name="editPermission"]').is(":checked")) {
        pageModel.set('editPermissions', ['Everyone']);
      } else {
        pageModel.set('editPermissions', this.editPermissions);
      }
    },
    
    nextStep : function() {
      var _this = this;
      var editMode = editorView.model.get('editMode');
      if (editMode == editorView.EditorState.EDIT_CURRENT_PAGE) {
        var pageModel = editorView.getPageView().model;
        pageModel.set('pageDisplayName', $(".modal-body input[name='pageDisplayName']").val());
        pageModel.set('factoryId', $(".modal-body select[name='factoryId']").val());
        _this.bindPermissionToPageModel(pageModel);
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
                  _this.bindPermissionToPageModel(pageModel);
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
    },

    changePermissionTab: function(e) {
      e.preventDefault();
      var $target = $(e.target);
      var $li = $target.closest('li');
      var $ul = $li.closest('ul');
      var $pagePermissions = $ul.closest('div#pagepermissions');

      $ul.find('li.active').removeClass('active');
      $li.addClass('active');

      $pagePermissions.find('div.row-permissions').addClass('hide');
      $pagePermissions.find($target.attr('href')).removeClass('hide');

      return false;
    },

    changePermission: function(e) {
      var $target = $(e.target);
      var $permissions = $target.closest('div.row-permissions').find('div.permissions');
      $permissions.toggleClass('hide');
    },

    addPermission: function(e) {
      e.preventDefault();

      var $form = $(e.target);
      var $permissions = $form.closest('div.permissions');

      var group = $permissions.find('select[name="group"]').val();
      var membership = $permissions.find('select[name="membershipType"]').val();
      if(group == '' || membership == '') {
        return;
      }

      var permission = membership + ":" + group;
      //Unique
      var existing = true;
      if($form.attr('name') == 'accessPermission' && !_.contains(this.accessPermissions, permission)) {
        existing = false;
        this.accessPermissions.push(permission);
      } else if($form.attr('name') == 'editPermission' && !_.contains(this.editPermissions, permission)) {
        existing = false;
        this.editPermissions.push(permission);
      }

      if(!existing) {
        var $permission = $('<li class="permission">' + permission + '</li>')
        $permissions.find('ul.list-permissions').append($permission);
      }

      $form.trigger('reset');
    },

    removePermission: function(e) {
      var $permission = $(e.target);
      var perm = $permission.html();

      var $form = $permission.closest("div.permissions").find('form.form-permission');
      if($form.attr('name') == 'accessPermission') {
        this.accessPermissions = _.without(this.accessPermissions, perm);
      } else if($form.attr('name') == 'editPermission') {
        this.editPermissions = _.without(this.editPermissions, perm);
      }


      $permission.remove();
    }
  });
  
  return PagePropertiesModal;
})(Backbone, $, _, editorView);