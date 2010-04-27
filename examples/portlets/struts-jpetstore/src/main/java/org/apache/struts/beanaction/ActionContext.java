package org.apache.struts.beanaction;

import org.apache.struts.beanaction.httpmap.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * The ActionContext class gives simplified, thread-safe access to
 * <p/>
 * the request and response, as well as form parameters, request
 * <p/>
 * attributes, session attributes, application attributes.  Much
 * <p/>
 * of this can be accopmplished without using the Struts or even
 * <p/>
 * the Servlet API, therefore isolating your application from
 * <p/>
 * presentation framework details.
 * <p/>
 * <p/>
 * <p/>
 * This class also provides facilities for simpler message and error
 * <p/>
 * message handling.  Although not as powerful as that provided by
 * <p/>
 * Struts, it is great for simple applications that don't require
 * <p/>
 * internationalization or the flexibility of resource bundles.
 * <p/>
 * <p/>
 * <p/>
 * <i>Note: A more complete error and message handling API will be implemented.</i>
 * <p/>
 * <p/>
 * <p/>
 * Date: Mar 9, 2004 9:57:39 PM
 *
 * @author Clinton Begin
 */
public class ActionContext {
  private static final ThreadLocal localContext = new ThreadLocal();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Map cookieMap;
  private Map parameterMap;
  private Map requestMap;
  private Map sessionMap;
  private Map applicationMap;

  public ActionContext() {
    cookieMap = new HashMap();
    parameterMap = new HashMap();
    requestMap = new HashMap();
    sessionMap = new HashMap();
    applicationMap = new HashMap();
  }

  static void initCurrentContext(HttpServletRequest request, HttpServletResponse response) {
    ActionContext ctx = getActionContext();
    ctx.request = request;
    ctx.response = response;
    ctx.cookieMap = null;
    ctx.parameterMap = null;
    ctx.requestMap = null;
    ctx.sessionMap = null;
    ctx.applicationMap = null;
  }

  public Map getCookieMap() {
    if (cookieMap == null) {
      cookieMap = new CookieMap(request);
    }
    return cookieMap;
  }

  public Map getParameterMap() {
    if (parameterMap == null) {
      parameterMap = new ParameterMap(request);
    }
    return parameterMap;
  }

  public Map getRequestMap() {
    if (requestMap == null) {
      requestMap = new RequestMap(request);
    }
    return requestMap;
  }

  public Map getSessionMap() {
    if (sessionMap == null) {
      sessionMap = new SessionMap(request);
    }
    return sessionMap;
  }

  public Map getApplicationMap() {
    if (applicationMap == null) {
      applicationMap = new ApplicationMap(request);
    }
    return applicationMap;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public static ActionContext getActionContext() {
    ActionContext ctx = (ActionContext) localContext.get();
    if (ctx == null) {
      ctx = new ActionContext();
      localContext.set(ctx);
    }
    return ctx;
  }
}
