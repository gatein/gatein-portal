/*
 * jQuery accordion pane plugin for responsive community portlet.
 */
(function($){

    init = function() {

    $.fn.accordion = function(options) {

        var settings = $.extend({}, {
            accordionGroupSelector: ".accordion-group",
            accordionToggleSelector: ".accordion-toggle",
            accordionBodySelector: ".accordion-body"
        }, options);

        var accordion = $(this).children(settings.accordionGroupSelector);

        accordion.each(function(){
            toggleLink = $(this).children(settings.accordionToggleSelector).first();
            toggleLink.click(function(){
                $(this).siblings(settings.accordionBodySelector).first().slideDown();
                $(this).parent().siblings(settings.accordionGroupSelector).children(settings.accordionBodySelector).each(function(){$(this).slideUp()});
            });
        });
    };

    };

    return {init: init};

})(jQuery);