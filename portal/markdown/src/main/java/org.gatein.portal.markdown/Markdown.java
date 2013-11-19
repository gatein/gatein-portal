package org.gatein.portal.markdown;

import java.io.Serializable;

/**
 * @author Julien Viet
 */
public class Markdown implements Serializable {

    /** . */
    final String value;

    public Markdown(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Markdown[" + value + "]";
    }
}
