(function() {
	var  LayoutView = Backbone.View.extend({
		el : '.editing',
		
        initialize : function() {
        	var editing = this.$el.hasClass("editing");
        	var view = this;
        	
        	if (editing) {
        		this.$( ".sortable" ).sortable({
        			placeholder: "portlet-placeholder",
        			revert: true,
        			update : function(event, ui) {
        	    		var cont = view.model.getDescendant(this.id);
        	    		var prev = $(ui.item).prev('.portlet');
        	    		var idx = prev.length ? $('#' + cont.getId() + ' > .portlet').index(prev.get(0)) + 1 : 0;
        	    		
        	    		cont.addChild(ui.item.attr('id'), idx);
        	    	}
        		});
        	}
        	
        	this.listenTo(this.model, 'addChild.eXo.Container', this.onAddChild);
        	this.listenTo(this.model, 'removeChild.eXo.Container', this.onRemoveChild);
        },
        
        onAddChild : function(child, container) {
        	var $app = $('#' + child.getId());
        	var prev = container.at(child.getIndex() - 1);
        	if (prev) {
        		$app.insertAfter($('#' + prev.getId()));
        	} else {
        		var $cont = $('#' + container.getId());
        		$cont.prepend($app);
        	}
        },
        
        onRemoveChild : function(child, container) {
        	var $app = $('#' + child.getId());
        	$app.remove();
        }
	});
	
	$(function() {
		var root = $('.editing');
		var url = root.attr('data-editURL');
		
		var container = new Container();
		$('.sortable').each(function() {
			var cont = new Container({id : this.id});			
			
			$(this).children('.portlet').each(function() {			
				var app = new Application({'id' : this.id});
				cont.addChild(app);
			});
			container.addChild(cont);
		});
		
		window.layoutView = new LayoutView({model : container});
	});
})();
