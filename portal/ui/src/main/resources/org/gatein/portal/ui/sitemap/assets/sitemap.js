require(['jquery'], function($) {
  $(document).ready(function() {
    (function(){
      var sitemapContent = $('.sitemapUnit').find('.sitemapContent');
      sitemapContent.children('.node:last').removeClass().addClass('lastNode node');
      sitemapContent.find('.childrenContainer').each(function(){
        $(this).children('.node:last').removeClass().addClass('lastNode node');
      });
      sitemapContent.find('.node').each(function(){
        $(this).children('.expandItem').click(function(event) {
          $(this).next('.childrenContainer').slideToggle('slow');
          $(this).toggleClass('collapseItem');
        })
      });
    })();
  });
});