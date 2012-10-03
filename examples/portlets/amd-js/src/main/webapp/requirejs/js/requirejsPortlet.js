require(['jquery', 'mustache', 'text!/amd-js/requirejs/jsp/hello.mustache'], function($, mustache, template) {									
	
	$(".requirejs-example button").on("click", function() {		
		var name = document.getElementById("name").value;
		name = name == "" ? "world" : name;
		
		var output = mustache.render(template, {"name": name});
		document.getElementById("result").innerHTML = output;
	});
	
});

/*require(['SHARED/highlight'], function($){
$('pre.code').highlight({source:1, zebra:1, indent:'space', list:'ol'});
});*/	