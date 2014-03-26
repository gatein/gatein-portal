/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.integration.jboss.as7.portal;

import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.APPLICATION;
import static org.gatein.integration.jboss.as7.portal.PortalResourceConstants.SITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Locale;
import java.util.ResourceBundle;

import org.gatein.integration.jboss.as7.GateInExtension;
import org.jboss.as.controller.descriptions.common.GlobalDescriptions;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalResourceDescriptions {
    private static final String RESOURCE_NAME = GateInExtension.class.getPackage().getName() + ".LocalDescriptions";

    static ModelNode getPortalReadResourceDescription(Locale locale) {
        ModelNode node = GlobalDescriptions.getReadResourceOperationDescription(locale);
        node.get(REPLY_PROPERTIES, VALUE_TYPE).set(ModelType.OBJECT);
        return node;
    }

    static ModelNode getPortalResourceDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set(bundle.getString("portal"));

        node.get(ATTRIBUTES).setEmptyObject();
        node.get(OPERATIONS).setEmptyObject();

        node.get(CHILDREN, SITE, DESCRIPTION).set(bundle.getString("portal.sites"));
        node.get(CHILDREN, SITE, MODEL_DESCRIPTION);

        node.get(CHILDREN, APPLICATION, DESCRIPTION).set(bundle.getString("portal.applications"));
        node.get(CHILDREN, APPLICATION, MODEL_DESCRIPTION);

        return node;
    }

    static ModelNode getSiteResourceDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set(bundle.getString("portal.site"));

        for (StatisticsMetricHandler.StatisticsMetric metric : StatisticsMetricHandler.StatisticsMetric.forType(SITE)) {
            metric.definition.addResourceAttributeDescription(bundle, "portal.site.metrics", node);
        }

        node.get(OPERATIONS).setEmptyObject();
        node.get(CHILDREN).setEmptyObject();

        return node;
    }

    static ModelNode getApplicationResourceDescription(Locale locale) {
        final ResourceBundle bundle = getResourceBundle(locale);

        final ModelNode node = new ModelNode();
        node.get(DESCRIPTION).set(bundle.getString("portal.application"));

        for (StatisticsMetricHandler.StatisticsMetric metric : StatisticsMetricHandler.StatisticsMetric.forType(APPLICATION)) {
            metric.definition.addResourceAttributeDescription(bundle, "portal.application.metrics", node);
        }

        node.get(OPERATIONS).setEmptyObject();
        node.get(CHILDREN).setEmptyObject();

        return node;
    }

    protected static ResourceBundle getResourceBundle(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return ResourceBundle.getBundle(RESOURCE_NAME, locale);
    }
}
