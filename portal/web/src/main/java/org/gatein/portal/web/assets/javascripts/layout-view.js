(function(Backbone, layoutDef, $, jqueryUI, editorView) {
  
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
      var pageView = editorView.getPageView();
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
        var composerView = editorView.getComposerView();
        
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
      var pageView = editorView.getPageView();
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
      this.urlRoot = this.$el.attr("data-urlRoot");
      this.layoutId = this.$el.attr('data-layoutId');
      this.pageKey = this.$el.attr('data-pageKey');
      this.pageDisplayName = this.$el.attr('data-pageDisplayName');
      this.factoryId = this.$el.attr('data-factoryId');
      this.parentLink = this.$el.attr('data-parentLink');
      this.pageName = this.pageKey.substring('portal::classic::'.length);
      
      //TODO: remove /null at the end of url - this should be refactor later
      this.urlRoot = this.urlRoot.substring(0, this.urlRoot.length - 4);

      // Build model from current DOM
      this.buildModel();
      this.listenTo(this.model, "change:factoryId", this.changeFactoryId);
    },
    
    changeFactoryId : function() {
      var factoryId = this.model.get('factoryId');
      $("input#composer-layout-" + factoryId).click();
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
          {
            id : this.layoutId, 
            factoryId : this.factoryId, 
            pageKey: this.pageKey, 
            pageName : this.pageName, 
            pageDisplayName : this.pageDisplayName,
            parentLink : this.parentLink,
            html : this.$el.html()
          },  
          {
            urlRoot : this.urlRoot, 
            model : Container
          }
      );
      
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
      
      this.model = model;
    }
  });

  return LayoutView;
})(Backbone, layoutDef, $, jqueryUI, editorView);
