package org.exoplatform.portal.pc.aspects;

import javax.portlet.PortletRequest;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.EventInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.portlet.PortletInvokerInterceptor;

/**
 * This interceptor just sets the portlet lifecycle phase so it can be retrieved else where during a request.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletLifecyclePhaseInterceptor extends PortletInvokerInterceptor {

    private static final ThreadLocal<String> phase = new ThreadLocal<String>();

    @Override
    public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException {
        String portletPhase = getPhase(invocation);
        try {
            phase.set(portletPhase);
            return super.invoke(invocation);
        } finally {
            phase.remove();
        }
    }

    public static String getLifecyclePhase() {
        return phase.get();
    }

    private String getPhase(PortletInvocation invocation) {
        if (invocation instanceof ActionInvocation) {
            return PortletRequest.ACTION_PHASE;
        } else if (invocation instanceof EventInvocation) {
            return PortletRequest.EVENT_PHASE;
        } else if (invocation instanceof RenderInvocation) {
            return PortletRequest.RENDER_PHASE;
        } else if (invocation instanceof ResourceInvocation) {
            return PortletRequest.RESOURCE_PHASE;
        } else {
            return null;
        }
    }
}