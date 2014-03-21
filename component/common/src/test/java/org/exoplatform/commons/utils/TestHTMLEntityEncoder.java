/**
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class TestHTMLEntityEncoder extends TestCase {
    private HTMLEntityEncoder htmlEncoder = HTMLEntityEncoder.getInstance();

    public void testHTMLEncoding() {
        assertEquals("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;", htmlEncoder.encode("<h1>HELLO WORLD</h1>"));
        assertEquals("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;", htmlEncoder.encodeHTML("<h1>HELLO WORLD</h1>"));

        assertEquals("alert&#x28;&#x27;HELLO WORLD&#x27;&#x29;", htmlEncoder.encode("alert('HELLO WORLD')"));
        assertEquals("alert&#x28;&#x27;HELLO WORLD&#x27;&#x29;", htmlEncoder.encodeHTML("alert('HELLO WORLD')"));

        assertEquals(
                "&lt;a href&#x3d;&quot;http&#x3a;&#x2f;&#x2f;example.com&#x2f;&#x3f;name1&#x3d;value1&amp;name2&#x3d;value2&amp;name3&#x3d;a&#x2b;b&quot;&gt;link&lt;&#x2f;a&gt;",
                htmlEncoder.encode("<a href=\"http://example.com/?name1=value1&name2=value2&name3=a+b\">link</a>"));
        assertEquals(
                "&lt;a href&#x3d;&quot;http&#x3a;&#x2f;&#x2f;example.com&#x2f;&#x3f;name1&#x3d;value1&amp;name2&#x3d;value2&amp;name3&#x3d;a&#x2b;b&quot;&gt;link&lt;&#x2f;a&gt;",
                htmlEncoder.encodeHTML("<a href=\"http://example.com/?name1=value1&name2=value2&name3=a+b\">link</a>"));
    }

    public void testHTMLAttributeEncoding() {
        assertEquals("&lt;h1&gt;HELLO&#x20;WORLD&lt;&#x2f;h1&gt;", htmlEncoder.encodeHTMLAttribute("<h1>HELLO WORLD</h1>"));

        assertEquals("alert&#x28;&#x27;HELLO&#x20;WORLD&#x27;&#x29;", htmlEncoder.encodeHTMLAttribute("alert('HELLO WORLD')"));

        assertEquals(
                "&lt;a&#x20;href&#x3d;&quot;http&#x3a;&#x2f;&#x2f;example.com&#x2f;&#x3f;name1&#x3d;value1&amp;name2&#x3d;value2&amp;name3&#x3d;a&#x2b;b&quot;&gt;link&lt;&#x2f;a&gt;",
                htmlEncoder.encodeHTMLAttribute("<a href=\"http://example.com/?name1=value1&name2=value2&name3=a+b\">link</a>"));
    }

    public void testIsEncoded() {
        assertTrue(htmlEncoder.isEncoded("&ccedil;&atilde;o"));
        assertFalse(htmlEncoder.isEncoded("not encoded"));
        assertFalse(htmlEncoder.isEncoded("not encoded;"));
        assertTrue(htmlEncoder.isEncoded("encoded&#x3b;"));
        assertFalse(htmlEncoder.isEncoded("&not encoded;"));
        assertFalse(htmlEncoder.isEncoded("&not encoded"));
        assertFalse(htmlEncoder.isEncoded("not encoded&;"));
        assertTrue(htmlEncoder.isEncoded("encoded&amp;"));
        assertTrue(htmlEncoder.isEncoded("&amp;encoded;"));
        assertTrue(htmlEncoder.isEncoded("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;"));
        assertTrue(htmlEncoder.isEncoded("alert&#x28;&#x27;HELLO WORLD&#x27;&#x29;"));
    }

    public void testEncodeIfNotEncoded() {
        assertEquals("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;", htmlEncoder.encodeIfNotEncoded("<h1>HELLO WORLD</h1>"));
        assertEquals("&ccedil;&atilde;o", htmlEncoder.encodeIfNotEncoded("&ccedil;&atilde;o"));
        assertEquals("not encoded", htmlEncoder.encodeIfNotEncoded("not encoded"));
        assertEquals("not encoded&#x3b;", htmlEncoder.encodeIfNotEncoded("not encoded;"));
        assertEquals("encoded&#x3b;", htmlEncoder.encodeIfNotEncoded("encoded&#x3b;"));
        assertEquals("&amp;not encoded&#x3b;", htmlEncoder.encodeIfNotEncoded("&not encoded;"));
        assertEquals("&amp;not encoded", htmlEncoder.encodeIfNotEncoded("&not encoded"));
        assertEquals("not encoded&amp;&#x3b;", htmlEncoder.encodeIfNotEncoded("not encoded&;"));
        assertEquals("encoded&amp;", htmlEncoder.encodeIfNotEncoded("encoded&amp;"));
        assertEquals("&amp;encoded;", htmlEncoder.encodeIfNotEncoded("&amp;encoded;"));
        assertEquals("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;", htmlEncoder.encodeIfNotEncoded("&lt;h1&gt;HELLO WORLD&lt;&#x2f;h1&gt;"));
        assertEquals("alert&#x28;&#x27;HELLO WORLD&#x27;&#x29;", htmlEncoder.encodeIfNotEncoded("alert&#x28;&#x27;HELLO WORLD&#x27;&#x29;"));
    }
}
