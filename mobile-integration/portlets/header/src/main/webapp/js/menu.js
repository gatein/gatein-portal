(function($){

    init = function() {

    $(".gtnResponsiveMenu>.menu").dropdownmenu();

    $(".gtnResponsiveMenuCollapseButton").each(function(){

            $(this).click(function() {
                var dataTarget = $(this).children(".data.target").length > 0 ? $(this).children(".data.target").first() : null;
                var dataTargetClass = $(this).children(".data.target.class").length > 0 ? $(this).children(".data.target.class").first() : null;
                var dataAction = $(this).children(".data.action").length > 0 ? $(this).children(".data.action").first() : null;
                var dataSelfClass = $(this).children(".data.self.class").length > 0 ? $(this).children(".data.self.class").first() : null;

                if (dataAction) {
                    if (dataTarget && dataTargetClass && dataAction.val() == "toggleCSS") {
                        $(dataTarget.val()).toggleClass(dataTargetClass.val());
                    }
                    if (dataSelfClass && dataAction.val() == "toggleCSS") {
                        $(this).toggleClass(dataSelfClass.val());
                    }
                }
            });

        });

    }

    return {init: init};

})(jQuery);

