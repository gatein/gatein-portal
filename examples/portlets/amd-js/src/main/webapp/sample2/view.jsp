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
  <div>First, we would configure it as a module of AMD depending to the shared built-in jQuery module</div>
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

  <div>Then we trigger to load the plugin somehow at least once for binding the plugin to the built-in jQuery instance. Here we rely on loading dependencies mechanism of RequireJS to load it</div>
  <pre class="code" lang="js">
  require(['SHARED/jquery', 'SHARED/jquery-plugin'], function($, plugin)
  {
    $(this).doesPluginWork();
  });
  </pre>
  <div>Click on this button <button onclick="onclick1()">jQuery</button> to execute above scripts</div>

  <p>
  <div>In addition, we can also return directly the instance of the built-in jQuery for the plugin module by assigning jQuery object to _module variable</div>
  <pre class="code" lang="js">
  (function($) {
    $.fn.doesPluginWork = function()
    {
      alert('YES, it works!');
    };
  })(jQuery);

  // Return the instance of jQuery for this jquery-plugin module
  _module = jQuery;
  </pre>
  <div>Then, what we have to do is just to require the plugin which actually return jQuery instance with the plugin added.</div>
  <pre class="code" lang="js">
  require(['SHARED/jquery-plugin'], function($)
  {
    $(this).doesPluginWork();
  });
  </pre>
  <div>Click on this button <button onclick="onclick2()">jQuery</button> to execute above scripts</div>
</div>


<script type="text/javascript">
<!--
  function onclick1() {
    require(['SHARED/jquery', 'SHARED/jquery-plugin'], function($, plugin) {
      $(this).doesPluginWork();
    });
  }

  function onclick2() {
    require(['SHARED/jquery-plugin'], function($) {
      $(this).doesPluginWork();
    });
  }

  require(['SHARED/highlight'], function($) {
    $('pre.code').highlight({
      source : 1,
      zebra : 1,
      indent : 'space',
      list : 'ol'
    });
  });
//-->
</script>

