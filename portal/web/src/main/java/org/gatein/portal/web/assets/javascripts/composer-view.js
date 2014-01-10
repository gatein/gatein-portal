(function(Backbone, layoutDef, $, editorView) {
  var ComposerView = Backbone.View.extend({
    events : {
      "keyup .composer-filter" : "onKeyUp",
      "click .close-composer": "closeComposer",
      'click a[data-toggle="tab"]': "onShowTabContent",
      'click input[name="composer-layout"]': "switchLayout"
    },

    initialize : function(options) {
      this.model = new layoutDef.Composer([], {model: layoutDef.ComposerTab, urlRoot: this.$el.attr("data-url")});
      this.model.fetch();

      this.listenTo(this.model, 'sync', this.render);
      this.listenTo(this.model, 'change:filterValue', this.render);
    },

    render: function() {
      var $container = this.$el.find('#composer-list-contents');

      var renderData = this.model.getRenderData();
      if ($.trim($container.html()) == '') {
        var template = $("#composer-list-contents-template").html();
        var html = _.template(template, {items: renderData});
        $container.html(html);
        var layoutView = editorView.getPageView();
        var factoryId = layoutView.model.get('factoryId');
        $("input#composer-layout-" + factoryId).attr('checked', true);
        $container.find("li.content").draggable({
          connectToSortable: ".sortable",
          revert: "invalid",
          helper: "clone",
          start: function(event, ui) {
            ui.helper.width($(this).width());
          }
        });
      } else {
        this.filterApp(renderData);
      }
    },
    
    setFactoryId : function() {
      var layoutView = editorView.getPageView();
      var factoryId = layoutView.model.get('factoryId');
      $("input#composer-layout-" + factoryId).attr('checked', true);
    },

    filterApp: function(data) {

      this.$el.find(".content-type").each(function() {
        var $contentType = $(this);
        var contentType = _.findWhere(data, {tagName: $contentType.attr("data-tagName")});

        if(!contentType) {
          $contentType.hide();
        } else {
          $contentType.show();

          //Filter children
          var contents = contentType.children;

          $contentType.find(".content").each(function() {
            var $content = $(this);
            var content = _.findWhere(contents, {contentId: $content.attr("data-contentId")});
            if(!content) {
              $content.hide();
            } else {
              $content.show();
            }
          });
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
      var factoryId = $target.attr("data-factoryId");
      editorView.getPageView().model.set("factoryId", factoryId);
    },

    findContent: function(contentId) {
      return this.model.findContent(contentId);
    }
  });
  
  return ComposerView;
})(Backbone, layoutDef, $, editorView);