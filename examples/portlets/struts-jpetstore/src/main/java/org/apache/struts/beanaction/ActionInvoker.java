package org.apache.struts.beanaction;

import java.lang.reflect.Method;

public class ActionInvoker {
  private Method method;
  private BaseBean bean;

  public ActionInvoker(BaseBean bean, Method method) {
    this.method = method;
    this.bean = bean;
  }

  public String invoke() {
    try {
      return (String) method.invoke(bean, (Object []) null);
    } catch (Exception e) {
      throw new BeanActionException("Error invoking Action.  Cause: " + e, e);
    }
  }
}
