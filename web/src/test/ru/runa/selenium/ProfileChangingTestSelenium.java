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
 * Created on 10.07.2008
 *
 * @author Konstantinov A
 */
public class ProfileChangingTestSelenium extends BaseSeleniumTestClass {

    public void testGroupExpanding() throws Exception {
        getSelenium().open("http://localhost:8080/wfe/");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(manager, "123");
        startReportProcess("test1");
        startReportProcess("test2");
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("editableFieldsValues", "report_theme");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("//input[@name='fieldsToGroupIds' and @value='0']");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "batch_presentation.task.variable", " 'report_theme': test1"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "batch_presentation.task.variable", " 'report_theme': test2"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=make report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("report", "rep1");
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=make report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("report", "rep2");

        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("//input[@name='fieldsToGroupIds' and @value='5']");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().select("displayPositionsIds", "label=" + getPageElementCorrespondMainMenu("label.none"));
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "button.logout", ""));

        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LoginUser(manager, "123");
        getSelenium().click("link=read report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=read report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("document.processForm.submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "button.logout", ""));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }
}
