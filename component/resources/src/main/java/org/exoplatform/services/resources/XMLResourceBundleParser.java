/**
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

package org.exoplatform.services.resources;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A parser for XML resource bundle having the following rules:
 *
 * <ul>
 * <li>The root document element is named "bundle"</li>
 * <li>Any non root element can have any name</li>
 * <li>Any non root element content must not have mixed content (i.e text and children elements)</li>
 * <li>Any element having textual content is considered as a bundle entry with they key formed by the
 *     dot concatenation of its parent element name except the root element and the value is the text content</li>
 * </ul>
 *
 * For instance the following document:
 *
 * &lt;bundle&gt;
 * &lt;foo&gt;
 * &lt;A&gt;1&lt;A&gt;
 * &lt;B&gt;2&lt;B&gt;
 * &lt;/foo&gt;
 * &lt;C&gt;3&lt;C&gt;
 * &lt;/bundle&gt;
 *
 * will give the bundle with entries:
 *
 * <ul>
 * <li>foo.A=1</li>
 * <li>foo.B=2</li>
 * <li>C=3</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class XMLResourceBundleParser
{

   /**
    * @see #asMap(org.xml.sax.InputSource)
    */
   public static Properties asProperties(InputStream in) throws IOException, SAXException,
      ParserConfigurationException, IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null input stream allowed");
      }
      return asProperties(new InputSource(in));
   }

   /**
    * @see #asMap(org.xml.sax.InputSource)
    */
   public static Properties asProperties(Reader in) throws IOException, SAXException, ParserConfigurationException,
      IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null reader allowed");
      }
      return asProperties(new InputSource(in));
   }

   /**
    * @see #asMap(org.xml.sax.InputSource)
    */
   public static Properties asProperties(InputSource in) throws IOException, SAXException,
      ParserConfigurationException, IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null input source allowed");
      }
      Map<String, String> bundle = asMap(in);
      Properties props = new Properties();
      props.putAll(bundle);
      return props;
   }

   /**
    * @see #asMap(org.xml.sax.InputSource)
    */
   public static Map<String, String> asMap(InputStream in) throws IOException, SAXException,
      ParserConfigurationException, IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null input stream allowed");
      }
      return asMap(new InputSource(in));
   }

   /**
    * @see #asMap(org.xml.sax.InputSource)
    */
   public static Map<String, String> asMap(Reader in) throws IOException, SAXException, ParserConfigurationException,
      IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null reader allowed");
      }
      return asMap(new InputSource(in));
   }

   /**
    * Load an xml resource bundle as a {@link Map<String,String>} object.
    *
    * @param in the input source
    * @return the properties object
    * @throws IOException any IOException
    * @throws SAXException any SAXException
    * @throws ParserConfigurationException any ParserConfigurationException
    * @throws IllegalArgumentException if the argument is null
    */
   public static Map<String, String> asMap(InputSource in) throws IOException, SAXException,
      ParserConfigurationException, IllegalArgumentException
   {
      if (in == null)
      {
         throw new IllegalArgumentException("No null input source allowed");
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(in);
      Element bundleElt = document.getDocumentElement();
      HashMap<String, String> bundle = new HashMap<String, String>();
      collect(new LinkedList<String>(), bundleElt, bundle);
      return bundle;
   }

   private static void collect(LinkedList<String> path, Element currentElt, Map<String, String> bundle)
   {
      NodeList children = currentElt.getChildNodes();
      boolean text = true;
      for (int i = children.getLength() - 1; i >= 0; i--)
      {
         Node child = children.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            text = false;
            Element childElt = (Element)child;
            String name = childElt.getTagName();
            path.addLast(name);
            collect(path, childElt, bundle);
            path.removeLast();
         }
      }
      if (text && path.size() > 0)
      {
         String value = currentElt.getTextContent();
         StringBuffer sb = new StringBuffer();
         for (Iterator<String> i = path.iterator(); i.hasNext();)
         {
            String name = i.next();
            sb.append(name);
            if (i.hasNext())
            {
               sb.append('.');
            }
         }
         String key = sb.toString();
         bundle.put(key, value);
      }
   }
}
