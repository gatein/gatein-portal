(function() {
	var  LayoutView = Backbone.View.extend({
		el : '.editing',
		
		events : {
			"click a.switch" : "switchLayout",
			"click button.close" : "deleteApp"
		},

        initialize : function() {
        	var editing = this.$el.hasClass("editing");
        	var view = this;
        	if (editing) {
        		this.$( ".sortable" ).sortable({
        			connectWith: ".sortable",
        			tolerance: "pointer",
        			placeholder: "portlet-placeholder",
        			revert: true,
        			update : function(event, ui) {
        				var dragObj = $(ui.item);
        	    		var cont = view.model.getDescendant(dragObj.closest('.sortable').attr('id'));
        	    		var prev = dragObj.prev('.portlet');
        	    		var idx = prev.length ? $('#' + cont.getId() + ' > .portlet').index(prev.get(0)) + 1 : 0;

        	    		cont.addChild(ui.item.attr('id'), {at : idx});

                        window.snapshotModel = view.model;
        	    	}
        		});
                this.$("#saveLayout").off().click(function(){
                    view.model.save().done(function($data){
                        if($data.code == 200) {
                            $url = _.result(view.model, 'url');
                            window.location.href = $url;
                        } else {
                            alert("error: " + data.message);
                        }
                    }).error(function($error){
                        alert("error on connect to server");
                    });
                    return false;
                });
        	}
        	this.listenTo(this.model, 'addChild.eXo.Container', this.onAddChild);
        	this.listenTo(this.model, 'removeChild.eXo.Container', this.onRemoveChild);
        },

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
        
        deleteApp : function(e) {
        	var appId = $(e.target).closest('div.portlet').attr('id');
        	var containerId = $(e.target).closest('div.sortable').attr('id')
        	var container = this.model.getDescendant(containerId);
        	container.removeChild(appId);
        },
        
        switchLayout : function(e) {
        	var anchor = e.target;
        	var href = $(anchor).attr('href');
        	e.preventDefault();
        	var url = this.$el.attr('data-editURL');
  				$.ajax({
  						url : href,
  						dataType : "json",
  						success: function(result) {
                if(result.code != 200) {
                    alert("change layout failure!");
                    return false;
                }

                var resultData = result.data;

                //Init new model
  							var newContainer = new PageContainer();
                newContainer.setUrlRoot(url);
                if(resultData.layout_id) {
                    newContainer.setLayoutId(resultData.layout_id);
                }

                var data = resultData.html;
  							$(data).find('.sortable').each(function() {
  								var cont = new Container({id : this.id});
  								newContainer.addChild(cont);
  							});

  							var pageBody = $('.pageBody');
  							pageBody.find('.sortable').each(function() {
  								var id = $(this).attr('id');
  								$(this).attr('id', id + '-old');
  							});
  							var oldLayout = $('<div></div>').hide().html(pageBody.html());
  							$(pageBody).html(data);
  							$('.container').append(oldLayout);

                var container = window.snapshotModel;
  							window.layoutView = new LayoutView({model : newContainer});
  							container.switchLayout(newContainer);

  							//remove old container
  							oldLayout.remove();
  						}
  				});
        }
	});

	var ApplicationView = Backbone.View.extend({
		tagName:  "li",
		initialize: function() {
		},
		render: function() {
			this.template = _.template($("#portlet-template").html());
			this.$el.html(this.template(this.model.toJSONForRenderer()));
			return this;
		}
	});
	var ComposerView = Backbone.View.extend({
		el: $("#composers"),
		initialize: function() {
			this.listenTo(this.model, 'addChild.eXo.Container', this.onAddChild);
		},

		onAddChild: function(child) {
			var container = $('#portlet-list');
			var view = new ApplicationView({model: child}).render();
			container.append(view.$el);
		}
	});

	//Bootstrap view and model of the editor 
	$(function() {
		//Composer
		var composerRoot = $("#composers");
		var fetchPortletURL = composerRoot.attr("data-url");

		var composer = new ComposerContainer();
		composer.url = fetchPortletURL;
		var composerView = new ComposerView({model: composer});

		composer.fetch();
		//End composer

		var root = $('.editing');
		var url = root.attr('data-editURL');

		var container = new PageContainer();
        container.setUrlRoot(url);
		$('.sortable').each(function() {
			var cont = new Container({id : this.id});			
			$(this).children('.portlet').each(function() {			
				var app = new Application({'id' : this.id});
				cont.addChild(app);
			});
			container.addChild(cont);
		});
		
    window.snapshotModel = container;
		window.layoutView = new LayoutView({model : container});
	});
})();
