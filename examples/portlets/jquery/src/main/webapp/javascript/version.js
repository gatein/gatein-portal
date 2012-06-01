$(".version").remove();

$("#portletJQuery").click(function()
{
  var target = $(this);
  $('#result').append("<p>The JQuery's version: " + $().jquery + "</p>");
  $('#result').children('p').fadeOut(3200);
});

$("#gateinJQuery").click(function()
{
  var target = $(this);
  gj('#result').append("<p>The JQuery's version: " + gj().jquery + "</p>");
  gj('#result').children('p').fadeOut(3200);
});
