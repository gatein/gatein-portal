$(function() {

	var  LayoutView = Backbone.View.extend({
        initialize : function() {
        	var editing = this.$el.hasClass("editing"), view = this;
        	if (editing) {
        		this.$( ".sortable" ).sortable({
        			revert: true,
        			update : function(event, ui) {
        	    		var item = $(ui.item);
        	    		view.collection.move(item.attr('id'), this.id, item.prev('div').attr('id'));
        	    		view.collection.save();
        	    	}
        		});
        	}
        	
        	this.listenTo(this.collection, 'change:staleData', this.onFetchError);
        	this.listenTo(this.collection, 'change:containerID, change:prev', this.move);
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
        },
        
        onFetchError : function(app) {
        	if (app.get('staleData')) {
        		alert('cant fetch data from server');
        		window.location.reload();
        	}
        }
	});
		
	var root = $('.editing');
	var url = root.attr('data-editURL');	

	var apps = new Layout();
	$('.sortable').children().each(function() {
		var $this = $(this);
		
		var app = new App({'id' : this.id, 
					  'containerID' : $this.closest('.sortable').attr('id'), 
					  'prev' : $this.prev('div').attr('id')
					  });
		app.url = url;
		apps.add(app);
	});	
	new LayoutView({el : root.get(0), collection : apps});
});