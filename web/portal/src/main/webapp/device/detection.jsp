<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"> 
<head>
  <meta name="viewport" content="initial-scale=1.0, maximum-scale=0, minimum-scale=1, height=device-height, width=device-width"/>
  <% String initialURI = (String)request.getAttribute("gtn.redirect.initialURI"); %>
  <% if (initialURI != null)
     { %>
       <meta http-equiv="REFRESH" content="2;url=<%=initialURI%>"/> <!-- Setting a 2 second delay. We want the javascript to submit the form and this is only added as a backup in case javascript is disabled or there is a problem.-->
  <% }
  %>
</head>
<body>
  <script type="text/javascript">

   var propertyMap = {};

   // ADD DEVICE PROPERTIES HERE 
   addParameter("screen.height", screen.height);
   addParameter("screen.width", screen.width);
   addParameter("window.devicePixelRatio", window.devicePixelRatio);
   addParameter("touch.enabled", touchEnabled());
   addParameter("window.innerWidth", window.innerWidth);

   
   //if it takes more than a half a second to load the logo image, then automatically submit without it. The reason why we want
   //to have the logo load first is because if the main portal site takes a long time to load, the user should be presented with something.
   //TODO: maybe just display the text 'Loading...' on the screen or something similar.
   setTimeout("submitParameters();", 500);


   function touchEnabled()
   {
     if ('ontouchstart' in window)
     {
       return true
     }
     else
     {
       return false;
     }
   }


   function addParameter(name, value)
   {
     propertyMap["gtn.device." + name] = value;
   }

   var submitted = false;

   function submitParameters ()
   {
     if (propertyMap != null && !submitted)
     {
       submitted = true;
       var form = document.createElement("form");
       form.setAttribute("method", "post");
       form.setAttribute("action", "<%= initialURI %>");
       
       for (key in propertyMap)
       {
          var input = document.createElement("input");
          input.setAttribute("type", "hidden");
          input.setAttribute("name", key);
          input.setAttribute("value", propertyMap[key]);
          form.appendChild(input);
       }
       document.body.appendChild(form);    
       form.submit();

     }
   }

  </script>

 <img src="/portal/device/loading.gif" onload="submitParameters()" style="display:block; margin-left:auto; margin-right:auto"/>
</body>
</html>
