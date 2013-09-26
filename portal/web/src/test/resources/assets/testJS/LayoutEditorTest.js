module('test Container model', {
	setup: function() {
		window.container = new Container();
		
		//setup 2 nested containers with 2 apps
		for (var i = 1; i <= 2; i++) {
			var con = new Container({id : i.toString()});
			for (var j = 1; j <= 2; j++) {
				con.addChild( new Application({id : i + "_" + j}));
			}
			window.container.addChild(con);
		}
	},
	teardown: function() { delete window.container;}
});

test("move app in same container test", function() {
	//move app 1_1 from position 0 to 1
	var cont1 = container.getDescendant('1');
	cont1.addChild('1_1', 1);
	
	var app1 = container.getDescendant('1_1');
	ok(app1, 'app1 should be not null');
	equal(app1.getParent().getId(), '1');
	equal(app1.getIndex(), 1);
});

test("move app to another container test", function() {
	var app1 = container.getDescendant('1_1');
	equal(app1.getParent().getId(), '1');
	equal(app1.getIndex(), 0);
	
	var app2 = container.getDescendant('2_2');
	equal(app2.getParent().getId(), '2');
	equal(app2.getIndex(), 1);
	
	//move app 1_1 to container 2 at postion 1
	var cont2 = container.getDescendant('2');
	cont2.addChild(app1, 1);
	equal(app1.getParent().getId(), '2');
	equal(app1.getIndex(), 1);
	
	//now app2 is in position 2
	equal(app2.getParent().getId(), '2');
	equal(app2.getIndex(), 2);		
});