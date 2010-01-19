package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_13 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_13() throws Exception {
selenium.setSpeed("500");
selenium.clickAt("link=Gadget", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@class='UIControlbar']/div[1]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@class='UIControlbar']/div[1]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("url")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("url", "http://www.google.com/ig/modules/colorjunction.xml");
selenium.clickAt("link=Add", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Gadget Details")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIGadgetInfo']//div[@class='UIBreadcumb']/div[@class='DownLoadIcon ControlIcon']", "1,1");
System.out.println("https://jira.jboss.org/jira/browse/GTNPORTAL-439");
assertTrue(selenium.isTextPresent("Gadget Details"));
}

}
