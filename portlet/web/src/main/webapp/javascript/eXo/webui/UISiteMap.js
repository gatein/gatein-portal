/*
 * Copyright (C) 2010 eXo Platform SAS.
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

function initSitemapPortlet(id)
{
  require(['SHARED/jquery'], function($){
    $("#" + id).on("click", "div.ExpandIcon,div.CollapseIcon", function(event) {
      collapseExpand(this);

      var input = $(this).children("input");
      if (input.attr("name") == "collapseURL") {
        ajaxAsyncGetRequest(input.val(), true);
      } else if (input.attr("name") == "expandURL") {
        updateTreeNode(this, input.val());
      }
      event.stopPropagation();
    });


    function collapseExpand(node)
    {
      var jqNode = $(node);
      var subGroup = jqNode.parent().children("div.ChildrenContainer");
      if(subGroup.css("display") == "none")
      {
        if(jqNode.hasClass("ExpandIcon"))
        {
          jqNode.attr("class", "CollapseIcon ClearFix");
        }
        subGroup.css("display", "block");
      }
      else
      {
        if(jqNode.hasClass("CollapseIcon"))
        {
          jqNode.attr("class", "ExpandIcon ClearFix");
        }
        subGroup.css("display", "none");
      }
    };

    function updateTreeNode(nodeToUpdate, getNodeURL)
    {
      if (!nodeToUpdate || !getNodeURL)
      {
        return;
      }
      var jqNode = $(nodeToUpdate);
      var subGroup = jqNode.parent().children("div.ChildrenContainer");
      if (subGroup.length == 0 || $.trim(subGroup.html()) !== "")
      {
        return;
      }
      var jsChilds = ajaxAsyncGetRequest(getNodeURL, false);
      try
      {
        var data = $.parseJSON(jsChilds);
      }
      catch (e)
      {
      }
      if (data && data.length)
      {
        var html = "";
        for(var i = 0; i < data.length; i++)
        {
          html += toHtml(data[i], i == data.length - 1);
        }
        subGroup.html(html);
        subGroup.show();
      }
      else
      {
        jqNode.removeClass("CollapseIcon").addClass("NullItem");
      }
    };

    /**
     * A recursive method which generates HTML fragment associated with node - an object created from JSON data.
     *
     * @param node
     * @param isLast
     * @return {*}
     */
    function toHtml(node, isLast)
    {
      if(!node)
      {
        return;
      }
      var nodeLink = node.actionLink ? node.actionLink : "javascript:void(0);";

      var fragment = "";
      var nodeCSS = isLast? "LastNode Node" : "Node";
      if(node.hasChild)
      {
        fragment += "<div class='" + nodeCSS + "'>";
        if(node.isExpanded)
        {
          fragment += "<div class='CollapseIcon ClearFix'>";
          fragment += "<input type='hidden' name='collapseURL' value='" + node.collapseURL + "'/>";
          fragment += "<a class='NodeIcon DefaultPageIcon' href='" + nodeLink + "'>" + node.label + "</a>";
          fragment += "</div><div class='ChildrenContainer' style='display: block'>";
          for (var i = 0; i < node.childs.length; i++)
          {
            fragment += toHtml(node.childs[i], i == node.childs.length - 1);
          }
        }
        else
        {
          fragment += "<div class='ExpandIcon ClearFix'>";
          fragment += "<input type='hidden' name='expandURL' value='" + node.getNodeURL + "'/>"
          fragment += "<a class='NodeIcon DefaultPageIcon' href='" + nodeLink + "'>" + node.label + "</a>";
          fragment += "</div><div class='ChildrenContainer' style='display: none'>";
          for (var i = 0; i < node.childs.length; i++)
          {
            fragment += toHtml(node.childs[i], i == node.childs.length - 1);
          }
        }
        fragment += "</div></div>";
      }
      else
      {
        fragment += "<div class='" + nodeCSS + " ClearFix'><div class='NullItem'><div class='ClearFix'>";
        fragment += "<a class='NodeIcon DefaultPageIcon' href='" + nodeLink + "'>" + node.label + "</a></div></div></div>";
      }
      return fragment;
    };

  });
}