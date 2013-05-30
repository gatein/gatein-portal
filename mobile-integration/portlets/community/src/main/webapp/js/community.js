(function ($) {
    init = function() {
    var loadWithAjax = function(feed){

        var loader = feed.children(".ajaxLoader").first();
        loader.click(function(){return false;});
        var url = loader.attr("href");

        $.ajax({
            type: "POST",
            url: url,
            cache: false,
            dataType: "text",
            success: function (data) {
                feed.append(data);
                loader.remove();
            }
        });
    };

    var feedBlog = $(".gtnResponsiveCommunityPortlet #blog-content-mobile");
    loadWithAjax(feedBlog);

    var feedTwitter = $(".gtnResponsiveCommunityPortlet #tweets-content-mobile");
    loadWithAjax(feedTwitter);

    $(".gtnResponsiveCommunityPortlet #conversation-accordion").accordion();

    };
    return {init: init};

})(jQuery);