/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.component.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.naming.InitialContextInitializer;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-configuration.xml")
})
public class BootstrapTestCase extends AbstractKernelTest
{

   /** . */
   private String previousTmpDirPath = null;

   public void testRequestLifeCycle()
   {
      PortalContainer container = PortalContainer.getInstance();
      CustomService testService = (CustomService)container.getComponentInstanceOfType(CustomService.class);
      assertNull(testService.currentContainer);
      begin();
      assertNotNull(testService);
      assertSame(container, testService.currentContainer);
      end();
      assertNull(testService.currentContainer);
   }

   public void testDataSource() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      container.getComponentInstanceOfType(InitialContextInitializer.class);
      DataSource ds = (DataSource)new InitialContext().lookup("jdbcexo");
      assertNotNull(ds);
      Connection conn = ds.getConnection();
      DatabaseMetaData databaseMD = conn.getMetaData();
      String db = databaseMD.getDatabaseProductName() + " " + databaseMD.getDatabaseProductVersion() + "." +
         databaseMD.getDatabaseMajorVersion() + "." +  databaseMD.getDatabaseMinorVersion();
      String driver = databaseMD.getDriverName() + " " + databaseMD.getDriverVersion() + "." +
         databaseMD.getDriverMajorVersion() + "." +  databaseMD.getDriverMinorVersion();
      log.info("Using database " + db + " with driver " + driver);
      conn.close();
   }

   public void testTmpDir1() throws Exception
   {
      ensureTmpDir();
   }

   public void testTmpDir2() throws Exception
   {
      ensureTmpDir();
   }

   private void ensureTmpDir() throws Exception
   {
      String tmpDirPath = System.getProperty("gatein.test.tmp.dir");
      assertNotNull(tmpDirPath);

      //
      if (previousTmpDirPath != null)
      {
         assertEquals(previousTmpDirPath, tmpDirPath);
      }
      else
      {
         previousTmpDirPath = tmpDirPath;
      }

      //
      File f = new File(tmpDirPath);
      assertTrue(f.exists());
      assertTrue(f.isDirectory());
      assertTrue(f.canWrite());
      assertEquals(0, f.list().length);
   }
}
