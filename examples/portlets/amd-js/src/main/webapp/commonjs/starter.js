(function(require, $) {
	var increOp = require("increment").increment;

   $("body").on("click", ".incre", function()
   {
     alert(increOp(100));
   });
})(require, $);
