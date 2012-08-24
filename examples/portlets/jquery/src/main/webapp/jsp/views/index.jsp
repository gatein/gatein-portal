<div style=" width: 98,5%; height: 253px; overflow: hidden;">
<div style="float: left; width: 70%">
<ul>
  <li>
    <p style="color: blue">Get information on portlet 's own JQuery use:</p>
    <pre>$('#result').append("&lt;p&gt;The JQuery's version: " + $().jquery + "&lt;/p&gt;");</pre>
  	<pre>$('#result').children('p').fadeOut(3200);</pre>
    <div id="portletJQuery"><span style="color:green">Click here</span></div>
  </li>
  <li>
    <p style="color: blue">Get information on GateIn 's own JQuery use:</p>
    <pre>require(["SHARED/jquery"], function($) {</pre>
    <pre>  $('#result').append("&lt;p&gt;The JQuery's version: " + $().jquery + "&lt;/p&gt;");</pre>
    <pre>  $('#result').children('p').fadeOut(3200);</pre> 
    <pre>});</pre>
    <div id="gateinJQuery"><span style="color:green">Click here</span></div>
  </li>
</ul>
</div>
<div id="result" style="float: right; width: 30%"><p></p></div>
</div>
