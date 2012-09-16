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

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.security.auth.Subject;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.web.action.StrutsTestCase;
import ru.runa.delegate.DelegateFactory;
import ru.runa.delegate.impl.InitializerServiceDelegateRemoteImpl;
import ru.runa.delegate.impl.WfeScriptClient;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Created on 10.07.2008
 *
 * @author Konstantinov A
 */
public class BaseSeleniumTestClass extends StrutsTestCase {
    private Selenium sel;

    protected String manager = "julius";
    protected String stuff = "marcus";
    protected String HR = "octavia";
    protected String bookkeeper = "caligula";

    public String getTestPrefix() {
        return DemoProcessSimpleTestSelenium.class.getName();
    }

    protected void setUp() throws Exception {
        new InitializerServiceDelegateRemoteImpl().init(true, false);
        WfeScriptClient.main(new String[] { "../adminkit/scripts/deploy-samples-script.xml", "Administrator", "wf" });
        super.setUp();
        sel = new DefaultSelenium("localhost", 3344, "*iehta", "http://localhost:8080/wfe");
        sel.start();
    }

    public void tearDown() throws Exception {
        sel.stop();
        super.tearDown();
    }

    protected Selenium getSelenium() {
        return sel;
    }

    protected final String pageLoadTimeout = "60000";

    protected String getPageElement(String prolog, String param, String epilog) {
        String current = prolog + ResourceBundle.getBundle("struts").getString(param) + epilog;
        current = current.replaceAll("&nbsp;", " ");
        current = current.replaceAll("<BR>", "\n");
        if (getSelenium().isElementPresent(current)) {
            return current;
        }

        return prolog + ResourceBundle.getBundle("struts", Locale.ENGLISH).getString(param) + epilog;
    }

    protected String getPageElementCorrespondMainMenu(String str) {
        String current = "//a[text()='" + ResourceBundle.getBundle("struts").getString("manage_definitions") + "']";
        current = current.replaceAll("&nbsp;", " ");
        current = current.replaceAll("<BR>", "\n");
        if (getSelenium().isElementPresent(current)) {
            return ResourceBundle.getBundle("struts").getString(str);
        }
        return ResourceBundle.getBundle("struts", Locale.ENGLISH).getString(str);
    }

    protected void checkTextMessage(String message) {
        if (ResourceBundle.getBundle("struts").getString(message).equals(getSelenium().getText("//font"))
                || ResourceBundle.getBundle("struts", Locale.ENGLISH).getString(message).equals(getSelenium().getText("//font"))) {
            return;
        }
        throw new InternalApplicationException("expected message '" + ResourceBundle.getBundle("struts").getString(message) + "' or '"
                + ResourceBundle.getBundle("struts", Locale.ENGLISH).getString(message) + "' not found");
    }

