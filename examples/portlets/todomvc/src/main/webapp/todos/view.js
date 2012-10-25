(function(Backbone, _, $) {
    var TodoView = Backbone.View.extend({
    
        tagName : "li",
    
        template : _.template($('.TodoPortlet > .ItemTmpl').html()),
    
        events : {
            "click .toggle" : "toggle",
            "dblclick .view" : "edit",
            "click .destroy" : "removeTodo",
            "keypress .Edit" : "finish",
            "blur .Edit" : "cancel"
        },
    
        initialize : function() {
    	    this.model.on('change', this.render, this);
    	    this.model.on('destroy', this.remove, this);
        },
    
        render : function() {
    	    this.$el.html(this.template(this.model.toJSON()));
    	    return this;
        },
    
        /** @expose */
        toggle : function() {
    	    this.model.toggle();
        },
    
        /** @expose */
        edit : function() {
    	    this.model.tryEdit();
    	    this.$('.Edit').focus();
        },
    
        /** @expose */
        cancel : function() {
    	    this.model.finishEdit(this.model.get('job'));
        },
    
        /** @expose */
        finish : function(e) {
    	    if (e.keyCode == 13) {
    		    var job = this.$('.Edit').val();
    		    if (!job) {
    			    this.removeTodo();
    		    } else {
    			    this.model.finishEdit(job);
    		    }
    	    } else if (e.keyCode === 27) {
    		    this.cancel();
    	    }
        },
    
        /** @expose */
        removeTodo : function() {
    	    this.model.destroy();
        }
    });
    
    var AppView = Backbone.View.extend({
        statsTemplate : _.template($('.StatsTmpl').html()),
    
        events : {
            "keypress .NewTodo" : "createTodo",
            "click .ClearCompleted" : "clearCompleted",
            "click .ToggleAll" : "toggleAll"
        },
    
        initialize : function() {
            this.input = this.$(".NewTodo");
            this.allCheckbox = this.$(".ToggleAll");
    
            this.model.on('add', this.renderTodo, this);
            this.model.on('add remove change:completed filter',
                    this.renderDecorator, this);
    
            _.each(this.model.models, this.renderTodo, this);
        },
    
        renderDecorator : function() {
            var completed = this.model.completed().length;
            var active = this.model.active().length;
    
            this.$('.Footer').html(this.statsTemplate({
                'active' : active,
                'completed' : completed,
                'filter' : this.model.filterParam
            }));
    
            this.allCheckbox.attr('checked', active == 0
                    && this.model.length != 0);
        },
    
        renderTodo : function(todo) {
            var view = new TodoView({
                model : todo
            });
            this.$('.TodoList').append(view.render().el);
        },
    
        /** @expose */
        createTodo : function(e) {
            if (e.keyCode != 13)
                return;
            if (!this.input.val())
                return;
    
            this.model.addTodo(this.input.val());
            this.input.val('');
        },
    
        /** @expose */
        clearCompleted : function() {
            this.model.clearCompleted();
        },
    
        /** @expose */
        toggleAll : function() {
            var completed = this.allCheckbox.attr("checked");
            this.model.toggleAll(completed === "checked");
        }
    });
    
    return AppView;
})(Backbone, _, $);