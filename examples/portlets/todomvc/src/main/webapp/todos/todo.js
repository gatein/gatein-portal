(function(model, AppView, Backbone, $) {	
	var todos = new model.TodoList();		
	todos.fetch();
	
	var portlets = $('.TodoPortlet');
	for (var i = 0; i < portlets.length; i++) {
		new AppView({model : todos, el : portlets[i]});		
	}
	
	var Router = Backbone.Router.extend({
		routes : {
			"*filter" : "filter"
		},
		
		/** @expose */
		filter : function(param) {
			todos.filterTodo(param);
		}		
	});
	
	var router = new Router();
	Backbone.history.start();
})(todomodel, todoview, Backbone, $);