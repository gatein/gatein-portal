package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_21 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_21() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/public/classic/");
System.out.println("-ChangePortal-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
selenium.clickAt("link=Site", "1,1");
System.out.println("--Add new portal");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Add New Portal")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Add New Portal", "1,1");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PortalSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=PortalSetting')\"]", "1,1");
selenium.type("name", "test_portal_name_21");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=Properties')\"]", "1,1");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=PermissionSetting')\"]", "1,1");
selenium.clickAt("publicMode", "1,1");
selenium.clickAt("link=Edit Permission Setting", "1,1");
selenium.clickAt("link=Select Permission", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("link=exact:*", "1,1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
assertTrue(selenium.isTextPresent("test_portal_name_21"));
System.out.println("--View new portal");
selenium.clickAt("link=test_portal_name_21", "1,1");
}

}
