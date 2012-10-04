<div class="jqueryPlugin" style="padding: 10px;">
  <h1>How to use a jQuery plugin with the built-in modular jQuery version in GateIn</h1>

  <div>The following is a very simple jQuery plugin /jqueryPlugin/jquery-plugin.js that we could take for this example.</div>
  <pre class="code" lang="js">
  (function($) {
    $.fn.doesPluginWork = function()
    {
      alert('YES, it works!');
    };
  })(jQuery);
  </pre>

  <br/>
  <div>First, we would configure it as a module of AMD depending to the shared built-in jQuery module</div>
  <pre class="code" lang="xml">
  &lt;module&gt;
      &lt;name&gt;jquery-plugin&lt;/name&gt;     
      &lt;script&gt;
         &lt;name&gt;jquery-plugin&lt;/name&gt;
         &lt;path&gt;/jqueryPlugin/jquery-plugin.js&lt;/path&gt;
      &lt;/script&gt;
      &lt;depends&gt;
        &lt;module&gt;jquery&lt;/module&gt;
        &lt;as&gt;$&lt;/as&gt;
      &lt;/depends&gt;
   &lt;/module&gt;
   
   &lt;portlet&gt;
     &lt;name&gt;JQueryPluginPortlet&lt;/name&gt;
     &lt;module&gt;
       &lt;script&gt;
         &lt;name&gt;jqueryPlugin&lt;/name&gt;
         &lt;path&gt;/jqueryPlugin/jqueryPluginPortlet.js&lt;/path&gt;
       &lt;/script&gt;
       &lt;depends&gt;
         &lt;module&gt;jquery&lt;/module&gt;
         &lt;as&gt;$&lt;/as&gt;
       &lt;/depends&gt;
       &lt;depends&gt;
       	&lt;module&gt;jquery-plugin&lt;/module&gt;
       &lt;/depends&gt;
     &lt;/module&gt;
   &lt;/portlet&gt;
  </pre>
  <br/>
  <div>
  		The above xml declaration make sure <strong>jquery</strong> and <strong>jquery-plugin</strong> have been load 
  		then $ is injected to portlet's JS code <br/>
  		Notice that we only need "$" as parameter, jquery plugin itself doesn't create something new, it only extend jquery  		   		  	
  </div>
  <pre class="code" lang="js">
   (function($) {
		$("body").on("click", ".jqueryPlugin .btn", function() {
			$(this).doesPluginWork();		
		});
	})($);
  </pre>
  <div>Click on this button <button class="btn">jQuery</button> to test plugin</div>  
</div>