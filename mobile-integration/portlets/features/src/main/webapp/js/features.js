(function ($) {

    var init = function() {
        function svg2png(elementImage){
            if(elementImage.size() > 0){
                var srcSvg = elementImage.attr("src");
                var srcPng = srcSvg.slice(0, -3) + "png";
                elementImage.attr("src", srcPng);
            }
        }

        if (!window.Modernizr.svg) {
            svg2png($(".ssoFeature > img"));
            svg2png($(".nuiFeature > img"));
            svg2png($(".psFeature > img"));
            svg2png($(".pbFeature > img"));
            svg2png($(".ugmFeature > img"));
            svg2png($(".asdFeature > img"));
            svg2png($(".wsrpFeature > img"));
        }
    };
    return {init: init};

})(jQuery);