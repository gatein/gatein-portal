(function($){


	$(document).ready(function()
			{
		var collapsibleElements = [];

		$(".collapsibleRow").each(function(){

			if (!$(this).cWidth) 
			{
				var cWidth = 0;
				$(this).children().each(function() {

					if ($(this).hasClass('collapseButton'))
					{
						$(this).click(function(){
							if ($(this).data("target") && $(this).data("target-class") && $(this).data("action") == "toggleCSS")
							{
								$($(this).data("target")).toggleClass($(this).data("target-class"));
							}
							
							if ($(this).data("self-class") && $(this).data("action") == "toggleCSS")
							{
								$(this).toggleClass($(this).data("self-class"));
							}
						});
					}


					//TODO: do we really need to add this here? Or can we force this to be set originally in the css?
					var originalWhiteSpace = $(this).css("white-space");
					$(this).css("white-space", "nowrap");
					cWidth += $(this).outerWidth();
					$(this).css("white-space", originalWhiteSpace);
				});

				var object = {"element" : this, "cWidth" : cWidth};

				collapsibleElements.push(object);
			}

		});

		//check the size right now
		checkSize();
		//make sure its check whenever the screen is resized
		$(window).resize(checkSize);

		function checkSize()
		{
			for (var i = 0; i < collapsibleElements.length; i++)
			{
				var collapsibleElement = collapsibleElements[i]["element"];
				var cWidth = collapsibleElements[i]["cWidth"];
				console.log($(collapsibleElement).outerWidth() + " < " + cWidth + " : " + ($(collapsibleElement).outerWidth() < cWidth));
				if ($(collapsibleElement).outerWidth() < cWidth)
				{
					$(collapsibleElement).toggleClass("collapsed", true);
					$(collapsibleElement).toggleClass("expanded", false);
					console.log("Need to collapse : " + i + " : " + $(collapsibleElement));
				}
				else 
				{
					$(collapsibleElement).toggleClass("collapsed", false);
					$(collapsibleElement).toggleClass("expanded", true);
				}
			}
		}

			});

})(jQuery);