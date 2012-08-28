<div style="padding: 10px;">
  <h1>How to use a jQuery plugin with the built-in modular jQuery version in GateIn</h1>

  <p>
  <div>The following is a very simple jQuery plugin /sample2/jquery-plugin.js that we could take for this example.</div>
  <pre class="code" lang="js">
  (function($) {
    $.fn.doesPluginWork = function()
    {
      alert('YES, it works!');
    };
  })(jQuery);
  </pre>

  <p>
  <div>Configure it as a module of AMD depending to the shared built-in jQuery module</div>
  <pre class="code" lang="xml">
  &lt;module&gt;
    &lt;name&gt;jquery-plugin&lt;/name&gt;
    &lt;script&gt;
      &lt;name&gt;jquery-plugin&lt;/name&gt;
      &lt;path&gt;/sample2/jquery-plugin.js&lt;/path&gt;
    &lt;/script&gt;
    &lt;depends&gt;
      &lt;module&gt;jquery&lt;/module&gt;
      &lt;as&gt;jQuery&lt;/as&gt;
    &lt;/depends&gt;
  &lt;/module&gt;
  </pre>

  <div>Now to use the jQuery with the added plugin, we have to trigger loading the plugin with the built-in jQuery at least once</div>
  <pre class="code" lang="js">
  require(["SHARED/jquery", "SHARED/jquery-plugin"], function($, plugin)
  {
    $(this).doesPluginWork();
  });
  </pre>
  <div>Click <a href="#" onclick="onclick1();">here</a> to execute above scripts</div>
</div>


<script type="text/javascript">
<!--
  function onclick1() {
    require([ "SHARED/jquery", "SHARED/jquery-plugin" ], function($, plugin) {
      $(this).doesPluginWork();
    });
  }

  require([ 'SHARED/highlight' ], function($) {
    $('pre.code').highlight({
      source : 1,
      zebra : 1,
      indent : 'space',
      list : 'ol'
    });
  });
//-->
</script>

