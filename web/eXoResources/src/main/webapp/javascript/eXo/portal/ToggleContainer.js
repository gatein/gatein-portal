(function($) {

  eXo.portal.ToggleContainer  = {
    addContainer: function(containerId, toggleWidth) {
      if (!containerId || !containerId.length || !toggleWidth || !toggleWidth.length || toggleWidth.indexOf("%") > 0 ) {
        return;
      }
      
      toggleWidth = toggleWidth.split("px")[0];
      toggleWidth = parseInt(toggleWidth);
      if (toggleWidth == NaN)
      {
	return;
      }

      
      var checkSizeHandler = function(event) {
        var containerId = event.data.containerId;
        var toggleWidth = event.data.toggleWidth;
        checkSize(containerId, toggleWidth);
      }

      var checkSize = function(containerId, toggleWidth) {
        var container = $("#" + containerId);
        var currentWidth = container.outerWidth();
        if (currentWidth > toggleWidth) { 
          container.removeClass("ToggledRow");
        } else {
          container.addClass("ToggledRow");
        }
      }
       
      $(window).resize({"containerId": containerId, "toggleWidth": toggleWidth}, checkSizeHandler);
      checkSize(containerId,toggleWidth);

    }
  };
  
  return eXo.portal.ToggleContainer;
})($); 
