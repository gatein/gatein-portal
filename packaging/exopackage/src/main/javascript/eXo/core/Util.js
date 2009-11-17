function Util() {

}

Util.prototype.shift = function(args) {
  if(args.length == 0) return args ;
  var newargs =  new Array() ;
  for(var i = 0; i < args.length - 1; i++)  newargs[i] = args[i + 1] ;
  return newargs ;
}

Util.prototype.modifyText = function(content, properties) {
  var i = properties.entrySet().iterator();
  while(i.hasNext()) {
    var entry = i.next() ;
    content = content.replace(entry.getKey(), entry.getValue()) ;
  }
  return content
}

eXo.core.Util = new Util() ;
