TaskDescriptor = function (taskName, wd) {
  this.name =  taskName ;
  this.description = "" ;
  this.workingDir =  wd ;
  this.message = "" ;
  this.executionTime = 0 ;

  this.banner = function () {
    print("--------------------------- RUNNING TASK ------------------------------------") ;
    print("TASK           : " + this.name) ;
    print("DESCRIPTION    : " + this.description) ;
    print("WORKING DIR    : " + this.workingDir) ;
    print("-----------------------------------------------------------------------------") ;
  }

  this.execute = function () {
    print("You Need to override this method") ;
  }

  this.report = function() {
    print("-----------------------------------------------------------------------------") ;    
    print("TASK           : " + this.name) ;
    print("DESCRIPTION    : " + this.description) ;
    print("WORKING DIR    : " + this.workingDir) ;
    print("MESSAGE        : " + this.message) 
    print("EXECUTION TIME : " + (this.executionTime/1000) + "s") ;
    print("-----------------------------------------------------------------------------\n") ;
  }
}

/*
var task1 = new TaskDescriptor("task1", "target");
var task2= new TaskDescriptor("task2", "target");

task2.execute = function() {
  print("execute task 2") ;
}

var tasks = [task1, task2] ;
print("\n");

for(var i = 0; i < tasks.length; i++) {
  tasks[i].banner();
  tasks[i].execute();
  tasks[i].report();
}
*/
