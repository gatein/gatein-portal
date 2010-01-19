package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_30 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_30() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add New Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add New Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageNodeSelector']//div[@class='HomeNode']/a")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageNodeSelector']//div[@class='HomeNode']/a", "1,1");
selenium.type("pageName", "test9");
selenium.type("pageDisplayName", "test9");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=View Page properties")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=View Page properties", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("title")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("title", "test9_changed");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("css=a.EdittedSaveButton", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Edit Page")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=View Page properties")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=View Page properties", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("title")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertEquals("test9_changed", selenium.getValue("title"));
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[3]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']/div[3]//div[@class='MiddleTab']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("publicMode")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("publicMode", "1,1");
selenium.clickAt("link=Add Permission", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Platform")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Platform", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Administrators")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Administrators", "1,1");
selenium.clickAt("link=exact:*", "1,1");
selenium.clickAt("link=Edit Permission Setting", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Select Permission")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Select Permission", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Platform")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("//div[@id='PermissionSelector']//div[@class='MembershipSelector']//div[@class='OverflowContainer']/div[3]/a", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageEditor']//div[@class='TLPortalComposer']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']", "1,1");
assertTrue(selenium.isTextPresent("test9"));
selenium.open("/portal/private/classic/");
}

}
