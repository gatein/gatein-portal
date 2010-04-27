package org.apache.struts.beanaction;

public class DefaultActionInterceptor implements ActionInterceptor {
  public String intercept(ActionInvoker invoker) {
    return invoker.invoke();
  }
}
