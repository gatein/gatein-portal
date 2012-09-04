<div>
   <input type="button" id="incre" value="Incre"/>

   <script type="text/javascript">
      require(["require", "exports", "module", "SHARED/jquery", "math", "increment"], function(require, exports, module, $)
      {
         var increOp = require("increment").increment;

         $("#incre").on("click", function()
         {
           alert(increOp(100));
         });
      });
   </script>
</div>