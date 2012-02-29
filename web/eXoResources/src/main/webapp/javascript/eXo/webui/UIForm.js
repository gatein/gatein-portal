/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * Manages a form component
 */
eXo.webui.UIForm = {

  /**
   * This method is called when a HTTP POST should be done but in an AJAX case
   * some maniputalions are needed Once the content of the form is placed into a
   * string object, the call is delegated to the doRequest() method
   */
  ajaxPost : function(formElement, callback) {
    if (!callback)
      callback = null;
    var queryString = eXo.webui.UIForm.serializeForm(formElement);
    var url = formElement.action + "&ajaxRequest=true";
    doRequest("POST", url, queryString, callback);
  },

  /**
   * Get form element with pattern condition
   * 
   * @param {String}
   *          pattern The pattern can be Id#Id, example: Account#UIAccountForm
   */
  getFormElemt : function(pattern) {
    var ids = pattern.split("#");
    if(ids.length == 1)
    {
      return xj("#" + ids[0])[0];
    }
    else
    {
      return xj("#" + ids[0]).find("#" + ids[1])[0];
    }
  },

  /**
   * A function that submits the form identified by formId, with the specified
   * action If useAjax is true, calls the ajaxPost function, with the given
   * callback function Note: ie bug you cannot have more than one button tag
   */
  submitForm : function(formId, action, useAjax, callback) {
    if (!callback)
      callback = null;
    var form = this.getFormElemt(formId);
    // TODO need review try-cactch block for form doesn't use FCK
    try {
      if (FCKeditorAPI && typeof FCKeditorAPI == "object") {
        for ( var name in FCKeditorAPI.__Instances) {
          var oEditor;
          try {
            oEditor = FCKeditorAPI.__Instances[name];
            if (oEditor && oEditor.GetParentForm
                && oEditor.GetParentForm() == form) {
              oEditor.UpdateLinkedField();
            }
          } catch (e) {
            continue;
          }
        }
      }
    } catch (e) {
    }

    form.elements['formOp'].value = action;
    if (useAjax)
      this.ajaxPost(form, callback);
    else
      form.submit();
  },

  /**
   * Submits a form by Ajax, with the given action and the given parameters
   * Calls ajaxPost Note: ie bug you cannot have more than one button tag
   */
  submitEvent : function(formId, action, params) {
    var form = this.getFormElemt(formId);
    try {
      if (FCKeditorAPI && typeof FCKeditorAPI == "object") {
        for ( var name in FCKeditorAPI.__Instances) {
          var oEditor = FCKeditorAPI.__Instances[name];
          if (oEditor.GetParentForm && oEditor.GetParentForm() == form) {
            oEditor.UpdateLinkedField();
          }
        }
      }
    } catch (e) {
    }
    form.elements['formOp'].value = action;
    if (!form.originalAction)
      form.originalAction = form.action;
    form.action = form.originalAction + encodeURI(params);
    this.ajaxPost(form);
  },

  selectBoxOnChange : function(formId, elemt) {
    var tabs = xj(elemt).closest(".UISelectBoxOnChange").find("div.SelectBoxContentContainer").eq(0).children("div.SelectBoxContent").each(function(index)
    {
      if(index == elemt.selectedIndex)
      {
        xj(this).css("display", "block");
      }
      else
      {
        xj(this).css("display", "none");
      }
    });
  },
  /**
   * Sets the value (hiddenValue) of a hidden field (typeId) in the form
   * (formId)
   */
  setHiddenValue : function(formId, typeId, hiddenValue) {
    var form = document.getElementById(formId);
    if (form == null)
    {
      form = xj("#UIMaskWorkspace").find("#" + formId)[0];
    }
    form.elements[typeId].value = hiddenValue;
  },
  /**
   * Returns a string that contains all the values of the elements of a form
   * (formElement) in this format . fieldName=value The result is a string like
   * this : abc=def&ghi=jkl... The values are encoded to be used in an URL Only
   * serializes the elements of type : . text, hidden, password, textarea .
   * checkbox and radio if they are checked . select-one if one option is
   * selected
   */
  /*
   * This method goes through the form element passed as an argument and
   * generates a string output in a GET request way. It also encodes the the
   * form parameter values
   */
  serializeForm : function(formElement) {
    var queryString = "";
    var element;
    var elements = formElement.elements;

    this.addField = function(name, value) {
      if (queryString.length > 0)
        queryString += "&";
      queryString += name + "=" + encodeURIComponent(value);
    };

    for ( var i = 0; i < elements.length; i++) {
      element = elements[i];
      // if(element.disabled) continue;
      switch (element.type) {
      case "text":
      case "hidden":
      case "password":
      case "textarea":
        this.addField(element.name, element.value.replace(/\r/gi, ""));
        break;

      case "checkbox":
        if (element.checked)
          this.addField(element.name, "true");
        else
          this.addField(element.name, "false");
        break;
      case "radio":
        if (element.checked)
          this.addField(element.name, element.value);
        break;

      case "select-one":
        if (element.selectedIndex > -1) {
          this.addField(element.name,
              element.options[element.selectedIndex].value);
        }
        break;
      case "select-multiple":
        for ( var j = 0; j < element.options.length; j++) {
          if (element.options[j].selected)
            this.addField(element.name, element.options[j].value);
        }
        break;
      } // switch
    } // for
    return queryString;
  }
}