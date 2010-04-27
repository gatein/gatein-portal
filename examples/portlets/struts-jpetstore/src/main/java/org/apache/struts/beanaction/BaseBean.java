package org.apache.struts.beanaction;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.validator.ValidatorActionForm;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * All actions mapped through the BeanAction class should be mapped
 * <p/>
 * to a subclass of BaseBean (or have no form bean mapping at all).
 * <p/>
 * <p/>
 * <p/>
 * The BaseBean class simplifies the validate() and reset() methods
 * <p/>
 * by allowing them to be managed without Struts dependencies. Quite
 * <p/>
 * simply, subclasses can override the parameterless validate()
 * <p/>
 * and reset() methods and set errors and messages using the ActionContext
 * <p/>
 * class.
 * <p/>
 * <p/>
 * <p/>
 * <i>Note:  Full error, message and internationalization support is not complete.</i>
 * <p/>
 * <p/>
 * <p/>
 * Date: Mar 12, 2004 9:20:39 PM
 *
 * @author Clinton Begin
 */
public abstract class BaseBean extends ValidatorActionForm {

  private ActionInterceptor interceptor;

  protected BaseBean() {
    this.interceptor = new DefaultActionInterceptor();
  }

  protected BaseBean(ActionInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public final void reset(ActionMapping mapping, ServletRequest request) {
    ActionContext.initCurrentContext((HttpServletRequest) request, null);
    reset();
  }

  public final void reset(ActionMapping mapping, HttpServletRequest request) {
    ActionContext.initCurrentContext((HttpServletRequest) request, null);
    reset();
  }

  public void reset() {
  }

  public ActionInterceptor getInterceptor() {
    return interceptor;
  }
}
