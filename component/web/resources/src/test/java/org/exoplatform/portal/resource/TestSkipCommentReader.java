/*
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
package org.exoplatform.portal.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/28/11
 */
public class TestSkipCommentReader extends TestCase {

    private SkipCommentReader skipCommentReader;

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        if (skipCommentReader != null) {
            skipCommentReader.close();
        }
    }

    private void initiateReader(String relativePath) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(relativePath);
        skipCommentReader = new SkipCommentReader(new InputStreamReader(in));
    }

    private void initiateReader(Reader reader) {
        skipCommentReader = new SkipCommentReader(reader);
    }

    public void testFirstCSSFile() throws IOException {
        initiateReader("skin/test_1.css");
        skipCommentReader.setCommentBlockHandler(new CommentBlockHandler.OrientationCommentBlockHandler());

        for (int i = 0; i < 30; i++) {
            String line = skipCommentReader.readLine();
            System.out.println(line);
            line = skipCommentReader.readLine();
        }
    }

    public void testSkipCommentBlock() throws IOException {
        Reader reader = new StringReader("abcdefgh/* comment block */ijklmn");
        initiateReader(reader);

        String line = skipCommentReader.readLine();
        assertEquals("abcdefghijklmn", line);
    }

    public void testSkipMultipleCommentBlocks() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("1.abcdefgh/* comment block */ijklmn\n");
        builder.append("2.abcdefgh/* comment block */ijklmn\n");
        builder.append("3.abcdefgh/* comment block */ijklmn\n");
        builder.append("4.abcdefgh/* comment block */ijklmn\n");

        Reader reader = new StringReader(builder.toString());
        initiateReader(reader);

        String line = skipCommentReader.readLine();
        assertEquals("1.abcdefghijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("2.abcdefghijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("3.abcdefghijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("4.abcdefghijklmn", line);
    }

    public void testSkipCommentBlocksWithHandler() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("1.abcdefgh/* orientation=lt */ijklmn\n");
        builder.append("2.abcdefgh/* comment block */ijklmn\n");
        builder.append("3.abcdefgh/* orientation=rt */ijklmn\n");
        builder.append("4.abcdefgh/* comment block */ijklmn\n");

        Reader reader = new StringReader(builder.toString());
        initiateReader(reader);
        skipCommentReader.setCommentBlockHandler(new CommentBlockHandler.OrientationCommentBlockHandler());

        String line = skipCommentReader.readLine();
        assertEquals("1.abcdefgh/* orientation=lt */ijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("2.abcdefghijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("3.abcdefgh/* orientation=rt */ijklmn", line);

        line = skipCommentReader.readLine();
        assertEquals("4.abcdefghijklmn", line);
    }

    public void testNumberOfCommingEscapes() throws IOException {
        String COMMENT_BLOCK = "/*COMMENT BLOCK*/";

        StringBuilder builder = new StringBuilder();
        builder.append(COMMENT_BLOCK).append("1.abcdefghijklmn\n");
        builder.append("2.abcdefghijklmn\n");
        builder.append("3.").append(COMMENT_BLOCK).append("abcdefghijklmn\n");

        Reader reader = new StringReader(builder.toString());
        initiateReader(reader);

        skipCommentReader.setNumberOfCommingEscapes(COMMENT_BLOCK.length());
        String firstLine = skipCommentReader.readLine();
        assertEquals(COMMENT_BLOCK + "1.abcdefghijklmn", firstLine);

        String secondLine = skipCommentReader.readLine();
        assertEquals("2.abcdefghijklmn", secondLine);

        skipCommentReader.setNumberOfCommingEscapes(2 + COMMENT_BLOCK.length());
        String thirdLine = skipCommentReader.readLine();
        assertEquals("3." + COMMENT_BLOCK + "abcdefghijklmn", thirdLine);
    }

    public void testCursorState() throws IOException {
        Reader reader = new StringReader("0123456//*xxxx*/*789");
        initiateReader(reader);

        assertEquals(SkipCommentReader.State.ENCOUNTING_ORDINARY_CHARACTER, skipCommentReader.getCursorState());
        for (int i = 0; i < 7; i++) {
            skipCommentReader.readSingleCharacter();
            assertEquals(SkipCommentReader.State.ENCOUNTING_ORDINARY_CHARACTER, skipCommentReader.getCursorState());
        }

        skipCommentReader.readSingleCharacter();
        assertEquals(SkipCommentReader.State.ENCOUNTING_FORWARD_SLASH, skipCommentReader.getCursorState());

        skipCommentReader.readSingleCharacter();// The comment block is automatically skipped
        assertEquals(SkipCommentReader.State.ENCOUNTING_ASTERIK, skipCommentReader.getCursorState());
    }
}
