<div class="sample1" style="padding: 10px;">
  <h1>Showing how to use AMD module in GateIn portal</h1>

  <h2>Define a shared immediately loaded module</h2>
  <div>Create a script file assets/js/foo.js at the webapp root folder</div>
  <pre class="code" lang="js">alert('hello world');</pre>
  <div>Configure it in gatein-resources.xml file</div>
  <pre class="code" lang="html">
   &lt;module&gt;
      &lt;name&gt;foo&lt;/name&gt;     
      &lt;script&gt;
         &lt;name&gt;foo&lt;/name&gt;
         &lt;path&gt;/assets/js/foo.js&lt;/path&gt;
      &lt;/script&gt;
   &lt;/module&gt;
  </pre>


  <h2>How to use a shared module ?</h2>
  <div>Let's say we want to use shared JQuery module to increase the number in this button <button type="button" id="u_123" onclick="increase()">Increase Me! 1</button></div>
  <pre class="code" lang="js">
  var counter = 1;
  function increase() {
    require([ 'SHARED/jquery' ], function($) {
      $('#u_123').html('Increase Me! ' + ++counter);
    });
  }
  </pre>
</div>
<script type="text/javascript">
<!--
  var counter = 1;
  function increase() {
    require([ 'SHARED/jquery' ], function($) {
      $('#u_123').html('Increase Me! ' + ++counter);
    });
  }
//-->
</script>

<script type="text/javascript">
<!--
require(['SHARED/highlight'], function($){
  $('.sample1 pre.code').highlight({source:1, zebra:1, indent:'space', list:'ol'});
});
//-->
</script>