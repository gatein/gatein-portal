package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_32 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_32() throws Exception {
setSpeed();
selenium.open("/portal/public/classic");
System.out.println("-ChangeDisplaySkin-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isElementPresent("link=Change Skin")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Change Skin", "1,1");
selenium.clickAt("//div[@id='UITabContent']//div[@class='ItemListContainer']//div[@class='ItemList']//div[@class='SelectedItem Item']", "1,1");
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[@class='ActionButton LightBlueStyle']", "1,1");
selenium.waitForPageToLoad("30000");
System.out.println("--Verify");
System.out.println("---------");
selenium.clickAt("link=Sign out", "1,1");
}

}
