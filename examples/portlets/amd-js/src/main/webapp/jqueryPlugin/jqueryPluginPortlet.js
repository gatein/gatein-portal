(function($) {
	$("body").on("click", ".jqueryPlugin .btn", function() {
		$(this).doesPluginWork();		
	});

	$('.jqueryPlugin pre.code').highlight({
		source : 1,
		zebra : 1,
		indent : 'space',
		list : 'ol'
	});
})($);