package org.exoplatform.portal.gadget.core;

import javax.servlet.ServletContext;
import java.io.IOException;

/**
 *   A generic loader, used to load gadget server configuration files. We abuse the ThreadLocal here, as there is no way
 *   to associate Guice components with Kernel 's configuration loader component (ConfigurationManager)
 *
 * User: Minh Hoang TO - hoang281283@gmail.com
 * Date: 1/12/11
 * Time: 3:31 PM
 */
public abstract class GateInContainerConfigLoader {

  public abstract String loadContentAsString(String path, String encoding) throws IOException;

}
