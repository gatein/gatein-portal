package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_20 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_20() throws Exception {
selenium.setSpeed("500");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=Site", "1,1");
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
selenium.type("name", "Haha");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'Properties');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=Properties')\"]", "1,1");
selenium.clickAt("//div[@onclick=\"eXo.webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, 'UIPortalForm', 'PermissionSetting');javascript:eXo.webui.UIForm.submitEvent('UIPortalForm','SelectTab','&amp;objectId=PermissionSetting')\"]", "1,1");
selenium.clickAt("publicMode", "1,1");
selenium.clickAt("link=Edit Permission Setting", "1,1");
selenium.clickAt("link=Select Permission", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("link=Platform", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@class='CollapseIcon']")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=exact:*", "1,1");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Save", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.click("//div[@id='UISiteManagement']/table[2]/tbody/tr/td[3]/a[4]");
selenium.waitForPageToLoad("30000");
assertFalse(selenium.isTextPresent("Haha"));
}

}
