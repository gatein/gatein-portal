(function($){

    init = function() {

    $(".gtnResponsiveMenu>.menu").dropdownmenu();

    $(".gtnResponsiveMenuCollapseButton").each(function(){

            $(this).click(function(){
                var dataTarget = $(this).children(".data.target").first();
                var dataTargetClass = $(this).children(".data.target.class").first();
                var dataAction = $(this).children(".data.action").first();
                var dataSelfClass = $(this).children(".data.self.class").first();

                if (dataTarget && dataTargetClass && dataAction.val() == "toggleCSS")
                {
                    $(dataTarget.val()).toggleClass(dataTargetClass.val());
                }

                if (dataSelfClass && dataAction.val() == "toggleCSS")
                {
                    $(this).toggleClass(dataSelfClass.val());
                }
            });

        });

    }

    return {init: init};

})(jQuery);

