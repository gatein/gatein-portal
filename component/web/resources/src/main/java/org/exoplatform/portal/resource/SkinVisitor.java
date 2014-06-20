package org.exoplatform.portal.resource;

import java.util.Collection;
import java.util.Map.Entry;

/**
 * This visitor is used in {@link SkinService#findSkins(SkinVisitor)}
 * to filter the returned results.
 *
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public interface SkinVisitor {

    void visitPortalSkin(Entry<SkinKey, SkinConfig> entry);

    void visitSkin(Entry<SkinKey, SkinConfig> entry);

    Collection<SkinConfig> getSkins();
}
