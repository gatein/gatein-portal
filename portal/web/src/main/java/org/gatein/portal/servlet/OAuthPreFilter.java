package org.gatein.portal.servlet;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: tuyennt
 * Date: 7/8/13
 * Time: 5:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OAuthPreFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //TODO: temp
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("org.gatein.sso.agent.tomcat.ServletAccess");
            Method getRequestMethod = clazz.getDeclaredMethod("setRequestAndResponse", HttpServletRequest.class, HttpServletResponse.class);
            getRequestMethod.invoke(null, servletRequest, servletResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            try {
                Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass("org.gatein.sso.agent.tomcat.ServletAccess");
                Method getRequestMethod = clazz.getDeclaredMethod("resetRequestAndResponse");
                getRequestMethod.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
    }
}
