$(document).ready(function(){
	init();
});

function init() {
	// Bootstrap js
	bootstrapDropdown();
	bootstrapTooltip();
	bootstrapAlert();
	bootstrapPopover();
	bootstrapButton();
	bootstrapModal();
	bootstrapTypeahead();
	// Useful js
	button();
	toggleContent();
	showHideMore();
	radioBackground();
	clearInputTextValue();
	switchGroupView();
	selectGroupInHierarchicalView();
	// Provisional js
	selectPermission();
	accessPermissionButton();
	accessPermissionTable();
	feedback();
	editRedirect();
	onConditionModal();
	nodeMappingPlaceholder();
	// Useful js that makes other ones not work
	//sortable();
}

/* =========================
 * == ADD CONDITION MODAL ==
 * =========================
 */

function addUAString(element) {
	$(element).closest('td').append($('div.tt-ua').html());
	$(element).closest('td').children('div:last').children('input').focus();
	// set opacity to 0 so it keeps its space, and remove tooltip
	// $(element).css('opacity','0');
	// $(element).removeClass('tooltipTrigger');
	// $(element).removeAttr("data-original-title");
	// $(element).removeAttr('onclick');
	$(element).hide();
}

function removeUAString(element) {
	$(element).tooltip('hide');
	// $(element).closest('div.more-condition').prev().find('button.add-user-agent-string').css('opacity','1');
	// $(element).addClass('tooltipTrigger');
	// $(element).attr('onclick', 'addUAString(this);');
	parent = $(element).closest('div.more-condition').parent();
	$(element).closest('div.more-condition').remove();
	parent.children('div:last').find('button.add-user-agent-string').show();
}

function afterAddProperty() {
	$('.property-operator').change();
	$('.property-name-input').last().focus();
	// disable all which are hidden
	$('span[class^="pp-"]').filter(function() { return $(this).css("display") == "none"; }).children("div").children("input").prop('disabled', true);
}

function removeProperty(element) {
	$(element).tooltip('hide');
	parent = $(element).closest('tr.more-property').parent();
	$(element).closest('tr.more-property').remove();
	parent.children('tr:last').find('button.add-property').show();
}

function changePropertyFields(element, focus) {
	// Change input box(es) according to selected option
	$(element).next().children().each(function() {
		otherInput = $(this).children().children();
		//otherInput.val("");
		otherInput.prop('disabled', true);
		$(this).hide();
	});
	curInputWrapper = $(element).next().children('.pp-' + $(element).val());
	curInput = curInputWrapper.children().children();
	curInput.prop('disabled', false);
	curInputWrapper.show();
	if (focus) {
		curInput.first().focus();
	}
}

function editMappingEntry(elem) {
	$(elem).parent().parent().addClass('hidden-element');
	$(elem).parent().parent().nextAll('tr .hidden-element').first().removeClass('hidden-element');
	return false;
}

function deleteMappingEntry(elem) {
	$(elem).tooltip("hide");
	parentTR = $(elem).parent("td").parent("tr");
	if ($(parentTR).hasClass("node-mapping-viewer")) {
		spacerTR = $(parentTR).next("tr");
		editorTR = $(spacerTR).next("tr");
		$(parentTR).remove();
		$(spacerTR).remove();
		$(editorTR).remove();
	}
	else if ($(parentTR).hasClass("node-mapping-editor")) {
		if($(parentTR).prev("tr").hasClass("node-mapping-spacer")) {
			spacerTR = $(parentTR).prev("tr");
			editorTR = $(spacerTR).prev("tr");
			$(parentTR).remove();
			$(spacerTR).remove();
			$(editorTR).remove();
		}
		else {
			$(parentTR).remove();
		}
	}
	return false;
}

/**
 * Validates the condition name, so it's not left empty.
 */
function validateConditionName() {
	var conditionName = $("input[id$='condition_name']").val();
	if(!conditionName) {
		$("button[id$='save-condition-button']").attr("disabled", "disabled");
	}
	else {
		$("button[id$='save-condition-button']").removeAttr("disabled");
	}

}

