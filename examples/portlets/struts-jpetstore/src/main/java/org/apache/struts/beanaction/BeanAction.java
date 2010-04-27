package org.apache.struts.beanaction;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * BeanAction is an extension to the typical Struts Action class that
 * <p/>
 * enables mappings to bean methods.  This allows for a more typical
 * <p/>
 * Object Oriented design where each object has behaviour as part of
 * <p/>
 * its definition.  Instead of writing separate Actions and Forms,
 * <p/>
 * BeanAction allows you to simply have a Bean, which models both
 * <p/>
 * the state and the methods that operate on that state.
 * <p/>
 * <p/>
 * <p/>
 * In addition to the simpler packaging, BeanAction also simplifies the
 * <p/>
 * Struts progamming paradigm and reduces dependency on Struts.  Using
 * <p/>
 * this pattern could allow easier migration to newer frameworks like JSF.
 * <p/>
 * <p/>
 * <p/>
 * The method signatures are greatly simplified to the following
 * <p/>
 * <pre>
 * <p/>
 * public String myActionMethod() {
 * <p/>
 *   //..work
 * <p/>
 *   return "success";
 * <p/>
 * }
 * <p/>
 * </pre>
 * <p/>
 * The return parameter becomes simply the name of the forward (as defined
 * <p/>
 * in the config file as usual).  Form parameters, request, response, session,
 * <p/>
 * attributes, and cookies are all accessed via the ActionContext class (see the
 * <p/>
 * ActionContext javadocs for more).
 * <p/>
 * <p/>
 * <p/>
 * The forms that you map to a BaseAction mapping must be a subclass of the
 * <p/>
 * BaseBean class.  BaseBean continues to simplify the validation and
 * <p/>
 * reset methods by removing the parameters from the signature as was done with
 * <p/>
 * the above action method example.
 * <p/>
 * <p/>
 * <p/>
 * There are 3 ways to map a BeanAction in the struts configuration file.
 * <p/>
 * They are as follows.
 * <p/>
 * <p/>
 * <p/>
 * <B>URL Pattern</B>
 * <p/>
 * <p/>
 * <p/>
 * This approach uses the end of the action definition to determine which
 * <p/>
 * method to call on the Bean.  For example if you request the URL:
 * <p/>
 * <p/>
 * <p/>
 * http://localhost/jpetstore4/shop/viewOrder.do
 * <p/>
 * <p/>
 * <p/>
 * Then the method called would be "viewOrder" (of the mapped bean as specified
 * <p/>
 * by the name="" parameter in the mapping below).  The mapping used for this
 * <p/>
 * approach is as follows.
 * <p/>
 * <pre>
 * <p/>
 *  &lt;action path="/shop/<b>viewOrder</b>" type="org.apache.struts.beanaction.BeanAction"
 * <p/>
 *    name="orderBean" scope="session"
 * <p/>
 *    validate="false"&gt;
 * <p/>
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 * <p/>
 *  &lt;/action&gt;
 * <p/>
 * </pre>
 * <p/>
 * <p/>
 * <p/>
 * <B>Method Parameter</B>
 * <p/>
 * <p/>
 * <p/>
 * This approach uses the Struts action parameter within the mapping
 * <p/>
 * to determine the method to call on the Bean.  For example the
 * <p/>
 * following action mapping would cause the "viewOrder" method to
 * <p/>
 * be called on the bean ("orderBean").  The mapping used for this
 * <p/>
 * approach is as follows.
 * <p/>
 * <pre>
 * <p/>
 *  &lt;action path="/shop/viewOrder" type="org.apache.struts.beanaction.BeanAction"
 * <p/>
 *    <b>name="orderBean" parameter="viewOrder"</b> scope="session"
 * <p/>
 *    validate="false"&gt;
 * <p/>
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 * <p/>
 *  &lt;/action&gt;
 * <p/>
 * </pre>
 * <p/>
 * <B>No Method call</B>
 * <p/>
 * <p/>
 * <p/>
 * BeanAction will ignore any Struts action mappings without beans associated
 * <p/>
 * to them (i.e. no name="" attribute in the mapping).  If you do want to associate
 * <p/>
 * a bean to the action mapping, but do not want a method to be called, simply
 * <p/>
 * set the parameter to an asterisk ("*").  The mapping used for this approach
 * <p/>
 * is as follows (no method will be called).
 * <p/>
 * <pre>
 * <p/>
 *  &lt;action path="/shop/viewOrder" type="org.apache.struts.beanaction.BeanAction"
 * <p/>
 *    <b>name="orderBean" parameter="*"</b> scope="session"
 * <p/>
 *    validate="false"&gt;
 * <p/>
 *    &lt;forward name="success" path="/order/ViewOrder.jsp"/&gt;
 * <p/>
 *  &lt;/action&gt;
 * <p/>
 * </pre>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * TO-DO List
 * <p/>
 * <ul>
 * <p/>
 * <li> Ignore mappings to methods that don't exist.
 * <p/>
 * </ul>
 * <p/>
 * </p>
 * <p/>
 * <B>A WORK IN PROGRESS</B>
 * <p/>
 * <p/>
 * <p/>
 * <i>The BeanAction Struts extension is a work in progress.  While it demonstrates
 * <p/>
 * good patterns for application development, the framework itself is very new and
 * <p/>
 * should not be considered stable.  Your comments and suggestions are welcome.
 * <p/>
 * Please visit <a href="http://www.ibatis.com">http://www.ibatis.com</a> for contact information.</i>
 * <p/>
 * <p/>
 * <p/>
 * Date: Mar 11, 2004 10:03:56 PM
 *
 * @author Clinton Begin
 * @see BaseBean
 * @see org.apache.struts.beanaction.ActionContext
 */
