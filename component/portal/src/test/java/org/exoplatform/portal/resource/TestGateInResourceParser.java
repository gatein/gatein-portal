package org.exoplatform.portal.resource;

import junit.framework.TestCase;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.portal.resource.config.tasks.SkinConfigTask;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class TestGateInResourceParser extends TestCase
{   
   public void testBinding() throws MalformedURLException {
      URL url = this.getClass().getResource("/WEB-INF/gatein-resources.xml");
      assertNotNull("the gatein-resources.xml can not be found", url);
      DocumentSource source = DocumentSource.create(url);
      List<SkinConfigTask> tasks = SkinConfigParser.fetchTasks(source);
      assertNotNull("There are no tasks", tasks);
      assertEquals(8, tasks.size());
   }
}
