(function() {

  var AddNewPageView = Backbone.View.extend({
    events : { 
      "click .cancel" : "cancel",
      "click .next" : "nextStep",
    },
    
    initialize : function(options) {
      this.model = new Backbone.Model();
    },
    
    cancel : function() {
      this.$el.removeData('modal');
      $('#addNewPageModal').modal('hide');
    },
    
    changeProperties : function(e) {
      $('#addNewPageModal').modal('show');
    },
    
    nextStep : function() {
      var pageNameInput = this.$el.find("input[name='pageName']");
      if (this.verifyPageName(pageNameInput)) {

        var nextStepURL = this.$el.attr('data-nextstep-url');
        this.model.set("parent", this.$el.find("select[name='parent']").val());
        this.model.set("factoryId", this.$el.find("select[name='factoryId']").val());
        this.model.set("pageName", $(pageNameInput).val());
        this.model.set("label", this.$el.find("input[name='label']").val());
        var _this = this;

        $.ajax({
          url : nextStepURL,
          dataType : "json",
          data : {
            "pageName" :_this.model.get("pageName"),
            "label" : _this.model.get("label"),
            "parent" : _this.model.get("parent"),
            "factoryId" : _this.model.get("factoryId")
          },
          statusCode : {
            200 : function(result) {
              _this.$el.modal('hide');
              _this.$el.removeData('modal');
              
              var proBtn = $('<a class="pageProperties" href="#addNewPageModal">Properties</a>');
              $('a.newPage').replaceWith(proBtn);
              proBtn.on('click',  _this.changeProperties);
              _this.$el.on('shown', function() {
                $(".modal-body input[name='pageName']").val(_this.model.get('pageName'));
                $(".modal-body input[name='label']").val(_this.model.get('label'));
                $(".modal-body select[name='parent']").val(_this.model.get('parent'));
                $(".modal-body select[name='factoryId']").val(_this.model.get('factoryId'));
              });

              if ($(".pageBody .editNewPage").length == 0) {
                $('.pageBody').html(result.html);
                editor = window.editorView;
                editor.startEdit();
              } else {
                $(".pageBody .editNewPage").remove();
              }
              
              layout = editor.layoutView.model;
              layout.set("id","newpage");
              if (layout.get('factoryId') && layout.get('factoryId') != result.factoryId) {
                var layoutURL = $("#composer-layout-" + result.factoryId).attr('data-layoutURL');

                // Make an ajax request to fetch the new layout data [layout_id, html_template]
                $.ajax({
                  url : layoutURL,
                  dataType : "json",
                  async : false,
                  success : function(result) {
                    // Ask the layout view to switch layout with passed layout data
                    var layoutView = window.editorView.layoutView;
                    layoutView.switchLayout(result);
                  }
                });
              }
              
              layout.set("factoryId",result.factoryId);
              layout.set("parent", result.parent);
              layout.set("label", result.label);
              layout.set("pageKey", result.pageKey);
              
              $('.pageBody').prepend($("<div class='alert alert-warning editNewPage'><h5>Edit phase for temporary page \"" + result.pageKey + "\"</h5></div>"))
            }, 
            500 : function(resp) {
              _this.message(resp.responseText);
              $(pageNameInput).select();
            }
          }
        });
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
  
  var ComposerView = Backbone.View.extend({
    events : {
      "keyup .composer-filter" : "onKeyUp",
      "click .close-composer": "closeComposer",
      'click a[data-toggle="tab"]': "onShowTabContent",
      'click input[name="composer-layout"]': "switchLayout"
    },

    initialize : function(options) {
      this.model = new Composer([], {model: ComposerTab, urlRoot: this.$el.attr("data-url")});
      this.model.fetch();

      this.listenTo(this.model, 'sync', this.render);
      this.listenTo(this.model, 'change:filterValue', this.render);
    },

    render: function() {
      var $container = this.$el.find('#composer-list-contents');

      if ($.trim($container.html()) == '') {
        var template = $("#composer-list-contents-template").html();
        var html = _.template(template, {items: this.model.getRenderData()});
        $container.html(html);
        $container.find("li.content").draggable({
          connectToSortable: ".sortable",
          revert: "invalid",
          helper: "clone",
          start: function(event, ui) {
            ui.helper.width($(this).width());
          }
        });
      } else {
        this.filterApp();
      }
    },

    filterApp: function() {
      var filter = this.model.get('filterValue');
      var regex = new RegExp(filter, 'i');

      var _this = this;

      //Get all: content type div
      this.$el.find(".content-type").each(function() {
        var $contentType = $(this);
        var display = false;

        $contentType.find(".content").each(function() {
          var $content = $(this);
          var contentId = $content.attr("data-contentId");
          var content = _this.model.findContent(contentId);

          if(content && regex.test(content.get('title'))) {
            $content.show();
            display = true;
          } else {
            $content.hide();
          }
        });

        if(display) {
          $contentType.show();
        } else {
          $contentType.hide();
        }
      });
    },

    onKeyUp: function(e) {
      var keyCode = e.keyCode || e.which;
      var timeToWait = 500;
      var $target = $(e.target);

      if(keyCode == 27) {
        //Escape key
        $(e.target).val("");
        timeToWait = 0;
      }

      var value = $.trim($target.val());
      if(value == this.model.get("filterValue")) {
        return;
      }

      if(!$target.hasClass("loading")) {
        $target.addClass("loading");
      }

      if(this.timeout) {
        clearTimeout(this.timeout);
      }

      var _this = this;
      this.timeout =  setTimeout(function() {
        _this.model.set('filterValue', value);
        $target.removeClass("loading");
      }, timeToWait);
    },

    closeComposer: function(e) {
      var $target = $(e.target);
      this.$el.find(".active").removeClass("active");
      this.$el.find(".nav-tabs").addClass("nav-tabs-close");
      $target.hide();
    },
    onShowTabContent: function(e) {
      this.$el.find(".close-composer").show();
      this.$el.find(".nav-tabs").removeClass("nav-tabs-close");
    },

    switchLayout: function(e) {
      var $target = $(e.target);
      var layoutURL = $target.attr("data-layoutURL");

      // Make an ajax request to fetch the new layout data [layout_id, html_template]
      $.ajax({
        url : layoutURL,
        dataType : "json",
        success : function(result) {
          // Ask the layout view to switch layout with passed layout data
          var layoutView = window.editorView.layoutView;
          layoutView.switchLayout(result);
        }
      });
    },

    findContent: function(contentId) {
      return this.model.findContent(contentId);
    }
  });

  var ApplicationView = Backbone.View.extend({
    tagName: "div",

    className: "window",

    events : {
      "click .close" : "deleteApp"
    },

    initialize: function() {

      // Bind the callback 'updateContent' to the 'change' event of the Application model
      // The callback will be executed in this ApplicationView object context
      this.model.on('change:content', this.render, this);
    },

    // Render the application frame from template
    render: function() {
      //lazy loading portlet content
      this.model.fetchContent(window.location.href);
      
      var template = _.template($("#application-template").html());
      this.$el.html(template(this.model.toJSON()));
      this.$el.attr("id", this.model.getId());
      this.$el.find('.content').append("<div class='mask-layer'></div>");
      return this;
    },

    deleteApp: function() {
      this.model.getParent().removeChild(this.model);
      this.remove();

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
    
    events : {
      "dragenter" : "dragEnter",
      "dragover"  : "dragOver",
      "dragleave" : "dragLeave",
      "drop"      : "drop"
    },

    dragEnter : function ( event ) {
      console.log('enter');
      event.preventDefault();
      $('.portlet-placeholder').remove();
      
      if (this.model.isEmpty()) {
        $('#' + this.model.id).append("<li class='portlet-placeholder'></li>");
        return;
      }
      var Position = Backbone.Model;
      var list = new Backbone.Collection;
      list.comparator = 'top';
      
      $.each(this.model.getChildren(), function() {
        var top = $('#' + this.getId()).offset().top;
        list.add(new Position({id : this.getId(), top: top}));
      });
      list.add(new Position({id : 'placeholder', top : event.originalEvent.pageY}));
      var placeHolder = list.get('placeholder');
      var index = list.indexOf(placeHolder);
      if (list.at(index - 1)) {
        var id = list.at(index - 1).id;
        $("<li class='portlet-placeholder'></li>").insertAfter('#' + id);
      }
    },

    dragOver : function ( event ) {
      event.preventDefault();
    },

    dragLeave : function ( event ) {
      console.log('leave');
      event.preventDefault();
      if (!this.isInside(event)) {
        this.$el.find('.portlet-placeholder').remove();
      }
    },

    drop : function ( event ) {
      event.preventDefault();
      
      var files = event.originalEvent.dataTransfer.files;
      var formData = new FormData();
      formData.append('file', files[0]);
      var xhr = new XMLHttpRequest();
      xhr.open('POST', '/portal/upload');
      xhr.onload = function () {
        if (xhr.status === 200) {
          console.log('all done: ' + xhr.status);
        } else {
          console.log('Something went terribly wrong...');
        }
      };
      
      var file = files[0];
      var acceptedTypes = {
          'image/png': true,
          'image/jpeg': true,
          'image/gif': true
        };

      if (acceptedTypes[file.type] === true) {
        var reader = new FileReader();
        var el = this.$el;
        reader.onload = function (event) {
          var image = new Image();
          image.src = event.target.result;
          image.width = 100; // a fake resize
          el.append(image);
        };

        console.log(file);
        reader.readAsDataURL(file);
      }
      xhr.send(formData);
      $('.portlet-placeholder').remove()
    },
    
    isInside : function ( event ) {
      var top    = this.$el.offset().top;
      var left   = this.$el.offset().left;
      var right  = left + this.$el.outerWidth();
      var bottom = top + this.$el.outerHeight();
      if ((event.originalEvent.pageX > right) || (event.originalEvent.pageX < left)) {
        return false;
      }

      if ((event.originalEvent.pageY >= bottom) || (event.originalEvent.pageY <= top)) {
        return false;
      }

      return true;
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
        var composerView = window.editorView.getComposerView();
        
        //Add new application
        var newChild = composerView.findContent($dragObj.attr("data-contentId"));
        targetContainer.addChild(newChild, {at: idx});

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
        console.log("onRemoveChild.......");
        $cont.addClass('emptyContainer');
      }
    }
  });

  //
  var LayoutView = Backbone.View.extend({
    initialize : function(options) {
      var options = options || {};
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
          if (resp.redirect) {
            window.location = resp.redirect;
          } else {
            window.location.reload();
          }
        },
        error: function(model, xhr, options) {
          //TODO: need to define a unified error handler on UI
          $('html').html(xhr.responseText);
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
        if (!this.isEmpty()) {
          var apps = this.getChildren();
          $(apps).each(function() {
            var appView = new ApplicationView({model : this});
            var $app = appView.render().$el;
            //Remove if existed app
            $('#' + appView.model.getId()).remove();
            //Append newest app content
            $('#' + id).append($app);
          });
          $container = $('#' + id);
          $container.removeClass('emptyContainer');
        }
      });
    },

    // Build model from existing DOM
    buildModel : function() {

      // TODO: Consider to initialize PageLayout model's url properly following Backbone standard
      var model = new PageLayout(
          {id : this.layoutId, pageKey: this.pageKey}, 
          {urlRoot : this.urlRoot, model : Container});
      
      // Loop through all Zone and Application
      this.$el.find('.sortable').each(function() {
        var container = new Container({id : this.id}, {mode : Application});

        $(this).children('.window').each(function() {
          var content = $(this).find('.content').html();
          var title = $(this).find('.title').text();
          var app = new Application({
            'id' : this.id,
            'content' : content,
            'title' : title
          });
          new ApplicationView({model : app, el : "#" + app.getId()});
          container.addChild(app);
        });

        new ContainerView({model: container});
        model.addChild(container);
      });
      
      model.updateSnapshot();
      
      return model;
    }
  });
  
  var EditorState = Backbone.Model.extend({
    defaults : {
      editMode : 0      
    }
  }, {
    NORMAL: 0,
    EDIT_PAGE: 1,
    EDIT_SITE: 2
  });

  // The root container view of Layout Edition mode
  var EditorView = Backbone.View.extend({
    events : {
      'click #saveLayout' : 'saveLayout',
      'click .editLayout' : 'startEdit',
      'click .cancelEditLayout' : 'cancelEdit'
    },

    initialize : function() {
      this.listenTo(this.model, 'change:editMode', this.switchMode);
    },
    
    startEdit : function() {
      $('a.newPage').remove();
      this.model.set('editMode', EditorState.EDIT_PAGE);
    },
    
    cancelEdit : function() {
      this.model.set('editMode', EditorState.NORMAL);
      window.location.reload();
    },
    
    switchMode : function() {
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
  $(function() {
    window.editorView = new EditorView({el : 'body > .container', model: new EditorState()});
    window.addNewPageView = new AddNewPageView({ el : "#addNewPageModal"});
  });
})();
