/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.upload;

import java.io.File;
import java.io.IOException;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.junit.Test;

@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/services/upload-service.xml")
})
public class TestCorrectFileName extends AbstractKernelTest {
    private static final String tmpDir = System.getProperty("java.io.tmpdir");
    UploadService uploadService;

    @Override
    protected void beforeRunBare() {
        super.beforeRunBare();
        uploadService = getContainer().getComponentInstanceOfType(UploadService.class);
    }

    @Test
    public void testCorrectFileName() {
        String fileName;

        fileName = "test_abcdef0123456789.ext";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_abcdef0123456789.ext", fileName);
        assertFileCreation(fileName);

        fileName = "test~`!@#$%^&*()-=+_.ext";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test~`!@#$%^&_()-=+_.ext", fileName);
        assertFileCreation(fileName);

        fileName = "test[]{}\\|.ext";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test[]{}__.ext", fileName);
        assertFileCreation(fileName);

        fileName = "test;:\"'.ext";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test;__'.ext", fileName);
        assertFileCreation(fileName);

        fileName = "test_tên tiếng việt.mở rộng";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_tên tiếng việt.mở rộng", fileName);
        assertFileCreation(fileName);

        fileName = "test_越南人的名字。扩展";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_越南人的名字。扩展", fileName);
        assertFileCreation(fileName);

        fileName = "test_В'єтнамські імена. розширення";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_В'єтнамські імена. розширення", fileName);
        assertFileCreation(fileName);

        fileName = "test_베트남어 이름. 확장";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_베트남어 이름. 확장", fileName);
        assertFileCreation(fileName);

        fileName = "test_वियतनामी नामों. विस्तार";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_वियतनामी नामों. विस्तार", fileName);
        assertFileCreation(fileName);

        fileName = "test_ベトナム語名。拡大";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_ベトナム語名。拡大", fileName);
        assertFileCreation(fileName);

        fileName = "test_ਵੀਅਤਨਾਮੀ ਨਾਮ. ਵਿਸਤਾਰ";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_ਵੀਅਤਨਾਮੀ ਨਾਮ. ਵਿਸਤਾਰ", fileName);
        assertFileCreation(fileName);

        fileName = "test_Вьетнамские имена. расширение";
        fileName = uploadService.correctFileName(fileName);
        assertEquals("test_Вьетнамские имена. расширение", fileName);
        assertFileCreation(fileName);
    }

    private void assertFileCreation(String fileName) {
        String file = tmpDir + File.separator + fileName;
        File f = new File(file);
        f.deleteOnExit();
        try {
            f.createNewFile();
        } catch (IOException ex) {
            fail("Can not create file with name: " + fileName);
        }
    }
}
