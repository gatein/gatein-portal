eXo.System = {
  verbose :false,

  addSystemClasspath : function(url) {
    var sysClassLoader = java.lang.ClassLoader.getSystemClassLoader();
    var argTypes = [ java.net.URL.getClass() ];
    method = java.net.URLClassLoader.getClass().getDeclaredMethod("addURL", argTypes);
    method.setAccessible(true);
    for (i = 0; i < url.length; i++) {
      var args = new Array();
      args[0] = url[i];
      method.invoke(sysClassLoader, args);
    }
  },

  info : function(tag, message) {
    if (message == null) {
      message = tag;
      tag = "INFO";
    }
    java.lang.System.out.print("[" + tag + "]");
    for ( var i = tag.length + 2; i < 10; i++)
      java.lang.System.out.print(" ");

   java.lang.System.out.println(message);
//    var tmp = message.split("\n");
//    java.lang.System.out.println(tmp[0]);
//    for ( var j = 1; j < tmp.length; j++) {
//      java.lang.System.out.println("         " + tmp[j]);
//    }
  },

  error : function(message) {
    this.info("ERROR", message);
  },

  vinfo : function(tag, message) {
    if (this.verbose)
      this.info(tag, message);
  },

  print : function(message) {
    java.lang.System.out.print(message);
  },

  vprint : function(message) {
    if (this.verbose)
      java.lang.System.out.print(message);
  },

  printIndentation : function() {
    print("          ");
  },

  vprintIndentation : function() {
    if (this.verbose)
      print("          ");
  },

  run : function(args, printResult, printError) {
    var proc = java.lang.Runtime.getRuntime().exec(args);
    var din = new java.io.DataInputStream(proc.getInputStream());
    var b = new java.lang.StringBuilder();
    var line = null;
    while ((line = din.readLine()) != null) {
      b.append(line).append('\n');
      if (printResult)
        print(line);
    }

    if (printError) {
      var errStream = new java.io.DataInputStream(proc.getErrorStream());
      while ((line = errStream.readLine()) != null)
        print(line);
    }

    return b.toString();
  },

  readInput : function(message) {
    java.lang.System.out.print(message + ": ");
    var b = new java.lang.StringBuilder();
    var systemin = java.lang.System['in'];
    while (true) {
      var val = systemin.read();
      if (val == 13)
        continue;
      if (val == 10)
        break;
      b.appendCodePoint(val);
    }
    return b.toString();
  }
}
