(function(uiMaskWorkspace, jQuery) {
  var originalShow = eXo.portal.UIMaskWorkspace.show;
  eXo.portal.UIMaskWorkspace.show = function(maskId, width, height) {
     originalShow(maskId, width, height);
     jQuery("#UIWorkingWorkspace").addClass("background");
  };

  var originalHide = eXo.portal.UIMaskWorkspace.hide;
  eXo.portal.UIMaskWorkspace.hide = function(maskId) {
     originalHide(maskId);
     jQuery("#UIWorkingWorkspace").removeClass("background");
  };

})(uiMaskWorkspace, jQuery);
