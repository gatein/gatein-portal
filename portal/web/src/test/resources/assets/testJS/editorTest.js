module('test page model', {
	setup: function() {
		layout = new Layout();
		
		//setup 2 containers with 2 apps
		for (var i = 0; i < 2; i++) {
			for (var j = 0; j < 2; j++) {				
				layout.add({
					'id' : j + "_" + i,
					'containerID': i,
					'prev' : j > 0 ? (j - 1) + "_" + i : null
				});
			}
		}

		equal(layout.size(), 4, 'layout have 2 containers with 2 apps');
	},
	teardown: function() {window.layout = null;}
});

test("move app in same container test", function() {	
	layout.move('1_1', '1', '2_1');
	
	var app1 = layout.get('1_1');
	ok(app1, 'app1 should be not null');
	equal(app1.get('containerID'), '1');
	equal(app1.get('prev'), '2_1');
});

test("move app to another container test", function() {	
	layout.move('1_1', '2', '2_2');
	
	var app1 = layout.get('1_1');
	ok(app1, 'app1 should be not null');
	equal(app1.get('containerID'), '2');
	equal(app1.get('prev'), '2_2');
});

test("save success after move test", function() {
	var app = layout.get('1_1');
	
	var tmp = Backbone.sync;
	Backbone.sync = function(method, model, options) {
		equal('update', method, 'sync method is update when model attribute is changed');
		equal('1_1', model.get('id'), 'saving app 1_1');
	
		//fake server response data
		options.success({'layout' : [{'id': '2_1', 'containerID' : '1', 'prev' : null}, {'id': '1_1', 'containerID' : '1', 'prev' : '2_1'}]});
	}
	
	layout.move('1_1', '1', '2_1');
	layout.save();
	
	equal(2, layout.size(), 'after save, the whole list is refreshed with data from server');
	equal(null, layout.get('2_1').get('prev'), 'app 2_1 is first app');
	equal('2_1', layout.get('1_1').get('prev'), 'app 1_1 is second app');	
	
	Backbone.sync = tmp;	
});

test("save error after move test", function() {
	var app = layout.get('1_1');
	
	var tmp = Backbone.sync;
	Backbone.sync = function(method, model, options) {	
		//fake server response data
		options.error();
	}
	
	layout.move('1_1', '1', '2_1');
	layout.save();
	
	//error fetching data for app
	ok(app.get('staleData'));
	
	Backbone.sync = tmp;	
});