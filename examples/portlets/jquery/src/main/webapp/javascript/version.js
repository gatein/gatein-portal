$(function() {
	$(document).delegate("#portletJQuery", "click", function() {
		$('#result').append("<p>The JQuery's version: " + $().jquery + "</p>");
		$('#result').children('p').fadeOut(3200);
	});
});

require(["SHARED/jquery"], function($) 
{
	$(document).on("click", "#gateinJQuery", function() 
	{
		$('#result').append("<p>The JQuery's version: " + $().jquery + "</p>");
		$('#result').children('p').fadeOut(3200);
	});
});
