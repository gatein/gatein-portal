/*
 * jQuery drop-down menu plugin for responsive navigation portlet interface. 
 * Features:
 *  - Basic on-click drop-down support.
 *  - Dynamically respond to window changes, distributes menus not to be outside the window.
 *  - Marking the currently opened node for better orientation.
 */
(function($){
    $.fn.dropdownmenu = function(options) {

        var settings = $.extend({}, {
            // Selector for the menu-element.
            menuElement: ".menuitem",
            // Selector for the to-be-collapsed element inside this parent element.
            arrowElement: ".menuhandler",
            // Class which is set to the to-be-collapsed element when it's collapsed.
            collapsedClass: "close",
            // Selector for the submenu element.
            submenuElement: ".submenu",
            // Class which is set to the menu which does not fit on screen.
            inverseSubmenuClass: "inverse",
            // Class which is set to the last opened menuitem.
            lastOpenedClass: "current"
        }, options);

        // Remember the topmenu element to be able to check if submenu fits into resized window
        var topmenu = $(this);

        // Traverse opened submenus from parent menu and inverse them if needed
        function findAndCheckOpenedSubmenu(parentMenu){
            var openedSubmenu = parentMenu.children(settings.menuElement+":not(."+settings.collapsedClass+")").children(settings.submenuElement).first();

            // if no submenu is found, do nothing
            if (openedSubmenu.offset() == null) {
                return;
            }

            // Check whether the submenu fits into the window
            checkMenuFit(openedSubmenu);
            
            findAndCheckOpenedSubmenu(openedSubmenu);
        }

        function checkMenuFit(submenuElement){
            var screenWidth = $(window).width();

            // If no submenu is opened, do nothing
            if (submenuElement.offset() == null)
                return;

            // Put the submenu into its default state
            submenuElement.removeClass(settings.inverseSubmenuClass);

            // Get the width and left offset of the submenut in its default state
            var elementWidth = submenuElement.outerWidth(true);
            var handlerLeft = submenuElement.offset().left;

            // If the submenu overflows over right border of page, move it to the left / reverse it (apply inverse CSS)
            if ((elementWidth + handlerLeft > screenWidth) || (handlerLeft < 0)) {
                if (!submenuElement.hasClass(settings.inverseSubmenuClass))
                    submenuElement.addClass(settings.inverseSubmenuClass);
            }
        }

        function openSubmenu(menuItem) {
            // Close sibling submenus so that only the submenu opened above is present
            menuItem.siblings().each(function() {
                $(this).children().remove(settings.submenuElement);

                if (!$(this).hasClass(settings.collapsedClass)) {
                    $(this).addClass(settings.collapsedClass);
                }
            });

            // Find the newly opened submenu
            var submenuElement = menuItem.children(settings.submenuElement);

            // Check whether the newly opened submenu fits into the window
            checkMenuFit(submenuElement);
        }

        // Check wether the submenu fits into the resized window
        $(window).resize(function(){            
            // Traverse from topmenu through opened submenus and inverse them if needed
            findAndCheckOpenedSubmenu(topmenu);
        });

        function submenuOpenAction(actionItem) {
            var menuItem = actionItem.parent(settings.menuElement);
            menuItem.toggleClass(settings.collapsedClass);

            if (menuItem.children(settings.submenuElement).length === 0) {
                $.ajax({
                    type: "POST",
                    url: actionItem.attr('href').substring(1),
                    cache: false,
                    dataType: "text",
                    success: function(data) {
                        menuItem.append(data);
                        openSubmenu(menuItem);
                    },
                    error: function(XMLHttpRequest, textStatus, errorThrown) {
                        console && console.log("Ajax error");
                    }
                });
            } else {
                menuItem.children().remove(settings.submenuElement);
            }
        }

        /* Apply the onClick event function for each menu handler (arrow)
         * Thanks to the usage of the on function, this is applied even to
         * the content loaded by ajax and inserted to the dom in the future.
         */
        topmenu.children(settings.menuElement).each(function(){

            $(this).on("click", settings.arrowElement, function(e) {
                submenuOpenAction($(this));
            });

            $(this).on("hover", settings.submenuElement + " > " + settings.menuElement, function(e) {
                var hasChildren = $(this).children(settings.arrowElement).length > 0;
                var isExpanded = topmenu.css("z-index") == 1;
                if (hasChildren && isExpanded){
                    submenuOpenAction($(this).children(settings.arrowElement));
                }
            });

        });

    };

})(jQuery);