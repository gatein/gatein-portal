package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_28 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_28() throws Exception {
setSpeed();
selenium.open("/portal/public/classic");
System.out.println("-ActionsDashboardpage-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.clickAt("link=Dashboard", "1,1");
System.out.println("--Add gadgets into dashboard page");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Gadgets")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Gadgets", "1,1");
System.out.println("--By url");
selenium.type("//input[@id='url']", "http://www.google.com/ig/modules/datetime.xml");
selenium.clickAt("//img[@class='AddNewNodeIcon']", "1,1");
selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
assertTrue(selenium.isElementPresent("//div[@class='GadgetTitle']"));
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@class='CloseGadget IconControl' and @title='Delete Gadget']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@class='CloseGadget IconControl' and @title='Delete Gadget']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Are you sure to delete this gadget?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add Gadgets")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add Gadgets", "1,1");
System.out.println("--By drag and drop");
selenium.dragAndDropToObject("//div[@class='GadgetTitle' and @title='Calculator']","//div[@class='GadgetTitle' and @title='Calculator']");
selenium.clickAt("//div[@id='UIAddGadgetPopup']//div[@class='CloseButton']", "1,1");
assertTrue(selenium.isElementPresent("//div[@class='GadgetTitle']"));
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@class='CloseGadget IconControl']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("//div[@title='Delete Gadget']", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
if (selenium.getConfirmation().equals("Are you sure to delete this gadget?")) {
break;
}
}
catch (Exception e) {
}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("Drag your gadgets here."));
selenium.clickAt("link=Sign out", "1,1");
}

}
