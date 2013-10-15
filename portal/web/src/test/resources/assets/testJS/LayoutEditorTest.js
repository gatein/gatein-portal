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
	cont1.addChild('1_1', {at : 1});
	
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
	cont2.addChild(app1, {at : 1});
	equal(app1.getParent().getId(), '2');
	equal(app1.getIndex(), 1);
	
	//now app2 is in position 2
	equal(app2.getParent().getId(), '2');
	equal(app2.getIndex(), 2);
});

test("test create container from JSON", function() {
	var data = {id : 'root', childrens: [{id : 'cont1'} , {id : '2', childrens : []}]};
	container.set(data);
	
	equal(container.getId(), 'root');
	equal(container.getChild('cont1').getId(), 'cont1');
	equal(container.getChild('1'), null);
	equal(container.getChild('2').getChild('2_2'), null);
});


test("test toJSON", function() {
	var data = container.toJSON();
	equal(data.childrens.length, 2);
	
	var cont1 = data.childrens[0];
	equal(cont1.id, 1);
	equal(cont1['childrens'].length, 2);
	equal(cont1['childrens'][0].id, '1_1');
	equal(cont1['childrens'][1].id, '1_2');
	
	var cont2 = data.childrens[1];
	equal(cont2.id, 2);
	equal(cont2['childrens'].length, 2);
	equal(cont2['childrens'][0].id, '2_1');
	equal(cont2['childrens'][1].id, '2_2');
});

test("test switchLayout", function() {
	var oneZone = new Container();
	oneZone.addChild(new Container({id : "1"}));
	equal(1, oneZone.get("_childrens").length);
	container.switchLayout(oneZone);
	
	equal(4, oneZone.getChild("1").getChildrens().length);
	equal('1_1', oneZone.getChild('1').at(0).getId());
	equal('1_2', oneZone.getChild('1').at(1).getId());
	equal('2_1', oneZone.getChild('1').at(2).getId());
	equal('2_2', oneZone.getChild('1').at(3).getId());
	
	var twoZone = new Container();
	twoZone.addChild(new Container({id : "1"}));
	twoZone.addChild(new Container({id : "2"}));
	container.switchLayout(twoZone);
	
	equal('1_1', twoZone.getChild('1').at(0).getId());
	equal('1_2', twoZone.getChild('1').at(1).getId());
	equal('2_1', twoZone.getChild('2').at(0).getId());
	equal('2_2', twoZone.getChild('2').at(1).getId());
	
	var threeZone = new Container();
	threeZone.addChild(new Container({id : "1"}))
	threeZone.addChild(new Container({id : "2"}))
	threeZone.addChild(new Container({id : "3"}))
	container.switchLayout(threeZone);
	
	equal('1_1', threeZone.getChild('1').at(0).getId());
	equal('1_2', threeZone.getChild('1').at(1).getId());
	equal('2_1', threeZone.getChild('2').at(0).getId());
	equal('2_2', threeZone.getChild('2').at(1).getId());
	equal(true, threeZone.getChild('3').isEmpty());
});