(function(require, $) {
	var increOp = require("increment").increment;

   $("#incre").on("click", function()
   {
     alert(increOp(100));
   });
})(require, $);
