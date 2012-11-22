/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.integration.wsrp.jcr;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.format.FormatterContext;
import org.chromattic.api.format.ObjectFormatter;
import org.chromattic.spi.jcr.SessionLifeCycle;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.gatein.wsrp.jcr.BaseChromatticPersister;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRPersister extends BaseChromatticPersister {
    public JCRPersister(ExoContainer container, String workspaceName) {
        super(workspaceName);
    }

    @Override
    protected void setBuilderOptions(ChromatticBuilder builder) {
        if (PORTLET_STATES_WORKSPACE_NAME.equals(workspaceName)) {
            builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, PortletStatesSessionLifeCycle.class.getName());
        } else if (WSRP_WORKSPACE_NAME.equals(workspaceName)) {
            builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, WSRPSessionLifeCycle.class.getName());
        } else {
            throw new IllegalArgumentException("Unknown workspace name: '" + workspaceName + "'");
        }
    }

    public abstract static class AbstractSessionLifeCycle implements SessionLifeCycle {
        private ManageableRepository repository;
        private ThreadLocal<SessionProvider> provider = new ThreadLocal<SessionProvider>();

        public AbstractSessionLifeCycle() {
            try {
                ExoContainer container = ExoContainerContext.getCurrentContainer();
                RepositoryService repoService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
                repository = repoService.getRepository(REPOSITORY_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Session login() throws RepositoryException {
            SessionProvider sessionProvider = provider.get();
            if (sessionProvider == null) {
                sessionProvider = SessionProvider.createSystemProvider();
                provider.set(sessionProvider);
            }

            return sessionProvider.getSession(getWorkspaceName(), repository);
        }

        protected abstract String getWorkspaceName();

        public Session login(String s) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

        public Session login(Credentials credentials, String s) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

        public Session login(Credentials credentials) throws RepositoryException {
            throw new UnsupportedOperationException();
        }

        public void save(Session session) throws RepositoryException {
            session.save();
        }

        public void close(Session session) {
            session.logout();
        }
    }

    public static class WSRPSessionLifeCycle extends AbstractSessionLifeCycle {
        @Override
        protected String getWorkspaceName() {
            return WSRP_WORKSPACE_NAME;
        }
    }

    public static class PortletStatesSessionLifeCycle extends AbstractSessionLifeCycle {
        @Override
        protected String getWorkspaceName() {
            return PORTLET_STATES_WORKSPACE_NAME;
        }
    }

    public static class QNameFormatter implements ObjectFormatter {
        private static final String OPEN_BRACE_REPLACEMENT = "-__";
        private static final String CLOSE_BRACE_REPLACEMENT = "__-";
        private static final String COLON_REPLACEMENT = "_-_";
        private static final String CLOSE_BRACE = "}";
        private static final String OPEN_BRACE = "{";
        private static final String COLON = ":";

        public String decodeNodeName(FormatterContext formatterContext, String s) {
            return decode(s);
        }

        public String encodeNodeName(FormatterContext formatterContext, String s) {
            return encode(s);
        }

        public String decodePropertyName(FormatterContext formatterContext, String s) {
            return decode(s);
        }

        public String encodePropertyName(FormatterContext formatterContext, String s) {
            return encode(s);
        }

        public static String decode(String s) {
            return s.replace(CLOSE_BRACE_REPLACEMENT, CLOSE_BRACE).replace(OPEN_BRACE_REPLACEMENT, OPEN_BRACE)
                    .replace(COLON_REPLACEMENT, COLON);
        }

        public static String encode(String s) {
            return s.replace(OPEN_BRACE, OPEN_BRACE_REPLACEMENT).replace(CLOSE_BRACE, CLOSE_BRACE_REPLACEMENT)
                    .replace(COLON, COLON_REPLACEMENT);
        }
    }

    public static class PortletNameFormatter implements ObjectFormatter {
        public static final String SLASH_REPLACEMENT = "-_-";
        private static final String SLASH = "/";

        public String decodeNodeName(FormatterContext formatterContext, String s) {
            return decode(s);
        }

        public static String decode(String s) {
            return s.replace(SLASH_REPLACEMENT, SLASH);
        }

        public String encodeNodeName(FormatterContext formatterContext, String s) throws IllegalArgumentException,
                NullPointerException {
            return encode(s);
        }

        public static String encode(String s) {
            return s.replace(SLASH, SLASH_REPLACEMENT);
        }
    }
}
