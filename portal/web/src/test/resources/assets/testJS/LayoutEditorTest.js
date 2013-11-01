module('test Container model', {
	setup: function() {
		window.container = new PageLayout();
		
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
	var cont1 = container.getDescendant('1');
	var listener = listenTo(cont1);
	
	//move app 1_1 from position 0 to 1  
	cont1.addChild('1_1', {at : 1});
	//
	equal(listener.aCalled.length, 1);
	var args = listener.aCalled[0];
	equal(args[0].getId(), '1_1');
	equal(args[1].getId(), '1');
	equal(listener.rCalled.length, 0);
	
	var app1 = container.getDescendant('1_1');
	ok(app1, 'app1 should be not null');
	equal(app1.getParent().getId(), '1');
	equal(app1.getIndex(), 1);
});

test("move app to another container test", function() {
  var cont1 = container.getDescendant('1');
  var cont2 = container.getDescendant('2');
  
	var app1 = container.getDescendant('1_1');
	equal(app1.getParent().getId(), '1');
	equal(app1.getIndex(), 0);
	
	var app2 = container.getDescendant('2_2');
	equal(app2.getParent().getId(), '2');
	equal(app2.getIndex(), 1);
	
	//
	var listener1 = listenTo(cont1);
	var listener2 = listenTo(cont2);
	
	//move app 1_1 to container 2 at postion 1	
	cont2.addChild('1_1', {at : 1});
	equal(app1.getParent().getId(), '2');
	equal(app1.getIndex(), 1);
	
	//onRemoveChild event on cont1
	equal(listener1.rCalled.length, 1);
	equal(listener1.rCalled[0][0].getId(), '1_1');
	equal(listener1.aCalled.length, 0);
	//onAddChild event on cont2
	equal(listener2.aCalled.length, 1);
	equal(listener2.aCalled[0][0].getId(), '1_1');
	equal(listener2.rCalled.length, 0);
	
	//now app2 is in position 2
	equal(app2.getParent().getId(), '2');
	equal(app2.getIndex(), 2);
});

test("remove application test", function() {
  var cont1 = container.getDescendant('1');
  var listener = listenTo(cont1);
    
  cont1.removeChild('1_1');
  cont1.removeChild('1_2');
  ok(cont1.isEmpty());
  
  //
  equal(listener.rCalled.length, 2);
  var args = listener.rCalled[0];
  equal(args[0].getId(), '1_1');
  equal(args[1].getId(), '1');
  equal(listener.rCalled[1][0].getId(), '1_2');
  //
  equal(listener.aCalled.length, 0);  
});

test("test toJSON", function() {
	var data = container.toJSON();
	equal(data.children.length, 2);
	
	var cont1 = data.children[0];
	equal(cont1.id, 1);
	equal(cont1['children'].length, 2);
	equal(cont1['children'][0].id, '1_1');
	equal(cont1['children'][1].id, '1_2');
	
	var cont2 = data.children[1];
	equal(cont2.id, 2);
	equal(cont2['children'].length, 2);
	equal(cont2['children'][0].id, '2_1');
	equal(cont2['children'][1].id, '2_2');
});

test("test switchLayout", function() {
  //container order: #1 #2
  equal(container.getChild('1').getIndex(), 0);
  equal(container.getChild('2').getIndex(), 1);
  
  //new layout metadata with reverted container order
  var rLayout = new Object();
  rLayout.containers = ['2', '1'];
  
  container.switchLayout(rLayout);
  //
  equal(container.getChild('1').getIndex(), 1);
  equal('1_1', container.getChild('1').at(0).getId());
  equal('1_2', container.getChild('1').at(1).getId());
  //
  equal(container.getChild('2').getIndex(), 0);
  equal('2_1', container.getChild('2').at(0).getId());
  equal('2_2', container.getChild('2').at(1).getId());
  
  //new layout with 1 zone
  var oneZone = new Object();
  oneZone.containers = ['1'];
  
  //switch from reverted layout to 1 zone layout
  container.switchLayout(oneZone);

  equal(1, container.getChildren().length);
  equal(4, container.getChild("1").getChildren().length);
  equal('1_1', container.getChild('1').at(0).getId());
  equal('1_2', container.getChild('1').at(1).getId());
  equal('2_1', container.getChild('1').at(2).getId());
  equal('2_2', container.getChild('1').at(3).getId());
  
  var twoZone = new Object();
  twoZone.containers = ['1', '2'];
  container.switchLayout(twoZone);
  equal('1_1', container.getChild('1').at(0).getId());
  equal('1_2', container.getChild('1').at(1).getId());
  equal('2_1', container.getChild('2').at(0).getId());
  equal('2_2', container.getChild('2').at(1).getId());
	
	var threeZone = new Object();
	threeZone.containers = ['1', '2', '3'];
	container.switchLayout(threeZone);
	
	equal(3, container.getChildren().length);
	equal('1_1', container.getChild('1').at(0).getId());
	equal('1_2', container.getChild('1').at(1).getId());
	equal('2_1', container.getChild('2').at(0).getId());
	equal('2_2', container.getChild('2').at(1).getId());
	equal(true, container.getChild('3').isEmpty());
});

function listenTo(model) {
  var listener = {
    aCalled: [],
    rCalled: [],
    
    onAddChild: function() {
      listener.aCalled.push(arguments);
    },
    onRemoveChild: function() {
      listener.rCalled.push(arguments);
    }
  };
  model.on('container.addChild', listener.onAddChild);
  model.on('container.removeChild', listener.onRemoveChild);
  return listener;
}