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
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.ApplicationStatisticService;
import org.exoplatform.portal.application.PortalStatisticService;
import org.jboss.as.controller.AbstractRuntimeOnlyHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.client.helpers.MeasurementUnit;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class StatisticsMetricHandler extends AbstractRuntimeOnlyHandler {
    static enum StatisticsMetric {
        MAX_TIME(PortalResourceConstants.MAX_TIME, ModelType.DOUBLE, MeasurementUnit.SECONDS), MIN_TIME(
                PortalResourceConstants.MIN_TIME, ModelType.DOUBLE, MeasurementUnit.SECONDS), AVERAGE_TIME(
                PortalResourceConstants.AVERAGE_TIME, ModelType.DOUBLE, MeasurementUnit.SECONDS), THROUGHPUT(
                PortalResourceConstants.THROUGHPUT, ModelType.DOUBLE, MeasurementUnit.PER_SECOND, SITE), EXECUTION_COUNT(
                PortalResourceConstants.EXECUTION_COUNT, ModelType.LONG, MeasurementUnit.NONE);

        private static final Map<String, StatisticsMetric> MAP;

        static {
            Map<String, StatisticsMetric> map = new HashMap<String, StatisticsMetric>();
            for (StatisticsMetric metric : StatisticsMetric.values()) {
                map.put(metric.toString(), metric);
            }
            MAP = map;
        }

        final AttributeDefinition definition;
        final String typeSpecific; // if statistic is 'site' or 'application' specific. i.e. throughput

        private StatisticsMetric(String attributeName, ModelType type, MeasurementUnit measurementUnit) {
            this(attributeName, type, measurementUnit, null);
        }

        private StatisticsMetric(String attributeName, ModelType type, MeasurementUnit measurementUnit, String typeSpecific) {
            this(new SimpleAttributeDefinitionBuilder(attributeName, type, false).setMeasurementUnit(measurementUnit)
                    .setStorageRuntime().build(), typeSpecific);
        }

        private StatisticsMetric(final AttributeDefinition definition, String typeSpecific) {
            this.definition = definition;
            this.typeSpecific = typeSpecific;
        }

        static StatisticsMetric forName(String attributeName) {
            return MAP.get(attributeName);
        }

        static StatisticsMetric[] forType(String type) {
            EnumSet<StatisticsMetric> set = EnumSet.noneOf(StatisticsMetric.class);
            for (StatisticsMetric metric : StatisticsMetric.values()) {
                if (metric.typeSpecific == null || metric.typeSpecific.equals(type)) {
                    set.add(metric);
                }
            }

            return set.toArray(new StatisticsMetric[set.size()]);
        }

        @Override
        public String toString() {
            return definition.getName();
        }
    }

    private static final StatisticsMetricHandler INSTANCE = new StatisticsMetricHandler();

    static void registerMetrics(String type, ManagementResourceRegistration registration) {
        for (StatisticsMetric metric : StatisticsMetric.forType(type)) {
            registration.registerMetric(metric.definition, INSTANCE);
        }
    }

    private StatisticsMetricHandler() {
    }

    @Override
    protected void executeRuntimeStep(OperationContext context, ModelNode operation) throws OperationFailedException {
        final PathAddress address = PathAddress.pathAddress(operation.require(OP_ADDR));
        final String portal = address.getElement(address.size() - 2).getValue();
        final String type = address.getLastElement().getKey();
        final String name = address.getLastElement().getValue();
        final String attributeName = operation.require(NAME).asString();
        final StatisticsMetric metric = StatisticsMetric.forName(attributeName);
        if (metric == null) {
            context.getFailureDescription().set(format("Unknown metric %s", attributeName));
        } else {
            try {
                ModelNode result = new ModelNode();
                switch (metric) {
                    case MAX_TIME:
                        result.set(maxTime(portal, type, name));
                        break;
                    case MIN_TIME:
                        result.set(minTime(portal, type, name));
                        break;
                    case AVERAGE_TIME:
                        result.set(averageTime(portal, type, name));
                        break;
                    case THROUGHPUT:
                        result.set(throughput(portal, type, name));
                        break;
                    case EXECUTION_COUNT:
                        result.set(executionCount(portal, type, name));
                        break;
                }
                context.getResult().set(result);
            } catch (OperationFailedException ofe) {
                throw ofe;
            } catch (Exception e) {
                throw new OperationFailedException(format(
                        "Unknown exception occurred while trying to obtain statistic metric %s for %s %s", metric, type, name));
            }
        }

        context.completeStep();
    }

    private static double maxTime(final String portal, final String type, final String name) throws Exception {
        final PortalContext context = new PortalContext(portal);
        if (type.equals(SITE)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return siteStatistic(container).getMaxTime(name);
                }
            });
        } else if (type.equals(APPLICATION)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return applicationStatistic(container).getMaxTime(name);
                }
            });
        } else {
            throw new OperationFailedException(format("Unknown service statistic type '%s'. Valid values are '%s' and '%s'.",
                    type, SITE, APPLICATION));
        }
    }

    private static double minTime(final String portal, final String type, final String name) throws Exception {
        final PortalContext context = new PortalContext(portal);
        if (type.equals(SITE)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return siteStatistic(container).getMinTime(name);
                }
            });
        } else if (type.equals(APPLICATION)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return applicationStatistic(container).getMinTime(name);
                }
            });
        } else {
            throw new OperationFailedException(format("Unknown service statistic type '%s'. Valid values are '%s' and '%s'.",
                    type, SITE, APPLICATION));
        }
    }

    private static double averageTime(final String portal, final String type, final String name) throws Exception {
        final PortalContext context = new PortalContext(portal);
        if (type.equals(SITE)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return siteStatistic(container).getAverageTime(name);
                }
            });
        } else if (type.equals(APPLICATION)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return applicationStatistic(container).getAverageTime(name);
                }
            });
        } else {
            throw new OperationFailedException(format("Unknown service statistic type '%s'. Valid values are '%s' and '%s'.",
                    type, SITE, APPLICATION));
        }
    }

    private static double throughput(final String portal, final String type, final String name) throws Exception {
        final PortalContext context = new PortalContext(portal);
        if (type.equals(SITE)) {
            return context.execute(new PortalContext.Request<Double>() {
                @Override
                public Double within(PortalContainer container) {
                    return siteStatistic(container).getThroughput(name);
                }
            });
        } else if (type.equals(APPLICATION)) {
            throw new OperationFailedException(format("Throughput metric is not available for %s statistics.", APPLICATION));
        } else {
            throw new OperationFailedException(format("Unknown service statistic type '%s'. Valid values are '%s' and '%s'.",
                    type, SITE, APPLICATION));
        }
    }

    private static long executionCount(final String portal, final String type, final String name) throws Exception {
        final PortalContext context = new PortalContext(portal);
        if (type.equals(SITE)) {
            return context.execute(new PortalContext.Request<Long>() {
                @Override
                public Long within(PortalContainer container) {
                    return siteStatistic(container).getExecutionCount(name);
                }
            });
        } else if (type.equals(APPLICATION)) {
            return context.execute(new PortalContext.Request<Long>() {
                @Override
                public Long within(PortalContainer container) {
                    return applicationStatistic(container).getExecutionCount(name);
                }
            });
        } else {
            throw new OperationFailedException(format("Unknown service statistic type '%s'. Valid values are '%s' and '%s'.",
                    type, SITE, APPLICATION));
        }
    }

    private static PortalStatisticService siteStatistic(final PortalContainer container) {
        return service(PortalStatisticService.class, container);
    }

    private static ApplicationStatisticService applicationStatistic(final PortalContainer container) {
        return service(ApplicationStatisticService.class, container);
    }

    private static <T> T service(Class<T> componentType, PortalContainer container) {
        return componentType.cast(container.getComponentInstanceOfType(componentType));
    }

    private static String format(String format, Object... args) {
        return String.format(format, args);
    }
}
