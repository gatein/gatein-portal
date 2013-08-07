var App = Backbone.Model.extend({
	defaults : {
		'id' : null, 'containerID' : null, 'prev' : null, 'staleData' : false
	}
});
var Layout = Backbone.Collection.extend({
	model : App,
	changes : [],
	move : function(target, to, prev) {
		var app = this.get(target);
		if (app) {
			app.set({'containerID': to, 'prev': prev}, {'silent': true});
			this.changes.push(app);
		}
	},
	save : function() {
		function moveSuccess(model, response, options) {
			if (response) {
				model.collection.set(response);
			}
		}
		function moveError(model, xhr, options) {
			model.set('staleData', true);
		}
		
		//reset stateData when save
		this.each(function(app) {
			app.set('staleData', false);
		});
		_.each(this.changes, function(app) {
			app.save(null, {'success': moveSuccess, 'error': moveError});
		});
		this.changes = [];
	}	
});