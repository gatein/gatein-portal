(function($){

         var collapsibleElements = new Array();

         init = function() {
             calculateCollapse();
             $(window).resize(checkCollapse);
             $(window).on("orientationchange", calculateCollapse);
         }

         function calculateCollapse(){
            $(".collapsibleRow").each(function(){
                $(this).toggleClass("collapsed", false);
                $(this).toggleClass("expanded", false);
                var collapseWidth = 0;
                $(this).children().each(function() {

                    if (!$(this).hasClass('gtnResponsiveMenuCollapseButton'))
                    {
                        collapseWidth += $(this)[0].scrollWidth + $(this).outerWidth(true) - $(this).outerWidth(false) ;
                    }
                });

                collapsibleElements.push({"element" : this, "collapseWidth" : collapseWidth});
                $(this).toggleClass("expanded",true);
            });
            checkCollapse();
        }

        function checkCollapse()
        {
            for (var i = 0; i < collapsibleElements.length; i++)
            {
                var collapsibleElement = collapsibleElements[i]["element"];
                var collapseWidth = collapsibleElements[i]["collapseWidth"];
                if($(collapsibleElement)[0].scrollWidth ? $(collapsibleElement)[0].scrollWidth < collapseWidth : $(collapsibleElement).outerWidth() < collapseWidth)
                {
                    $(collapsibleElement).toggleClass("collapsed", true);
                    $(collapsibleElement).toggleClass("expanded", false);
                }
                else
                {
                    $(collapsibleElement).toggleClass("collapsed", false);
                    $(collapsibleElement).toggleClass("expanded", true);
                }
            }
        }

        return {init: init};

})(jQuery);
