package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_27_1 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_27_1() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=Page Management", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageBrowser']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add New Page", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("pageName")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("pageName", "newpage21211");
selenium.type("pageDisplayName", "new page21211");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
selenium.clickAt("//div[@id='UIPageCreationWizard']//div[@class='UIAction']//div[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='Administration/AccountPortlet']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=View Page properties", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Show Max Window")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
selenium.clickAt("link=Edit Permission Setting", "1,1");
selenium.clickAt("link=Cancel", "1,1");
selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//img[@alt='']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Edit Page", "1,1");
selenium.clickAt("link=View Page properties", "1,1");
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
selenium.clickAt("link=Add Permission", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("//div[@id='UIPageFormPopupGroupMembershipSelector']//div[@class='MembershipSelector']//a", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageEditor']/div[1]/div/div/div/a[2]", "1,1");
}

}