function deleteRedirect(siteName, redirectName) {
	$('#delete-redirect-site-input').attr("value", siteName);
	$('#delete-redirect-site-text').text(siteName);
	$('#delete-redirect-name-input').attr("value", redirectName);
	$('#delete-redirect-name-text').text(redirectName);
	$('#modal-delete-redirect').modal();
}

function showAlert(title, message, aclass) {
	$('#main-alert-title').text(title);
	$('#main-alert-message').text(message);
	$('#main-alert').addClass(aclass);
	$('#main-alert').animate({
		opacity: 1
	}, 1000);

    if (window.hideAlertTimer) {
        clearTimeout(window.hideAlertTimer);
    }
    window.hideAlertTimer = setTimeout(function(){hideAlert(aclass);}, 3000);
}

function hideAlert(aclass) {
    window.hideAlertTimer = null;
	$('#main-alert').animate({
		opacity: 0
	}, 1000, function() {
        if (!window.hideAlertTimer) {
            $('#main-alert').removeClass(aclass);
            $('#main-alert-title').text("<undefined>");
            $('#main-alert-message').text("<undefined>");
        }
	});
}

function configureRedirect() {
	// FIXME: this is because the validation is not performed and last message still shows, we remove it manually
	$('.rdr-name-error').hide();
	$('.rdr-name-prev').removeClass('error-sibling');
	$('.rdr-edit-name').keyup();

	$('.add-redirect').css("visibility", "hidden");

	// fade summary out...
	$('#redirectSummaryWrapper').fadeOut(150, function() {
		// .. and when done, fade config in
		$('.edit-group').fadeIn(150, function() {
			sortable();
		});
	});
}

$(document).on('focusout', '.dropdown-menu a', function(e) {
	$(this).closest('.dropdown').removeClass('open');
});

$(document).on('keydown', '.site-link', function(e) {
	var code = e.keyCode || e.which;
	var shift = e.shiftKey;

	if (code == '9' && shift) {
		$(this).closest('.dropdown').removeClass('open');
	}
});

$(document).on('keyup', '.site-link,.space-link', function(e) {
	var code = e.keyCode || e.which;

	if (code == '9') {
		$(this).next('.dropdown-toggle').dropdown('toggle');
	}
});

/**
 * Enable usage of keys to toggle ON/OFF switches.
 */
$('.onoffswitch').live('keydown', function (e) {
	// make it work with space (32) and enter (13)
	if (e.keyCode === 32 || e.keyCode === 13) {
		e.stopImmediatePropagation();
		e.preventDefault();
		$(e.target).find("label").click();
	}
});

function onConditionModal() {
	$(document).on('shown', '#modal-condition', function () {
		$('.property-operator').each(function() {
			changePropertyFields(this, false);
		});
	});
}

function nodeMappingPlaceholder() {
	$(document).on("DOMSubtreeModified", "#node-mappings", function() {
		$(".origin-node-name-input").attr("placeholder", "e.g. homepage");
		$(".redirect-node-name-input").attr("placeholder", "e.g. home_page");
	});
}


// --- UNUSED (SO FAR) ---

// Enabling dropdown
bootstrapDropdown = function() {
	$('.dropdown-toggle').dropdown();
};

// Enabling tooltip
bootstrapTooltip = function() {
	// $('body') makes it live, works for future components
	$('body').tooltip({
		selector: '.tooltipTrigger'
	});
};

// Enabling/Disabling alert
bootstrapAlert = function() {
	$('.alert').alert();
	$('.alert button.close').click(function(){
		$(this).parent.parent.alert('hide');
	});
};

// Enabling popover
bootstrapPopover = function() {
	$('[rel=popover]').popover();
	$('[rel=popoverTop]').popover({
		placement: 'top'
	});
};

// Enabling radio buttons
bootstrapButton = function() {
	$('.radio-group').button();
};

