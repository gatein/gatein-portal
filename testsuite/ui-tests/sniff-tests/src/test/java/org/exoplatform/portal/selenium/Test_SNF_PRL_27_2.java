package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_27_2 extends SeleneseTestCase {
public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*firefox");
}

public void testSNF_PRL_27_2() throws Exception {
selenium.setSpeed("500");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div");
selenium.waitForPageToLoad("30000");
selenium.click("link=Group");
selenium.waitForPageToLoad("30000");
selenium.open("/portal/private/classic/");
selenium.clickAt("link=Edit Layout", "1,1");
selenium.clickAt("link=Portal Properties", "1,1");
selenium.select("locale", "label=English");
selenium.clickAt("//div[@id='UIMaskWorkspace']//div[3]//div[@class='MiddleTab']", "1,1");
selenium.clickAt("publicMode", "1,1");
selenium.clickAt("link=Edit Permission Setting", "1,1");
for (int second = 0;; second++) {
if (second >= 30) fail("timeout");
try {
 if (selenium.isTextPresent("Current Permission")) 
break; }
 catch (Exception e) {}
Thread.sleep(1000);
}
selenium.clickAt("link=Select Permission", "1,1");
selenium.click("link=Select Permission");
selenium.clickAt("//div[@id='PermissionSelector']/div/div[2]/div/div[2]/div/div/div[1]/a", "1,1");
selenium.clickAt("link=Platform", "1,1");
selenium.clickAt("link=exact:*", "1,1");
selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[2]", "1,1");
selenium.clickAt("link=Save", "1,1");
}

}
