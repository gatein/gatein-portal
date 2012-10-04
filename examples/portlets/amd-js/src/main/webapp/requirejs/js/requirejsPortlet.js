require(['highlight', 'mustache', 'text!/amd-js/requirejs/jsp/hello.mustache'], function($, mustache, template) {									
	
	$("body").on("click", ".requirejs-example button", function() {
		var portlet = $(this).closest(".requirejs-example"); 
		
		var name = portlet.find(".name").val();
		name = name == "" ? "world" : name;
		
		var output = mustache.render(template, {"name": name});
		portlet.find(".result").html(output);
	});
	
	$('.requirejs-example pre.code').highlight({source:1, zebra:1, indent:'space', list:'ol'});
	$("body").on("click", ".requirejs-example .nav-tabs li", function() {
		var jLi = $(this);
		var portlet = jLi.closest(".requirejs-example");
		
		portlet.find(".active").removeClass("active");
		jLi.addClass("active");
		
		var contentId = jLi.find("a").attr("class");
		
		portlet.find(".fade.in.active").removeClass("fade in active").addClass("fade");
		portlet.find("#" + contentId).removeClass("fade").addClass("fade in active");		
	});		
});	