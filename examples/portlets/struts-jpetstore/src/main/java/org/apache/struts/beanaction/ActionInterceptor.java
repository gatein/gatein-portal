package org.apache.struts.beanaction;

public interface ActionInterceptor {
  String intercept(ActionInvoker invoker);
}
