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

package org.exoplatform.test.crawler;

/**
 * Created by The eXo Platform SARL
 * Author : Lai Van Khoi
 *          laivankhoi46pm1@yahoo.com
 * Dec 1, 2006  
 */
public class SimpleCrawlerExample
{
   public static void main(String[] args) throws Exception
   {
      CrawlerService crawl = new CrawlerService();
      crawl.startCrawl("http://www.vnexpress.net/Vietnam/Home/", "utf-8",
         "BODY[0].TABLE[0].TBODY[0].TR[0].TD[0].TABLE[1].TBODY[0].TR[0].TD[2]",
         "BODY[0].TABLE[0].TBODY[0].TR[0].TD[0].TABLE[1].TBODY[0].TR[0].TD[2].TABLE[0].TBODY[0].TR[1].TD[0]");

   }
}
