package org.gatein.api.composition;

import java.util.List;

/**
 * Builds a {@link ColumnContainerImpl}.
 *
 * @see ColumnContainerImpl
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ColumnContainerBuilderImpl<T extends LayoutBuilder<T>> extends ContainerBuilderImpl<T> {
    public ColumnContainerBuilderImpl(T topBuilder) {
        super(topBuilder);
    }

    public ColumnContainerBuilderImpl(T topBuilder, ContainerBuilderImpl<T> parent) {
        super(topBuilder, parent);
    }

    /**
     * Specializes the Container type, by wrapping the given list of containerItems into a ColumnContainerImpl
     *
     * @see ColumnContainerImpl
     * @param containerItems the list of container to be included into a column
     * @return the column, with the list of containerItems as children
     */
    @Override
    protected Container createContainer(List<ContainerItem> containerItems) {
        return completeContainer(new ColumnContainerImpl(containerItems));
    }
}
