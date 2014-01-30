package org.gatein.api.composition;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A basic layout builder.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class LayoutBuilderImpl<T extends LayoutBuilder<T>> implements LayoutBuilder<T> {
    private static final Logger log = LoggerFactory.getLogger(LayoutBuilderImpl.class);
    protected List<ContainerItem> children = new ArrayList<ContainerItem>();

    /**
     * @see org.gatein.api.composition.LayoutBuilder#newColumnsBuilder()
     */
    @Override
    public ContainerBuilder<T> newColumnsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        @SuppressWarnings("unchecked")
        ColumnContainerBuilderImpl<T> containerBuilder = new ColumnContainerBuilderImpl<T>((T) this);

        return containerBuilder;
    }

    /**
     * @see org.gatein.api.composition.LayoutBuilder#newRowsBuilder()
     */
    @Override
    public ContainerBuilder<T> newRowsBuilder() {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        @SuppressWarnings("unchecked")
        ContainerBuilderImpl<T> containerBuilder = new ContainerBuilderImpl<T>((T) this);

        return containerBuilder;
    }

    /**
     * @see org.gatein.api.composition.LayoutBuilder#newCustomContainerBuilder(org.gatein.api.composition.Container)
     */
    @Override
    public ContainerBuilder<T> newCustomContainerBuilder(Container container) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new layout container");
        }

        /**
         * this is the root container, located at the first level
         */
        @SuppressWarnings("unchecked")
        CustomContainerBuilderImpl<T> containerBuilder = new CustomContainerBuilderImpl<T>(container, (T) this);

        return containerBuilder;
    }

    /**
     * @see org.gatein.api.composition.LayoutBuilder#newCustomContainerBuilder(java.lang.String)
     */
    @Override
    public ContainerBuilder<T> newCustomContainerBuilder(String template) {
        if (log.isTraceEnabled()) {
            log.trace("Creating a new custom container builder");
        }

        Container container = new ContainerImpl(template, null);
        @SuppressWarnings("unchecked")
        T t = (T) this;
        return new CustomContainerBuilderImpl<T>(container, t);
    }


    /**
     * @see org.gatein.api.composition.LayoutBuilder#child(org.gatein.api.composition.ContainerItem)
     */
    @Override
    public T child(ContainerItem containerItem) {
        this.children.add(containerItem);
        @SuppressWarnings("unchecked")
        T t = (T) this;
        return t;
    }

    /**
     * @see org.gatein.api.composition.LayoutBuilder#children(java.util.List)
     */
    @Override
    public T children(List<ContainerItem> children) {
        if (null == children) {
            this.children.clear();
            @SuppressWarnings("unchecked")
            T t = (T) this;
            return (T) t;
        }

        this.children.addAll(children);
        @SuppressWarnings("unchecked")
        T t = (T) this;
        return t;
    }
}