// Modal
bootstrapModal = function() {
	$('#delete-site-space').click(function() {
		$('#modal-delete-site-space').modal();
	});
	$('.edit-permission').click(function() {
		$('#modal-permission').modal();
	});
	$('.delete-permission').click(function() {
		$('#modal-delete-permission').modal();
	});
	$('.delete-site').click(function() {
		// Set proper values for site to delete
		siteName = $(this).parent().parent().children("a").text().trim();
		$('#delete-site-id-input').attr("value", siteName);
		$('#delete-site-id-text').text(siteName);
		// Forcing dropdown to close.. should be automatic
		$(this).parent().parent().removeClass("open");
		$('#modal-delete-site').modal();
	});

	$('#create-group').click(function() {
		$('#modal-create-group').modal();
	});

	$('.modal button.close').click(function() {
		$(this).parent.parent.modal('hide');
	});
};

// Typeahead
bootstrapTypeahead = function() {
	$('.typeahead').typeahead();
};

// Do not move screen when clicking in a button
button = function(){
	$('button').click(function() {
		return false;
	});
};

// Toggle content
toggleContent = function() {
	$(document).on('click', '.toggle', function() {
		$(this).toggleClass('closed');
		$(this).parent().next().toggleClass('hidden-element');
	});
};

// Show / Hide More
showHideMore = function() {
	$(document).on('click', 'nav .more', function() {
		$(this).text($(this).text() == "Show more" ? "Hide more" : "Show more");
		$(this).parent().next('ul').find('.extra').toggleClass('hidden-element');
	});
};

//Sortable
sortable = function() {
	$(".sortable-rdr").sortable({
		start: function(event, ui) { $("[id$='sortFrom']").val(ui.item.index()); },
		update: function(event, ui) {
			$("[id$='sortTo']").val(ui.item.index());
			$("[id$='sortSite']").val($("[id$='sitename_text']").text());
			$("[id$='doSortRdrBtn']").click();
		}
	}).disableSelection();
	$(".sortable-cnd").sortable({
		start: function(event, ui) { $("[id$='sortFrom']").val(ui.item.index()); },
		update: function(event, ui) {
			$("[id$='sortTo']").val(ui.item.index());
			$("[id$='doSortCndBtn']").click();
		}
	}).disableSelection();
};


// Fixes footer at the bottom
footer = function() {
	var windowHeight = $(window).height();
	var containerRightHeight = $('#container-right').height();
	var footerFixed = windowHeight - containerRightHeight;
	if (footerFixed > 69){
		$('footer').addClass('fixed');
	}
};

// Background for selected item
radioBackground = function() {
	$('input[name=user-group]:checked').parent().addClass('active');
	$('input[name=user-group]').live("click", function () {
		$('input[name=user-group]').parent().removeClass('active');
		$('input[name=user-group]:checked').parent().addClass('active');
	});
};

// Clear input text value
clearInputTextValue = function() {
	$('.clear-input button').click(function() {
		$(this).parent().find('input').val('').focus();
	});
};

// Switch Group View
switchGroupView = function() {
	$('#list-view').click(function() {
		$(this).addClass('active');
		$('#tree-view').removeClass('active');
		$('.window-list').removeClass('hidden-element');
		$('.window-tree').addClass('hidden-element');
		return false;
	});
	$('#tree-view').click(function() {
		$(this).addClass('active');
		$('#list-view').removeClass('active');
		$('.window-tree').removeClass('hidden-element');
		$('.window-list').addClass('hidden-element');
		return false;
	});
};

// Select Group in Hierarchical View
selectGroupInHierarchicalView = function() {
/*
	$('.window-tree a').click(function() {
		$('.window-tree a').parent().removeClass('active');
		$(this).parent().addClass('active');
		$(this).parent().parent().children('li.parent').find('ul').addClass('hidden-element');
		$(this).parent().parent().find('li.parent').removeClass('opened');
	});
	$('.window-tree li.parent a').click(function() {
		$(this).next('ul').removeClass('hidden-element');
		$(this).parent('li.parent').addClass('opened');
	});
*/
	var ulHeight = $('.window-tree div ul').height();
	if (ulHeight > 375){
		$('.window-tree div ul').addClass('scroll');
	}
	$('.window-tree a').click(function() {
		$('.window-tree a').parent().removeClass('active');
		$(this).parent().addClass('active');
		return false;
	});
	$('.window-tree li.parent a').click(function() {
		$(this).parent().parent().parent().children('ul').removeClass('hidden-element');
		$(this).parent().parent().parent().next('div').children('ul').removeClass('hidden-element');
		$(this).parent('li.parent').addClass('opened');
	});
};


