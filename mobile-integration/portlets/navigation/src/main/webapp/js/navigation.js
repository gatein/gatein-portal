(function($){
    $(".gtnResponsiveNavigationPortlet .topmenu").dropdownmenu();

    $(".gtnResponsiveNavigationPortlet .collapsibleToggle").click( function(){
        visibleClass = "visible-element";
        $(this).toggleClass(visibleClass);
        $(".gtnResponsiveNavigationPortlet .topmenu").toggleClass(visibleClass);
    });
    
})(jQuery);