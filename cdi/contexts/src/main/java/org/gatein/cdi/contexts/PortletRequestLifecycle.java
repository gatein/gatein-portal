/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.cdi.contexts;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortletRequestLifecycle implements Serializable {
    private final LinkedList<State> states;

    public PortletRequestLifecycle() {
        states = new LinkedList<State>();
    }

    public State last() {
        return states.peekLast();
    }

    public State first() {
        return states.peekFirst();
    }

    public State addNext(State next) {
        State previous = last();
        states.add(next);
        return previous;
    }

    public int size() {
        return states.size();
    }

    public static class State implements Serializable {

        private static final int START = 0;
        private static final int END = 1;

        private final String phase;
        private final int flag;

        public static State starting(String phase) {
            return new State(phase, START);
        }

        public static State ending(String phase) {
            return new State(phase, END);
        }

        private State(String phase, int flag) {
            if (phase == null) throw new NullPointerException("phase cannot be null");

            this.phase = phase;
            this.flag = flag;
        }

        public String getPhase() {
            return phase;
        }

        public boolean isPhase(String... phases) {
            if (phases == null) return false;

            for (String phase : phases) {
                if (this.phase.equals(phase)) {
                    return true;
                }
            }

            return false;
        }

        public boolean started() {
            return flag == START;
        }

        public boolean ended() {
            return flag == END;
        }
    }
}
