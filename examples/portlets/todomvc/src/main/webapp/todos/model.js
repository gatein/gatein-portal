(function(Backbone, _) {	
    var Todo = Backbone.Model.extend({
    	defaults : {
    		'job' : '',
    		'completed' : false,
    		'editing' : false,
    		'display' : true
    	},		
    	
    	/** @expose */
    	toggle : function(options) {			
    		var opt = {'completed' : !this.get('completed')};
    		_.extend(opt, options);
    		this.save(opt);
    	},		
    	
    	finishEdit : function(job) {
    		this.save({'job' : job, 'editing' : false});
    	},
    	
    	tryEdit : function() {
    		this.save({'editing' : true});
    	},		
    	
    	/** @expose */
    	setDisplay : function(display) {
    		this.set({'display' : display});
    	}
    });
    
    var TodoList = Backbone.Collection.extend({
    
    	model : Todo,
    
    	localStorage : new Backbone.LocalStorage('todoPortlet'),
    
    	initialize : function() {
    		this.on('add change:completed', function(todo) {
    			if (this.filterParam === 'active' && todo.get('completed') || 
    					this.filterParam === 'completed' && !todo.get('completed')) {
    				todo.setDisplay(false);					
    			}
    		}, this);
    	},
    	
    	addTodo : function(job) {
    		return this.create({'job' : job});
    	},
    
    	completed : function() {
    		return this.filter(function(todo) {
    			return todo.get('completed');
    		});
    	},
    
    	active : function() {
    		return this.without.apply(this, this.completed());
    	},
    	
    	/** @expose */
    	filterTodo : function(param) {
    		if (param === 'active') {
    			_.invoke(this.completed(), 'setDisplay', false);
    			_.invoke(this.active(), 'setDisplay', true);
    		} else if (param === 'completed') {
    			_.invoke(this.active(), 'setDisplay', false);
    			_.invoke(this.completed(), 'setDisplay', true);
    		} else {
    			_.invoke(this.models, 'setDisplay', true);
    			param = '';
    		}
    		this.filterParam = param;
    		this.trigger("filter");
    	},
    	
    	clearCompleted : function() {
    		_.invoke(this.completed(), 'destroy');
    	},
    	
    	toggleAll : function(completed) {
    		_.invoke(this.models, 'toggle', {'completed' : completed});
    	}
    });
    
    return {'Todo' : Todo, 'TodoList' : TodoList};
})(Backbone, _);