/*
 * TODO: Dummy package containing future jquery dropdown menu plugin
 */
(function($){
    $.fn.dropdownmenu = function(options) {

        var settings = $.extend({}, {
            // Selector for the menu-element.
            menuElement: ".menuitem",
            // Selector for the to-be-collapsed element inside this parent element.
            arrowElement: ".menuhandler",
            // Class which is set to the to-be-collapsed element when it's collapsed.
            collapsedClass: "close",
            inverseSubmenuClass: "inverse"
        }, options);

        // Apply the onClick event function for each menu handler (arrow)
        $(this).find(settings.arrowElement).each(function(){
            
            $(this).click(function(){
                var screenWidth = $(document).width();
                
                // Open the submenu under the menu handler
                $(this).parent(settings.menuElement).toggleClass(settings.collapsedClass);
                                
                // Close sibling submenus so that only the submenu opened above is present
                $(this).parent(settings.menuElement+":not(."+settings.collapsedClass+")").siblings().each(function(){
                    $(this).toggleClass(settings.collapsedClass, true);
                })
                
                // Check if the element overflows
                var submenuElement = $(this).parent(settings.menuElement).children(".submenu");                
                var elementWidth = submenuElement.outerWidth(true);
                var elementLeft = submenuElement.offset().left;
                
                // If the submenu overflows over right border of page, move it to the left (apply inverse CSS)
                if (elementWidth + elementLeft > screenWidth) {
                    submenuElement.toggleClass(settings.inverseSubmenuClass);
                }                
            });          
            
            
        });

    };
})(jQuery);