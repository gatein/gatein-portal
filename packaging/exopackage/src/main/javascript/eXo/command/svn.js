eXo.require("eXo.core.TaskDescriptor") ;
eXo.require("eXo.core.Util") ;

function svn() {

};


svn.prototype.UpdateTask = function(module) {
  
  var task =  new TaskDescriptor("svn update", module) ;
  task.description = "Run svn update on module " + module ;

  task.execute = function() {
    var args = ['svn', 'update', module] ;
    print("Module: " + module) ;
    // Runs the command
    var result = eXo.System.run(args, true, true) ;
  } 
  return task;
}

eXo.command.svn = svn.prototype.constructor ;