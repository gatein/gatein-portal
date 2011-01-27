package org.exoplatform.portal.gadget.core;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.servlet.GuiceServletContextListener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 *   Extending the GuiceServletContextListener to setup a ThreadLocal<GateInContainerConfigLoader> variable wrapping
 * the ServletContext. That makes the ServletContext accessible within the scope of thread executing the method
 * contextInitialized
 *
 *
 * User: Minh Hoang TO - hoang281283@gmail.com
 * Date: 1/12/11
 * Time: 3:50 PM
 */
public class GateInGuiceServletContextListener extends GuiceServletContextListener {

  private static ThreadLocal<GateInContainerConfigLoader> currentLoader = new ThreadLocal<GateInContainerConfigLoader>();

  @Override
  public void contextInitialized(ServletContextEvent event) {

    final ServletContext scontext = event.getServletContext();

    GateInContainerConfigLoader loader = new GateInContainerConfigLoader() {
      @Override
      public String loadContentAsString(String path, String encoding) throws IOException{

        //To make sure that the path begins with a slash, as required in the javadoc of method
        // getResourceAsStream in ServletContext
        //The leading slash is required on certain application servers such as JBoss, WebSphere
        if('/' != path.charAt(0))
        {
           path = '/' + path;
        }

        InputStream is = scontext.getResourceAsStream(path);

        if(is == null)
        {
          throw new NullPointerException("There is no file specified by path : " + path);
        }
        return IOUtils.toString(is, encoding);
      }
    };

    //Setup the threadlocal loader
    currentLoader.set(loader);

    try
    {
      //Setup the Guice objects, the threadlocal loader is accessible for the moment
      super.contextInitialized(event);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      //Reset the threadlocal loader to null
      currentLoader.set(null);
    }

  }

  public static GateInContainerConfigLoader getCurrentLoader()
  {
    return currentLoader.get();
  }
}
