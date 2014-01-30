package org.gatein.api.composition;

import java.util.List;

public class CustomContainerBuilderImpl<T extends LayoutBuilder<T>> extends ContainerBuilderImpl<T> {
    private Container baseContainer;

    public CustomContainerBuilderImpl(Container baseContainer, T topBuilder) {
        super(topBuilder);
        this.baseContainer = baseContainer;
    }

    public CustomContainerBuilderImpl(Container baseContainer, T topBuilder, ContainerBuilderImpl<T> parent) {
        super(topBuilder, parent);
        this.baseContainer = baseContainer;
    }

    @Override
    protected Container createContainer(List<ContainerItem> containerItems) {
        baseContainer.setChildren(containerItems);
        return completeContainer(baseContainer);
    }

}
