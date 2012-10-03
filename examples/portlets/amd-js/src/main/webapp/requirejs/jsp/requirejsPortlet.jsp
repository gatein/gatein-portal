<div class="requirejs-example">
  <h2 style="margin-left:auto; margin-right:auto; width: 300px;">RequireJS example</h2>
  <ul class="nav nav-tabs" id="myTab">
    <li class="active"><a data-toggle="tab" href="#text">Text - Mustache</a></li>
    <li><a data-toggle="tab" href="#sourceCode">Source Code</a></li>
  </ul>
  <div class="tab-content" id="myTabContent">
    <div id="text" class="tab-pane fade in active">
    	<div>This example show how to use NATIVE requirejs lib in GateIn</div>
    	<h4 class="well">    		 
    		<a href="https://github.com/requirejs/text">"Text" - a requirejs plugin</a><br/> 
    		<a href="http://mustache.github.com/">"Mustache" - logic-less templates</a> 
    	</h4>
    	<h4>Try it!</h4>
    	<div style="float: left; margin-left:50px;">
    	  <input id="name" type="text" placeholder="Type your name..." />
		  <button type="button" class="btn">Say hello</button>
		</div>
		<div id="result" style="float: left; margin-left: 50px;"></div>
		<div style="clear: both;margin-top:51px;
				border-color: #DDDDDD #DDDDDD transparent; border-top-style: solid; border-top-width: 1px;">
			<ol>
				<li>
					Each js library should be a GateIn resource - declare it in gatein-resources.xml
					<pre class="code" lang="html">
                 &lt;module&gt;
                    &lt;name&gt;text&lt;/name&gt;     
                    &lt;script&gt;
                       &lt;name&gt;requirejs.text&lt;/name&gt;
                       &lt;path&gt;/requirejs/js/plugins/text.js&lt;/path&gt;
                    &lt;/script&gt;
                    &lt;depends&gt;
   	                 &lt;module&gt;module&lt;/module&gt;
                    &lt;/depends&gt;
                 &lt;/module&gt;


                 &lt;portlet&gt;
                   &lt;name&gt;RequireJSPortlet&lt;/name&gt;
                   &lt;module&gt;
                      &lt;script&gt;
                         &lt;name&gt;starter&lt;/name&gt;
                         &lt;path&gt;/requirejs/js/requirejsPortlet.js&lt;/path&gt;
                      &lt;/script&gt;
                      &lt;depends&gt;
       	                &lt;module&gt;jquery&lt;/module&gt;
       	                &lt;as&gt;jquery&lt;/as&gt;
                      &lt;/depends&gt;
                      &lt;depends&gt;
                         &lt;module&gt;mustache&lt;/module&gt;
                         &lt;as&gt;mustache&lt;/as&gt;
                      &lt;/depends&gt;
                      &lt;depends&gt;
                         &lt;module&gt;text&lt;/module&gt;
                         &lt;as&gt;text&lt;/as&gt;
                         &lt;resource&gt;/amd-js/requirejs/jsp/hello.mustache&lt;/resource&gt;
                      &lt;/depends&gt;
                    &lt;/module&gt;
                 &lt;/portlet&gt;
					</pre>
					<b>Note:</b>
					<ul>
						<li>
							<strong>"text.js"</strong> is not only a native requirejs lib, but also a requirejs-plugin. We use <strong>&lt;resource&gt;</strong> tag 
							to declare a requirejs-plugin resource
						</li>
						<li>
							Notice how we use <strong>&lt;depends&gt;</strong> tag to declare dependencies - 
							They must be indentical to the dependencies array of the native script
						</li>
					</ul> 						
				</li>
				<li style="margin-top: 10px">					
				   When you work with native script, alias should be declared equal to the module name in the dependency list.
				   If you have code like this<br/> 
							<pre class="code" lang="js">
	   define('foo', ['mustache'], function (mst) {	
	   	//code use mustache
	   });
							</pre>
					
					You will need to config it in the gatein-resources.xml<br/>
					<pre class="code" lang="html">
                 &lt;module&gt;
                    &lt;name&gt;foo&lt;/name&gt;     
                    &lt;script&gt;
                       &lt;name&gt;foo&lt;/name&gt;
                       &lt;path&gt;/path/foo.js&lt;/path&gt;
                    &lt;/script&gt;
                    &lt;depends&gt;
   	                 &lt;module&gt;mustache&lt;/module&gt;
   	                 &lt;as&gt;mustache&lt;/as&gt;
                    &lt;/depends&gt;
                 &lt;/module&gt;	   
					</pre>
					Then portal will resolve <strong>"mustache"</strong> module and injected it with the name equal to dependency list<br/>
					If you don't have <strong>&lt;as&gt;</strong> declared, it will be injected with default name ("SHARED/mustache") 
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
		  require(['mustache', 'text!/amd-js/jsp/hello.mustache'], 
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