public class BeanAction extends Action {
  private static final String NO_METHOD_CALL = "*";
  private static final String SUCCESS_FORWARD = "success";

  public final ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
    String forward = SUCCESS_FORWARD;
    try {
      if (!(form instanceof BaseBean)) {
        if (form != null) {
          throw new BeanActionException("The form for mapping '" + mapping.getPath() + "' named '" + mapping.getName() + "' was not an instance of BaseBean.  BeanAction requires an BaseBean instance.");
        } else {
          throw new BeanActionException("The form for mapping '" + mapping.getPath() + "' named '" + mapping.getName() + "' was null.  BeanAction requires an BaseBean instance.");
        }
      }
      BaseBean bean = (BaseBean) form;
      ActionContext.initCurrentContext(request, response);
      if (bean != null) {
        // Explicit Method Mapping

        Method method = null;
        String methodName = mapping.getParameter();
        if (methodName != null && !NO_METHOD_CALL.equals(methodName)) {
          try {
            method = bean.getClass().getMethod(methodName, (Class []) null);
            synchronized (bean) {
              forward = bean.getInterceptor().intercept(new ActionInvoker(bean, method));
            }
          } catch (Exception e) {
            throw new BeanActionException("Error dispatching bean action via method parameter ('" + methodName + "').  Cause: " + e, e);
          }
        }



        // Path Based Method Mapping

        if (method == null && !NO_METHOD_CALL.equals(methodName)) {
          methodName = mapping.getPath();
          if (methodName.length() > 1) {
            int slash = methodName.lastIndexOf("/") + 1;
            methodName = methodName.substring(slash);
            if (methodName.length() > 0) {
              try {
                method = bean.getClass().getMethod(methodName, (Class []) null);
                synchronized (bean) {
                  forward = bean.getInterceptor().intercept(new ActionInvoker(bean, method));
                }
              } catch (Exception e) {
                throw new BeanActionException("Error dispatching bean action via URL pattern ('" + methodName + "').  Cause: " + e, e);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      forward = "error";
      request.setAttribute("BeanActionException", e);
    }
    return mapping.findForward(forward);
  }
}
