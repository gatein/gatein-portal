(function($){


    $(document).ready(function() {

        var collapsibleElements = [];
        
        //setup the collapse button
        $(".collapseButton").each(function(){

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

        
        
        calculateCollapse();
        $(window).resize(checkCollapse);     
        $(window).on("orientationchange", calculateCollapse);


        function calculateCollapse(){
            $(".collapsibleRow").each(function(){
                $(this).toggleClass("collapsed", false);
                $(this).toggleClass("expanded", false);
                var collapseWidth = 0;
                $(this).children().each(function() {

                    if (!$(this).hasClass('collapseButton'))
                    {
                        collapseWidth += $(this)[0].scrollWidth; //use scroll width in case the content is rendered outside of the current screen (especially since this should be white-space nowrap)
                    }
                });

                collapsibleElements.push({"element" : this, "collapseWidth" : collapseWidth});

            });
            checkCollapse();
        }
        
        
        function checkCollapse()
        {
            for (var i = 0; i < collapsibleElements.length; i++)
            {
                var collapsibleElement = collapsibleElements[i]["element"];
                var collapseWidth = collapsibleElements[i]["collapseWidth"];
                if ($(collapsibleElement).outerWidth() < collapseWidth)
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
    });

})(jQuery);