// STYLES BELOW HERE ARE PROVISIONAL


// Select permission in the list
selectPermission = function() {
	$('.select li span').click(function() {
		$(this).parent().parent().find('li').find('span').removeClass('selected');
		$(this).toggleClass('selected');
		$('#modal-permission .pull-right .select').addClass('enabled');
	});
	$('.select .second li span').click(function() {
		$(this).parent().parent().parent().parent().find('li').find('span').removeClass('selected');
		$(this).toggleClass('selected');
	});
	$('.select .third li span').click(function() {
		$(this).parent().parent().parent().parent().parent().parent().find('li').find('span').removeClass('selected');
		$(this).toggleClass('selected');
	});
	$('.select i').click(function() {
		$(this).toggleClass('icon-closed');
		$(this).toggleClass('icon-opened');
		$(this).next().next('ul').toggleClass('hidden-element');
	});
};

// Show and Hide button "Add Permission" in "Permission to Access"
accessPermissionButton = function() {
	$('#private').click(function() {
		$(this).parent().next('a').removeClass('hidden-element');
		$(this).parent().parent().find('.alert').removeClass('hidden-element');
	});
	$('#public').click(function() {
		$(this).parent().parent().find('a').addClass('hidden-element');
		$(this).parent().parent().find('table').addClass('hidden-element');
		$(this).parent().parent().find('.alert').addClass('hidden-element');
	});
};

// Show table in "Permission to Access"
accessPermissionTable = function() {
	$('#access-permission a').click(function() {
		$(this).parent().find('table').removeClass('hidden-element');
		$(this).parent().find('.alert').addClass('hidden-element');
	});
};

// Show Feedback
feedback = function() {
	$('.form-actions .btn-primary.save').click(function() {
		$('.alert-container.save').removeClass('hidden-element');
	});
};


// Redirect
editRedirect = function() {
	// Avoid showing summary and edit when edit is loaded. maybe show modal to confirm if there are changes made ?
	$('.site-link').live('click', function(){
		$(".edit-group").hide();
	});

	$('#modal-delete-redirect .btn-primary').click(function(){
		$(this).parent().parent().modal('hide');
		$('.alert-container').removeClass('hidden-element');
	});
};

/*
 * When on Redirect Configure view and the save changes or cancel button is pressed.
 * @ param save if there was a save invoked
 */
function closeRedirectEdit(save) {
	$('.edit-group').fadeOut(300, function() {
		$('#redirectSummaryWrapper').fadeIn(300);
		$('.add-redirect').css("visibility", "visible");
		if(save) {
			showAlert('Well Done!', 'The changes have been successfully saved.', 'alert-success');
		}
	});
}

function showNodeList() {
	$('#modal-select-node').modal();
	// Allow double click
	$('.radio-node').parent('label').dblclick(function() {
		selectNodeFromList();
	});
}

function selectNodeFromList() {
	var sNode = $(".radio-node:checked").attr("id");
	setTimeout(function() {
		nodeInput.focus();
		nodeInput.val(sNode);
		nodeInput.keyup();
	}, 50);
	$('#modal-select-node').modal('hide');
	return false;
}

/*
// Anchor animation

anchorAnimation = function() {

	$(document).ready(function() {
		$("a.anchorLink").anchorAnimate()
	});
	
	jQuery.fn.anchorAnimate = function(settings) {
	
		settings = jQuery.extend({
			speed : 500
		}, settings);	
		
		return this.each(function(){
			var caller = this
			$(caller).click(function (event) {	
				event.preventDefault()
				var elementClick = $(caller).attr("href")
				
				var position = $(elementClick).offset().top;
				var destination = position - 40;
				$("html:not(:animated),body:not(:animated)").animate({ scrollTop: destination}, settings.speed);
			})
		})
	}
	
};
*/

/* Modal Import Site */

