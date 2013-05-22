(function($){

    init = function() {

    $(".gtnResponsiveMenu>.menu").dropdownmenu();

    $(".gtnResponsiveMenuCollapseButton").each(function(){

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

        });

    }

    return {init: init};

})(jQuery);

