package org.jboss.portal.portlet.samples.util;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class SimpleHtmlExtractor
{
   public static String extractAttribute(String htmlFragment, String attr)
   {
      String srch = " " + attr + "=";
      int pos = htmlFragment.indexOf(srch);
      if (pos == -1)
      {
         return null;
      }
      String[] segments = htmlFragment.substring(pos + srch.length() + 1).split("[\"\']");
      if (segments.length == 0)
      {
         return null;
      }

      return segments[0];
   }

   public static String removeElements(String htmlFragment, String ... elements)
   {
      if (elements.length == 0)
      {
         StringBuilder sb = new StringBuilder(" ");
         String[] noTags = htmlFragment.split("<[^>]+>");
         for (int i = 0; i < noTags.length; i++)
         {
            if (!"".equals(noTags[i]))
            {
               sb.append(noTags[i]);
            }
         }
         return sb.toString();
      }

      return htmlFragment;
   }
}
