package org.apache.struts.beanaction.httpmap;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * Map to wrap form parameters.
 * <p/>
 * <p/>
 * <p/>
 * Date: Mar 11, 2004 10:35:52 PM
 *
 * @author Clinton Begin
 */
public class ParameterMap extends BaseHttpMap {
  private HttpServletRequest request;

  public ParameterMap(HttpServletRequest request) {
    this.request = request;
  }

  protected Enumeration getNames() {
    return request.getParameterNames();
  }

  protected Object getValue(Object key) {
    return request.getParameter(String.valueOf(key));
  }

  protected Object[] getValues(Object key) {
    return request.getParameterValues(String.valueOf(key));
  }

  protected void putValue(Object key, Object value) {
    throw new UnsupportedOperationException("Cannot put value to ParameterMap.");
  }

  protected void removeValue(Object key) {
    throw new UnsupportedOperationException("Cannot remove value from ParameterMap.");
  }
}
