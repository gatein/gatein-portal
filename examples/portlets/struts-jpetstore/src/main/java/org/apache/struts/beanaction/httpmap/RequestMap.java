package org.apache.struts.beanaction.httpmap;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Map to wrap request scope attributes.
 * <p/>
 * Date: Mar 11, 2004 10:35:34 PM
 *
 * @author Clinton Begin
 */
public class RequestMap extends BaseHttpMap {
  private HttpServletRequest request;

  public RequestMap(HttpServletRequest request) {
    this.request = request;
  }

  protected Enumeration getNames() {
    return request.getAttributeNames();
  }

  protected Object getValue(Object key) {
    return request.getAttribute(String.valueOf(key));
  }

  protected void putValue(Object key, Object value) {
    request.setAttribute(String.valueOf(key), value);
  }

  protected void removeValue(Object key) {
    request.removeAttribute(String.valueOf(key));
  }
}