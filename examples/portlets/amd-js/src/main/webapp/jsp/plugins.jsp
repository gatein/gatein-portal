<div class="requirejs-plugins-example">
  <h2 style="margin-left:auto; margin-right:auto; width: 300px;">RequireJS plugin example</h2>
  <ul class="nav nav-tabs" id="myTab">
    <li class="active"><a data-toggle="tab" href="#text">Text - Mustache</a></li>
    <li><a data-toggle="tab" href="#sourceCode">Source Code</a></li>
  </ul>
  <div class="tab-content" id="myTabContent">
    <div id="text" class="tab-pane fade in active">
    	<h4 class="well">    		 
    		<a href="https://github.com/requirejs/text">"Text" - a requirejs plugin</a><br/> 
    		<a href="http://mustache.github.com/">"Mustache" - logic-less templates</a> 
    	</h4>
    	<div>Try it!</div>
    	<div style="float: left; margin-left:50px;">
    	  <input id="name" type="text" placeholder="Type your name..." />
		  <button type="button" class="btn" onclick="sayHello()">Say hello</button>
		</div>
		<div id="result" style="float: left; margin-left: 50px;"></div>
		<div style="clear: both;margin-top:51px;
				border-color: #DDDDDD #DDDDDD transparent; border-top-style: solid; border-top-width: 1px;">
			<ol>
				<li>
					Each js library should be a GateIn resource - declare it in gatein-resources.xml
					<pre class="code" lang="html">
	   &lt;scripts&gt;
	      &lt;name&gt;textPlugin&lt;/name&gt;     
	      &lt;script&gt;
	         &lt;name&gt;text&lt;/name&gt;
	         &lt;path&gt;/assets/js/plugins/text.js&lt;/path&gt;
	      &lt;/script&gt;
	   &lt;/scripts&gt;
	   
	   &lt;scripts&gt;
	      &lt;name&gt;mustacheTemplate&lt;/name&gt;     
	      &lt;script&gt;
	         &lt;name&gt;mustache&lt;/name&gt;
	         &lt;path&gt;/assets/js/plugins/mustache.js&lt;/path&gt;
	      &lt;/script&gt;
	   &lt;/scripts&gt;
					</pre>
					<b>Note:</b>
					<ul>
						<li>
							These js libs are already AMD modules, they have to be declared as GateIn's IMMEDIATE 
							script resources or native scripts
						</li>
						<li>
							Each lib should be declared in separate GateIn resources or that lib need to specify its module name<br/> 
							<pre class="code" lang="js">
	   define('Text', ['module'], function (module) {	
	   	//Text plugin code
	   });
							</pre>
						</li>
					</ul> 						
				</li>
				<li>					
					Load these libraries using Gatein's resourceID instead of normal names ("Text" or "Mustache") <br/>
					<pre class="code" lang="js">
	   require(['SHARED/mustacheTemplate', 'SHARED/textPlugin!tmpl.mustache'],function(...) {
	   	//Code that use template
	   });
					</pre>
				</li>
			</ol>
		</div>		
    </div>
    <div id="sourceCode" class="tab-pane fade">
    <ul>
    	<li>Mustache template
    		<pre class="code" lang="html">
	   Hello {{name}}!
			</pre>
    	</li>
    	<li>Html
    		<pre class="code" lang="html">
	   &lt;div&gt;
    	  &lt;input id="name" type="text" placeholder="Type your name..."/&gt;
		  &lt;button type="button" class="btn" onclick="sayHello()"&gt;Say hello&lt;/button&gt;
		&lt;/div&gt;
		&lt;div id="result"&gt;&lt;/div&gt;
			</pre>
    	</li>
    	<li>Javascript
    		<pre class="code" lang="js">
	   function sayHello() {
		  require(['SHARED/mustacheTemplate', 'SHARED/textPlugin!/amd-js/jsp/hello.mustache'], 
			function(mustache, template){									
				var name = document.getElementById("name").value;
				name = name == "" ? "world" : name;
				
				var output = mustache.render(template, {"name": name});
				document.getElementById("result").innerHTML = output;
			});
		}
			</pre>
    	</li>
    </ul>    		      				
    </div>
  </div>
</div>
<script type="text/javascript">
	function sayHello() {
		require(['SHARED/mustacheTemplate', 'SHARED/textPlugin!/amd-js/jsp/hello.mustache'], 
			function(mustache, template){									
				var name = document.getElementById("name").value;
				name = name == "" ? "world" : name;
				
				var output = mustache.render(template, {"name": name});
				document.getElementById("result").innerHTML = output;
			});
	}	
	
	require(['SHARED/highlight'], function($){
	  $('pre.code').highlight({source:1, zebra:1, indent:'space', list:'ol'});
	});
</script>