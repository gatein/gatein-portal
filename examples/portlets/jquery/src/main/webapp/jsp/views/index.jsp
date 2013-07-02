<div style="float: left; width: 70%">
<ul>
  <li>
    <p style="color: blue">Get information on portlet 's own JQuery use:</p>
    <pre>$(function() {</pre>
    <pre>  $(document).delegate("#portletJQuery", "click", function() {</pre>
    <pre>    //bind to document so that events presist over ajax page reloads</pre>
    <pre>    //delegate since jQuery 1.6 doesn't include on()</pre>
    <pre>    $('#result').append("&lt;p&gt;The JQuery's version: " + $().jquery + "&lt;/p&gt;");</pre>
    <pre>    $('#result').children('p').fadeOut(3200);</pre>
    <pre>  });</pre>
    <pre>});</pre>
    <div id="portletJQuery"><span style="color:green">Click here</span></div>
  </li>
  <li>
    <p style="color: blue">Get information on GateIn 's own JQuery use:</p>
    <pre>require(["SHARED/jquery"], function($) {</pre>
    <pre>  $(document).on("click", "#gateinJQuery", function() {</pre>
    <pre>    //bind to document so that events presist over ajax page reloads</pre>
    <pre>    $('#result').append("&lt;p&gt;The JQuery's version: " + $().jquery + "&lt;/p&gt;");</pre>
    <pre>    $('#result').children('p').fadeOut(3200);</pre>
    <pre>  });</pre> 
    <pre>});</pre>
    <div id="gateinJQuery"><span style="color:green">Click here</span></div>
  </li>
</ul>
</div>
<div id="result" style="float: right; width: 30%"><p></p></div>
