/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.gatein.ui.admin.redirect.beans;

import org.exoplatform.portal.config.model.DevicePropertyCondition;

/**
 * @author <a href="mailto:amendonca@redhat.com">Alexandre Mendonca</a>
 * @version $Revision$
 */
public class DevicePropertyConditionWrapper {

    private DevicePropertyCondition dpc;

    public DevicePropertyConditionWrapper(DevicePropertyCondition dpc) {
        this.dpc = dpc;
    }

    public String getPropertyName() {
        return dpc.getPropertyName();
    }

    public void setPropertyName(String propertyName) {
        dpc.setPropertyName(propertyName);
    }

    public Float getGreaterThan() {
        return dpc.getGreaterThan();
    }

    public void setGreaterThan(Float greaterThan) {
        clearAll();
        dpc.setGreaterThan(greaterThan);
    }

    public Float getLessThan() {
        return dpc.getLessThan();
    }

    public void setLessThan(Float lessThan) {
        clearAll();
        dpc.setLessThan(lessThan);
    }

    public String getEquals() {
        return dpc.getEquals();
    }

    public void setEquals(String equals) {
        clearAll();
        dpc.setEquals(equals);
    }

    public String getMatches() {
        return dpc.getMatches();
    }

    public void setMatches(String matches) {
        clearAll();
        dpc.setMatches(matches);
    }

    public Float getBetweenLow() {
        return dpc.getGreaterThan();
    }

    public void setBetweenLow(Float betweenLow) {
        Float high = dpc.getLessThan();
        clearAll();
        dpc.setLessThan(high);
        dpc.setGreaterThan(betweenLow);
        toString();
    }

    public Float getBetweenHigh() {
        return dpc.getLessThan();
    }

    public void setBetweenHigh(Float betweenHigh) {
        Float low = dpc.getGreaterThan();
        clearAll();
        dpc.setGreaterThan(low);
        dpc.setLessThan(betweenHigh);
        toString();
    }

    private void clearAll() {
        dpc.setLessThan(null);
        dpc.setEquals(null);
        dpc.setMatches(null);
        dpc.setGreaterThan(null);
    }

    public DevicePropertyCondition getDevicePropertyCondition() {
        return this.dpc;
    }

    public String getOperation() {
        return dpc.getEquals() != null ?  "eq" : dpc.getGreaterThan() != null ? (dpc.getLessThan() != null ? "bt" : "gt") : dpc.getLessThan() != null ? "lt" : "mt";
    }

    @Override
    public String toString() {
        return "DevicePropertyConditionWrapper GT[" + dpc.getGreaterThan() + "] LT[" + dpc.getLessThan() +
                "] EQ[" + dpc.getEquals() + "] MT[" + dpc.getMatches() + "] .. OP[" + getOperation() + "]";
    }
}