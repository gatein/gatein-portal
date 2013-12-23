/*
 * jQuery drop-down menu plugin for responsive navigation portlet interface. 
 * Features:
 *  - Basic on-click drop-down support.
 *  - Dynamically respond to window changes, distributes menus not to be outside the window.
 *  - Marking the currently opened node for better orientation.
 */
(function($){

    init = function() {
  
    $.fn.dropdownmenu = function(options) {

        var settings = $.extend({}, {
            // Selector for the menu-element.
            menuItem: ".menucategory",
            // Selector for the to-be-collapsed element inside this parent element.
            arrowElement: ".menutoggle",
            // Class which is set to the to-be-collapsed element when it's collapsed.
            hiddenClass: "close",
            // Class when the menu is open and should be visible
            visibleClass: "open",
            // Selector for the submenu element.
            menuElement: ".menu",
            // Class which is set to the menu which does not fit on screen.
            inverseSubmenuClass: "inverse",
            // Class which is set to the last opened menuitem.
            lastOpenedClass: "current"
        }, options);

        // Remember the topmenu element to be able to check if submenu fits into resized window
        var topmenu = $(this);

        // Traverse opened submenus from parent menu and inverse them if needed
        function findAndCheckOpenedSubmenu(parentMenu){
            var openedSubmenu = parentMenu.children(settings.menuItem+":not(."+settings.visbileClass+")").children(settings.menuElement).first();

            // if no submenu is found, do nothing
            if (openedSubmenu.offset() == null) {
                return;
            }

            // Check whether the submenu fits into the window
            checkMenuFit(openedSubmenu);
            
            findAndCheckOpenedSubmenu(openedSubmenu);
        }

        function checkMenuFit(menuElement){
            var screenWidth = $(window).width();

            // If no submenu is opened, do nothing
            if (menuElement.offset() == null)
                return;

            // Put the submenu into its default state
            menuElement.removeClass(settings.inverseSubmenuClass);

            // Get the width and left offset of the submenut in its default state
            var elementWidth = menuElement.outerWidth(true);
            var handlerLeft = menuElement.offset().left;

            // If the submenu overflows over right border of page, move it to the left / reverse it (apply inverse CSS)
            // TODO: handle case where there is also not enough room on the other side...
            if ((elementWidth + handlerLeft > screenWidth) || (handlerLeft < 0)) {
                if (!menuElement.hasClass(settings.inverseSubmenuClass))
                    menuElement.addClass(settings.inverseSubmenuClass);
            }
        }


        function openSubmenu(menuItem) {
            // Close sibling submenus so that only the submenu opened above is present
            menuItem.siblings().each(function() {
               // $(this).children().remove(settings.menuElement);

                if ($(this).hasClass(settings.visibleClass)) {
                    $(this).removeClass(settings.visibleClass);
                    $(this).addClass(settings.hiddenClass);
                }
            });

            // Find the newly opened submenu
            //var menuElement = menuItem.children(settings.menuElement);

            // Check whether the newly opened submenu fits into the window
            // checkMenuFit(menuElement);
        }

        // Check wether the submenu fits into the resized window
        $(window).resize(function(){            
            // Traverse from topmenu through opened submenus and inverse them if needed
            findAndCheckOpenedSubmenu(topmenu);
        });

        // open or close the menu when the user clicks the button
        function menuToggleAction(actionItem) {

            var menuCategory = actionItem.parent(settings.menuItem);            

            // if the current menu is closed, then open it
            if (!menuCategory.hasClass(settings.visibleClass))
            {
                menuCategory.removeClass(settings.hiddenClass);
                menuCategory.addClass(settings.visibleClass);
                updateMenu(actionItem);
                openSubmenu(menuCategory);
                checkMenuFit(menuCategory.children(settings.menuElement));
            }
            else //the menu is currently open, close it
            {
               menuCategory.removeClass(settings.visibleClass);
               menuCategory.addClass(settings.hiddenClass);
            }
        }

        function updateMenu(actionItem) {
                var menuCategory = actionItem.parent(settings.menuItem);
                if (!menuCategory.attr("gtn.ajax.fetching") && menuCategory.children(settings.menuElement).length === 0 && actionItem.attr('href'))
                {
                 menuCategory.attr("gtn.ajax.fetching", true);
                 $.ajax({
                    type: "POST",
                    url: actionItem.attr('href').substring(1),
                    cache: false,
                    dataType: "text",
                    success: function(data) {
                        menuCategory.append(data);
                        menuCategory.attr("gtn.ajax.fetching", false);
                    },
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        console && console.log("Ajax error");
                    }
                });
                }
                checkMenuFit(menuCategory.children(settings.menuElement));
        }

        /* Apply the onClick event function for each menu handler (arrow)
         * Thanks to the usage of the on function, this is applied even to
         * the content loaded by ajax and inserted to the dom in the future.
         */
        topmenu.children(settings.menuItem).each(function(){
            $(this).on("touchstart", settings.arrowElement, function(e){
                menuToggleAction($(this));
                //hack to specify that this element has been touched or not
                //Needed since some older mobile browser call will continue the
                //events chain even with a return false.
                e.target.gtnResponsiveTouch = true;
                return false;
            });

            // if a click is done, we explicitly set open/close the menu
            $(this).on("click", settings.arrowElement, function(e) {
                if(! e.target.gtnResponsiveTouch)
                {
                    menuToggleAction($(this));
                }
                e.target.gtnResponsiveTouch = null;
            });

            //if using a mouse, handle hover support
            //Note: this will only fetch the node data via ajax, it will not change the state of the node
            $(this).on("mouseover", settings.arrowElement, function(e){
                if(! e.target.gtnResponsiveTouch)
                {
                    updateMenu($(this));
                }
            });
            // For keyboard compatibily
            // TODO: there is an issue with focusin/focusout, so mouseover event couldn't be simulated with keyboard
            // This is a workaround to simulate click() event with mouse for header navigation
            $(this).on("keypress", settings.arrowElement, function(e){
                if (e.keyCode == 13) {
                  $(this).click();
                }
            });
        });

    };
    }

    return {init: init};

})(jQuery);
