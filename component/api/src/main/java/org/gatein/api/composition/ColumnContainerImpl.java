package org.gatein.api.composition;

import java.util.List;

/**
 * A {@link Container} having a template for rendering children in columns.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ColumnContainerImpl extends ContainerImpl {
    /**
     * Internal to this implementation. May change without notice.
     */
    public static final String COLUMNS_TEMPLATE_URL = "system:/groovy/portal/webui/container/UITableColumnContainer.gtmpl";

    public ColumnContainerImpl(List<ContainerItem> containers) {
        super(COLUMNS_TEMPLATE_URL, containers);
    }
}
