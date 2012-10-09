<div class="sample1" style="padding: 10px;">
  <h1>Showing how to create AMD module in GateIn portal</h1>
  <div>
    Beside of creating an AMD module in standard/native manner. GateIn also provides an internal scheme to build an AMD module,
    which is written in self-executing function pattern. And the returned value is exactly the AMD module you want to return.
  </div>
  <div>Here, we create a script file sample1/script.js at the webapp root folder</div>
  <pre class="code" lang="js">
  (function(){
    module = {};
    module.kickOff = function()
    {
      alert('Kick-off the module');
    }
    return module;
  })();
  </pre>
  <div>Configure it in gatein-resources.xml file as a *simple1* module</div>
  <pre class="code" lang="html">
   &lt;module&gt;
      &lt;name&gt;sample1&lt;/name&gt;     
      &lt;script&gt;
         &lt;path&gt;/sample1/script.js&lt;/path&gt;
      &lt;/script&gt;
   &lt;/module&gt;
  </pre>


  <h2>How to use a shared module ?</h2>
  <div>Above, we defined a shared module named 'SHARED/sample1', now we can use it in a standard way :</div>
  <pre class="code" lang="js">
  require(['SHARED/sample1'], function(m1) {
    m1.kickOff();
  });
  </pre>
  <div>Click here to execute above code snippet <button type="button" id="u_123" onclick="increase()">Execute script</button></div>
</div>
<script type="text/javascript">
<!--
  function increase() {
    require(['SHARED/sample1'], function(m1) {
      m1.kickOff();
    });
  }
//-->
</script>

<script type="text/javascript">
<!--
require(['SHARED/jquery', 'SHARED/highlight'], function($){
  $('.sample1 pre.code').highlight({source:1, zebra:1, indent:'space', list:'ol'});
});
//-->
</script>