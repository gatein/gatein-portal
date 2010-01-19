package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_35 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_35() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.clickAt("link=Sign in", "1,1");
selenium.open("/portal/private/classic/administration/pageManagement");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageBrowser']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("name")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.type("name", "user_page1");
selenium.type("title", "user_page_title1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.select("ownerType", "label=portal");
selenium.type("name", "portal_page1");
selenium.type("title", "portal_page_title1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("//div[@id='UIPageBrowser']//div[@class='UIAction']//div[@class='ActionButton LightBlueStyle']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.select("ownerType", "label=group");
selenium.clickAt("//option[@value='group']", "1,1");
selenium.type("name", "group_page1");
selenium.type("title", "group_page_title1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIRepeater']//img[@class='EditInfoIcon']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIRepeater']//img[@class='EditInfoIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageEditor']//div[@class='PageProfileIcon']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageEditor']//div[@class='PageProfileIcon']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='TabsContainer']//div[3]//div[@class='MiddleTab']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIListPermissionSelector']//input[@class='checkbox']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIListPermissionSelector']//input[@class='checkbox']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Save")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@id='UIPageEditor']//div[@class='OverflowContainer']/a[@class='EdittedSaveButton']", "1,1");
assertTrue(selenium.isTextPresent("user_page_title1"));
assertTrue(selenium.isTextPresent("group_page_title1"));
assertTrue(selenium.isTextPresent("portal_page_title1"));
}

}