    protected void LoginUser(String userName, String userPass) {
        getSelenium().type("login", userName);
        getSelenium().type("password", userPass);
        getSelenium().click(getPageElement("//input[@value='", "login.page.login.button", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void LogoutUser() {
        getSelenium().click(getPageElement("//a[text()='", "button.logout", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void startReportProcess(String report_theme) {
        getSelenium().click(getPageElement("//a[text()='", "manage_definitions", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Report");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("report_theme", report_theme);
        getSelenium().click("submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void startBusinesstripProcess(boolean isAnotherRegion, String reason, String comment) {
        getSelenium().click(getPageElement("//a[text()='", "manage_definitions", "']"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("link=Businesstrip");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("//input[@name='since']", "09.06.2008");
        getSelenium().type("//input[@name='till']", "10.06.2008");
        if (isAnotherRegion) {
            getSelenium().select("businessTripType", "label=to another region");
        }
        getSelenium().type("reason", reason);
        getSelenium().type("comment", comment);
        getSelenium().click("submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected boolean isCheckboxChecked(String id) {
        return getSelenium().getValue(id).equals("on");
    }

    protected void setDisplayPosition(int fieldIdx, int displayPosition) {
        getSelenium().select("//select[@name='displayPositionsIds' and ../../td/input[@name='ids' and @value='" + fieldIdx + "']]",
                "label=" + ((displayPosition == -1) ? getPageElementCorrespondMainMenu("label.none") : Integer.toString(displayPosition)));
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void setDisplayPositions(int[] displayPositions) {
        for (int i = 0; i < displayPositions.length; ++i) {
            getSelenium()
                    .select(
                            "//select[@name='displayPositionsIds' and ../../td/input[@name='ids' and @value='" + i + "']]",
                            "label="
                                    + ((displayPositions[i] == -1) ? getPageElementCorrespondMainMenu("label.none") : Integer
                                            .toString(displayPositions[i])));
            getSelenium().click("dispatch");
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
    }

    protected void setSortingPosition(int fieldIdx, int sortPosition) {
        getSelenium().select("//select[@name='sortPositionsIds' and ../../td/input[@name='ids' and @value='" + fieldIdx + "']]",
                "label=" + ((sortPosition == -1) ? getPageElementCorrespondMainMenu("label.none") : Integer.toString(sortPosition)));
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void setSortingPositions(int[] sortPositions) {
        for (int i = 0; i < sortPositions.length; ++i) {
            if (i == 6) {
                continue;
            }
            getSelenium().select("//select[@name='sortPositionsIds' and ../../td/input[@name='ids' and @value='" + i + "']]",
                    "label=" + ((sortPositions[i] == -1) ? getPageElementCorrespondMainMenu("label.none") : Integer.toString(sortPositions[i])));
            getSelenium().click("dispatch");
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
    }

    protected void setSortingMode(int fieldIdx, boolean isDecrase) {
        getSelenium().select("//select[@name='sortingModeNames' and ../../td/input[@name='ids' and @value='" + fieldIdx + "']]",
                "label=" + getPageElementCorrespondMainMenu(isDecrase ? "label.desc" : "label.asc"));
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void setSortingModes(boolean[] sortModes) {
        for (int i = 0; i < sortModes.length; ++i) {
            getSelenium().select("//select[@name='sortingModeNames' and ../../td/input[@name='ids' and @value='" + i + "']]",
                    "label=" + getPageElementCorrespondMainMenu(sortModes[i] ? "label.desc" : "label.asc"));
            getSelenium().click("dispatch");
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
    }

    protected void setGroupCheckbox(int fieldIdx, boolean isGrouped) {
        if (getSelenium().isChecked("//input[@name='fieldsToGroupIds' and @value='" + fieldIdx + "']") != isGrouped) {
            getSelenium().click("//input[@name='fieldsToGroupIds' and @value='" + fieldIdx + "']");
        }
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void unsetFilterCriteria(int fieldIdx) {
        if (getSelenium().isChecked("//input[@name='fieldsToFilterIds' and @value='" + fieldIdx + "']")) {
            getSelenium().click("//input[@name='fieldsToFilterIds' and @value='" + fieldIdx + "']");
        }
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected void setFilterCriteria(int fieldIdx, String[] criterias) {
        if (getSelenium().isChecked("//input[@name='fieldsToFilterIds' and @value='" + fieldIdx + "']") != true) {
            getSelenium().click("//input[@name='fieldsToFilterIds' and @value='" + fieldIdx + "']");
        } else {
            getSelenium().click("dispatch");
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
        final int maxPositions = 100;
        int pos = 1;
        for (int i = 0; i < criterias.length; ++i) {
            for (; pos < maxPositions; ++pos) {
                if (getSelenium().isElementPresent(
                        "//input[@name='fieldsToFilterCriterias' and ../input[@name='filterPositionsIds' and @value='" + fieldIdx
                                + "'] and position()='" + pos + "']")) {
                    getSelenium().type(
                            "//input[@name='fieldsToFilterCriterias' and ../input[@name='filterPositionsIds' and @value='" + fieldIdx
                                    + "'] and position()='" + pos + "']", criterias[i]);
                    ++pos;
                    getSelenium().click("dispatch");
                    getSelenium().waitForPageToLoad(pageLoadTimeout);
                    break;
                }
            }
        }
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
    }

    protected Subject getSubject(String name) throws Exception {
        return DelegateFactory.getInstance().getAuthenticationService().authenticate(name, "123");
    }

    protected List<TaskStub> getTasks(String name) throws Exception {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        return executionService.getTasks(getSubject(name), DelegateFactory.getInstance().getProfileService().getProfile(getSubject(name))
                .getActiveBatchPresentation(WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID));
    }

    protected Actor getActor(String actorName) throws Exception {
        return ru.runa.delegate.DelegateFactory.getInstance().getExecutorService().getActor(getSubject(actorName), actorName);
    }
}
