package org.exoplatform.portal.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;
public class Test_SNF_PRL_34 extends SeleneseTestCase {
public String speed = "1000";
public String browser = "firefox";
public void setSpeed() {
selenium.setSpeed(speed);
}

public void setUp() throws Exception {
setUp("http://localhost:8080/portal/", "*" + browser);
}

public void testSNF_PRL_34() throws Exception {
setSpeed();
selenium.open("/portal/public/classic");
System.out.println("-LogoPortletAccSetting-");
selenium.clickAt("link=Sign in", "1,1");
selenium.type("username", "root");
selenium.type("password", "gtn");
selenium.click("//div[@id='UIPortalLoginFormAction']/div/div/div/a");
selenium.waitForPageToLoad("30000");
selenium.clickAt("link=Dashboard", "1,1");
System.out.println("--Edit Logo Picture");
selenium.clickAt("link=Edit Layout", "1,1");
selenium.clickAt("//form[@id='UILogoEditMode']/div[2]/div/div/table/tbody/tr/td/div/div/div/div", "1,1");
selenium.type("logoUrl", "url to define !!");
selenium.clickAt("link=Save", "1,1");
selenium.clickAt("link=Close", "1,1");
selenium.clickAt("//div[@id='UIPortalComposer']/div[1]/div/div/div/a[2]", "1,1");
assertTrue(selenium.isElementPresent("//div[@id='UILogoPortlet']/a/img[@src='/eXoResources/skin/sharedImages/DashboardIcon.png']"));
selenium.clickAt("link=Sign out", "1,1");
}

}
