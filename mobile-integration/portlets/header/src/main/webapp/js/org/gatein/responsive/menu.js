(function($){
$(document).ready(function()
		{

	$(".menucategory").each(function(){
		
		var menuelement = $(this);
		
		var initialstate = "close";
		
		if ($(this).hasClass("open"))
		{
			intialstate = "open";
		}
		else if (!$(this).hasClass("close"))
		{
			$(this).addClass("close");
		}
		
		if (initialstate == "open")
		{
			$(this).children(".menuarrow").toggle(function(){closeMenu(menuelement);}, function(){openMenu(menuelement);});
			$(this).children(".menu").toggle(function(){closeMenu(menuelement);}, function(){openMenu(menuelement);});
		}
		else
		{
			$(this).children(".menuarrow").toggle(function(){openMenu(menuelement);}, function(){closeMenu(menuelement);});
			$(this).children(".menu").toggle(function(){closeMenu(menuelement);}, function(){openMenu(menuelement);});
		}

		
	});
	
	function openMenu(menuelement)
	{
		$(menuelement).toggleClass("open", true);
		$(menuelement).removeClass("close");
	}
	
	function closeMenu(menuelement)
	{
		$(menuelement).toggleClass("close", true);
		$(menuelement).removeClass("open");
	}
});
})(jQuery);
