$(function() {
	var root = $('.editing');
	var url = root.attr('data-editURL');
	
	var App = Backbone.Model.extend({
		defaults : {
			'id' : null, 'containerID' : null, 'prev' : null
		},
		url : url
	});
	var Layout = Backbone.Collection.extend({
		model : App,
    	move : function(target, to, prev) {
    		var app = this.get(target);
    		if (app) {
    			app.save({'containerID': to, 'prev': prev}, {'silent': true, 'success': this.onMove, 'error': this.onError});
    		}
    	},
    	onMove: function(model, response, options) {
    		var layout = response['layout'];
    		if (layout) {
    			model.collection.set(layout);
    		}
    	},
    	onError: function(model, xhr, options) {
    		alert("can't edit page");
    		window.location.reload();
    	}
    });

	var  LayoutView = Backbone.View.extend({
        initialize : function() {
        	var editing = this.$el.hasClass("editing"), view = this;
        	if (editing) {
        		this.$( ".sortable" ).sortable({
        			revert: true,
        			update : function(event, ui) {
        	    		var item = $(ui.item);
        	    		view.collection.move(item.attr('id'), this.id, item.prev('div').attr('id'));
        	    	}
        		});
        	}
        	
        	this.listenTo(this.collection, 'change', this.move);
        },
        
        move : function(app) {
        	var $app = $('#' + app.get('id'));
        	var to = app.get('containerID') ? $('#' + app.get('containerID')) : null;
        	var prev = app.get('prev') ? $('#' + app.get('prev')) : null;
        	if (prev) {
        		$app.insertAfter(prev);
        	} else if (to) {
        		to.prepend($app);
        	}
        }
	});
		
	var apps = new Layout();
	$('.sortable').children().each(function() {
		var $this = $(this);
		apps.add({'id' : this.id, 'containerID' : $this.closest('.sortable').attr('id'), 'prev' : $this.prev('div').attr('id')});
	});	
	new LayoutView({el : root.get(0), collection : apps});
});