<div class="sample2" style="padding: 10px;">
  <h1>How to use a jQuery plugin with the built-in modular jQuery version in GateIn</h1>

  <div>The following is a very simple jQuery plugin /sample2/jquery-plugin.js that we could take for this example.</div>
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
         &lt;adapter&gt; 
           (function(jQuery) {           
              &lt;include&gt;/sample2/jquery-plugin.js&lt;/include&gt;
              return jQuery;
           })($);
         &lt;/adapter&gt;
      &lt;/script&gt;
      &lt;depends&gt;
        &lt;module&gt;jquery&lt;/module&gt;
        &lt;as&gt;$&lt;/as&gt;
      &lt;/depends&gt;
   &lt;/module&gt;
  </pre>
  <br/>
  <div>Then we trigger to load the plugin somehow at least once for binding the plugin to the built-in jQuery instance. Here we rely on loading dependencies mechanism of RequireJS to load it</div>
  <pre class="code" lang="js">
  require(['SHARED/jquery-plugin'], function($)
  {
    $(this).doesPluginWork();
  });
  </pre>
  <div>Click on this button <button onclick="onclick1()">jQuery</button> to execute above scripts</div>  
</div>
<script type="text/javascript">
<!--
  function onclick1() {
    require(['SHARED/jquery-plugin'], function($) {
      $(this).doesPluginWork();
    });
  }

  require(['SHARED/highlight'], function($) {
    $('.sample2 pre.code').highlight({
      source : 1,
      zebra : 1,
      indent : 'space',
      list : 'ol'
    });
  });
//-->
</script>