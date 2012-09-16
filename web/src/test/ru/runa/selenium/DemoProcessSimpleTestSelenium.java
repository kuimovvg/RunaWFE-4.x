/*This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.selenium;

/**
 * Created on 8.06.2008
 *
 * @author Konstantinov A
 */
public class DemoProcessSimpleTestSelenium extends BaseSeleniumTestClass {

    public void testReportProcess() throws Exception {
        getSelenium().open("http://localhost:8080/wfe/");
        LoginUser(manager, "123");
        startReportProcess("selenium test report process request");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=make report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("report", "selenium test report response");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(manager, "123");
        getSelenium().click("link=read report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    public void testOvertimeProcess() throws Exception {
        getSelenium().open("http://localhost:8080/wfe/");
        LoginUser(manager, "123");
        getSelenium().click(getPageElement("//a[text()='", "manage_definitions", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Overtime Work");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("//input[@name='since']", "07.06.2008 12:45");
        getSelenium().type("//input[@name='till']", "07.06.2008 13:45");
        getSelenium().type("reason", "selenium overtime reason");
        getSelenium().type("comment", "selenium overtime manager comments");
        getSelenium().click("submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=Make a decision");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("document.processForm.elements['staff_person_comment']", "selenium overtime emploee comment accepted");
        getSelenium().click("//input[@value='accepted']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(manager, "123");
        getSelenium().click("link=Notify of acceptance");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    public void testBusinesstripProcess() throws Exception {
        getSelenium().open("http://localhost:8080/wfe/");
        LoginUser(manager, "123");
        startBusinesstripProcess(true, "selenium bussiness trip reason", "selenium bussines trip comment from boss");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=Confirm business trip notification");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(HR, "123");
        getSelenium().click("link=Make an order");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("document.processForm.elements['official_order_number']", "12344321");
        getSelenium().type("//input[@name='official_order_date']", "09.06.2008");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Receive a signature on the order");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=Sign the order");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Take a business trip warrant");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(HR, "123");
        getSelenium().click("link=Give a business trip warrant");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(bookkeeper, "123");
        getSelenium().click("link=Give business trip money");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=Take business trip money");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Give a business trip financial report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(bookkeeper, "123");
        getSelenium().click("link=Take a business trip financial report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    public void testVacationProcess() throws Exception {
        getSelenium().open("http://localhost:8080/wfe/");
        LoginUser(stuff, "123");
        getSelenium().click(getPageElement("//a[text()='", "manage_definitions", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Vacation");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("//input[@name='since']", "09.06.2008");
        getSelenium().type("//input[@name='till']", "09.06.2008");
        getSelenium().type("reason", "selenium vacation reason");
        getSelenium().type("comment", "selenium vacation request");
        getSelenium().click("submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(manager, "123");
        getSelenium().click("link=Evaluate a request");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("document.processForm.elements['boss_comment']", "cool");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(HR, "123");
        getSelenium().click("link=Check rules and technologies");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("document.processForm.elements['human_resource_inspector_comment']", "all good, go");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Receive a hardcopy request");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("link=Submit a hardcopy request");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(HR, "123");
        getSelenium().click("link=Make an official order");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("//input[@name='official order number']", "12344321");
        getSelenium().type("//input[@name='official order date']", "09.06.2008");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTextMessage("task.completed");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }
}
