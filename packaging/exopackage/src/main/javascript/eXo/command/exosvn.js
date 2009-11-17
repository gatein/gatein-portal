eXo.require("eXo.core.TaskDescriptor") ;
eXo.require("eXo.core.Util") ;

function exosvn() {

};

function exosvnInstructions() {
  print(
   "\n" +
   "Use of the exosvn command: \n\n" +
   "  exosvn \n" +
   "           [add]\n" + 
   "           [rm]\n" +
   "\n\n" +
   "Options: \n" +
   "  * add          looks for new files in the project and adds them in svn\n" +
   "  * rm           looks for file to remove from svn\n"
  );
}

exosvn.prototype.Param = function(args) {
  var param = {} ;
  for(var i = 0; i < args.length; i++ ) {
    if(args[i].match("--include=")) {
      param.include = args[i].substring("--include=".length()) ;
    } else if(args[i].match("--exclude=")) {
      param.exclude = args.substring("--exclude=".length()) ;
    } else {
      param.workingDir =  args[i] ;
    }
  }
  return param ;
}

exosvn.prototype.MatchFile = function (param) {
 var matcher = {} ;
  matcher.proc = java.lang.Runtime.getRuntime().exec("svn st " + param.workingDir) ;
  matcher.files = new java.util.ArrayList() ;
  var din = new java.io.DataInputStream(matcher.proc.getInputStream() );
  
  while( (line=din.readLine()) != null ) {
    if(line.matches(param.include) && !line.matches(param.exclude)) {
      matcher.files.add(line) ;
      print(line);
    }
  }
  // waits for the process to end before returns
  while (matcher.proc.waitFor() != 0) {}
  return matcher ;
}

exosvn.prototype.AddTask = function(args) {
  var descriptor = new TaskDescriptor("svn add", "") ;

    descriptor.execute = function() {
      param = exosvn.Param(args) ;
      if(param.workingDir == null) param.workingDir = descriptor.workingDir ;
	  // includes all lines beginning with '?' and followed by any characters
      if(param.include == null) param.include = "^\\?.*" ;
	  // excludes the 'target' directory and files created by eclipse (.classpath; .settings; .project; .wtpmodules)
      if(param.exclude == null) param.exclude = ".*\.classpath$|.*\.settings$|.*\.project$|.*\.wtpmodules$|.*target$";
      java.lang.System.setProperty("user.dir",  param.workingDir) ;

      matches =  exosvn.MatchFile(param) ;
      proc =  matches.proc ;
      var exitValue = proc.exitValue();
      if(exitValue == 0 && matches.files.size() > 0) {
        var input = eXo.System.readInput("Do you want to add the above files[yes] ?") ;
        if("yes".equals(input)) {
          var b = new java.lang.StringBuilder() ;
          b.append("svn add ");
          for(i = 0; i < matches.files.size();i++) { 
            b.append(matches.files.get(i).substring(6)); 
          }
          proc = java.lang.Runtime.getRuntime().exec(b.toString()) ;
        }
      } else {
        print("Cannot perform the svn command, svn exit with the code " + exitValue); 
      }
      errStream = new java.io.DataInputStream( proc.getInputStream() );
      while( (line = errStream.readLine()) != null ) print(line) ;
    }
    return descriptor ;
}

//exosvn.prototype.UpdateTask = function(module) {
//  
//  var task =  new TaskDescriptor("svn update", module) ;
//  task.description = "Run svn update again module " + module ;
//
//  task.execute = function() {
//    java.lang.Runtime.getRuntime().exec("svn update " + module) ;
//    var args = ['svn', 'update', module] ;
//    print("Module: " + module) ;
//    var result = eXo.System.run(args, true, true) ;
//  } 
//  return task;
//}

exosvn.prototype.RemoveTask = function(args) {
  var descriptor = new TaskDescriptor("svn remove", "") ;
  
    descriptor.execute = function() {
      param = exosvn.Param(args) ;
      if(param.workingDir == null)  param.workingDir = descriptor.workingDir ;
      if(param.include == null) param.include = "^\\!.*" ;
      java.lang.System.setProperty("user.dir",  param.workingDir) ;

      matches =  exosvn.MatchFile(param) ;
      proc =  matches.proc ;
	  var exitValue = proc.exitValue();
      if(exitValue == 0 && matches.files.size() > 0) {
        var input = eXo.System.readInput("Do you want to remove the above files[yes] ?") ;
        if("yes".equals(input)) {
          var b = new java.lang.StringBuilder() ;
          b.append("svn rm ");
          for(i = 0; i < matches.files.size();i++) {
            b.append(matches.files.get(i).substring(6)); 
          }
          proc = java.lang.Runtime.getRuntime().exec(b.toString()) ;
        }
      } else {
        print("Cannot perform the svn command, svn  exit with the code " + exitValue); 
      }
      errStream = new java.io.DataInputStream( proc.getInputStream() );
      while( (line = errStream.readLine()) != null ) print(line) ;
    }
	
    return descriptor ;
}

eXo.command.exosvn = exosvn.prototype.constructor ;


var args = arguments;

if(args.length == 0) {
    exosvnInstructions() ;
    java.lang.System.exit(1);
} else {
	exosvn = new exosvn();

	var svncommand = args[0] ;
	args =  eXo.core.Util.shift(args) ;
	
	if ("add".equals(svncommand)) {
		exosvn.AddTask(args).execute() ;
	} else if ("rm".equals(svncommand)) {
		exosvn.RemoveTask(args).execute() ;
	}
}