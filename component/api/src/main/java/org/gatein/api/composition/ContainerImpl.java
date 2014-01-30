package org.gatein.api.composition;

import java.util.List;

/**
 * Basic representation of a Container, as defined by the public API. Children are rendered in rows.
 *
 * @see org.gatein.api.composition.Container
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ContainerImpl extends BareContainerImpl implements Container {

    /**
     * Internal to this implementation. May change without notice.
     */
    private static final String ROWS_TEMPLATE_URL = "system:/groovy/portal/webui/container/UIContainer.gtmpl";

    private String template;

    public ContainerImpl(String template, List<ContainerItem> children) {
        super(children);
        this.template = template;
    }

    public ContainerImpl(List<ContainerItem> children) {
        this(ROWS_TEMPLATE_URL, children);
    }

    /**
     * @see org.gatein.api.composition.Container#getTemplate()
     */
    @Override
    public String getTemplate() {
        return template;
    }

    /**
     * @see org.gatein.api.composition.Container#setTemplate(java.lang.String)
     */
    @Override
    public void setTemplate(String template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return "ContainerImpl{" +
                "children=" + children +
                ", hashCode=" + hashCode() +
                '}';
    }
}
