package ru.runa.selenium;

import java.util.Arrays;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.InternalApplicationException;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.TaskClassPresentation;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.web.html.TaskDeadlineTDBuilder;
import ru.runa.wf.web.html.TaskRoleTDBuilder;
import ru.runa.wf.web.html.TaskVariableTDBuilder;

public class ListTaskTestSelenium extends BaseSeleniumTestClass {

    public void testHideDisplayedColumns() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {0,1,2,3,4,5,6,7}));
        setDisplayPosition(0, -1); // TaskName field invisible
        assertFalse("Name is set undisplayed, but link='make report' is found", getSelenium().isElementPresent("link=make report"));
        assertFalse("Name is set undisplayed, but link='Confirm business trip notification' is found", getSelenium().isElementPresent(
                "link=Confirm business trip notification"));
        checkTasksDisplayed(stuff);
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,3,4,5,6,7 }));
        setDisplayPosition(5, -1); // Role field invisible
        assertFalse("Role is set undisplayed, but link = stuff is found.", getSelenium().isElementPresent("link=stuff"));
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,3,4,6,7}));
        setDisplayPosition(4, -1); // Owner field invisible
        assertFalse("/owner is set undisplayed, but link=attila is found.", getSelenium().isElementPresent("link=attila[../td]"));
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,3,6,7}));
        setDisplayPosition(3, -1); // Process id field invisible
        for (TaskStub task : getTasks(stuff)) {
            assertFalse("Process instance id is set undissplayd, but link for process id " + task.getProcessInstanceId() + " id found.",
                    getSelenium().isElementPresent("link=" + task.getProcessInstanceId()));
        }
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,6,7}));
        setDisplayPosition(2, -1); // ProcessName field invisible
        assertFalse("Process name is set undisplayable, but link=Report is found.", getSelenium().isElementPresent("link=Report"));
        assertFalse("Process name is set undisplayable, but link=Buisinesstrip is found.", getSelenium().isElementPresent("link=Businesstrip"));
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,6,7}));
        setDisplayPosition(1, -1); // Description field invisible
        assertFalse("Descrption is set undisplayed, but link=Task for report making is found", getSelenium().isElementPresent(
                "link=Task for report making"));
        assertFalse("Descrption is set undisplayed, but link=Employee confirms that business trip notification is received is found", getSelenium()
                .isElementPresent("link=Employee confirms that business trip notification is received"));
        getSelenium().type("editableFieldsValues", "report_theme");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setDisplayPosition(0, 1); // Variable filter field visible
        assertTrue("Couldn't find variable test1.", getSelenium().isTextPresent("test1"));
        assertTrue("Couldn't find variable test2.", getSelenium().isTextPresent("test2"));
        checkTasksDisplayed("attila");
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {6,7}));
        setDisplayPosition(0, -1); // Variable filter field invisible
        assertFalse("Variable test1 is found.", getSelenium().isTextPresent("test1"));
        assertFalse("Variable test2 is found.", getSelenium().isTextPresent("test2"));
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {7}));
        LogoutUser();
    }

    public void testColumnsOrder() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        checkTasksDisplayed(stuff);
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().type("editableFieldsValues", "report_theme");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setDisplayPosition(0, 1); // Variable filter field visible
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {0,1,2,3,4,5,6,7}));
        setDisplayPositions(new int[] { 2, 3, 1, 4, 5, 6, 8 });
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {2,0,1,3,4,5,6,7}));
        setDisplayPosition(4, -1); // Owner filter field invisible
        setDisplayPosition(5, -1); // Role filter field invisible            4
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {2,0,1,3,6,7}));
        setDisplayPosition(0, -1); // variable filter field invisible
        checkTasksDisplayed(stuff);
        //assertTrue("Incorrect fields to display indexes.", Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {2,0,1,3,6}));
        LogoutUser();
    }

    public void testSorting() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        checkTasksDisplayed(stuff);
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        setSortingMode(0, true);
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] {}));
        checkTasksDisplayed(stuff);
        setSortingPosition(0, 1);
        setSortingMode(0, true);
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] { false }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0 }));
        checkTasksDisplayed(stuff);
        setSortingPositions(new int[] { 1, -1, 2, 3, -1, -1, -1 });
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] { false, true, true }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 2, 3 }));
        getSelenium().click("link=" + getPageElementCorrespondMainMenu(TaskClassPresentation.TASK_BATCH_PRESENTATION_DEFINITION_NAME));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] { false, false, true }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 2, 0, 3 }));
        checkTasksDisplayed(stuff);
        getSelenium().click("link=" + getPageElementCorrespondMainMenu(TaskClassPresentation.TASK_BATCH_PRESENTATION_DESCRIPTION));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] { true, false, false, true }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 1, 2, 0, 3 }));
        checkTasksDisplayed(stuff);
        setSortingPositions(new int[] { 1, -1, 2, -1, -1, -1, -1 });
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortModes(), new boolean[] { false, false }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 2 }));
        checkTasksDisplayed(stuff);
        LogoutUser();
    }

    public void testGrouping() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        checkTasksDisplayed(stuff);
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {0,1,2,3,4,5,6,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] {}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] {}));
        setGroupCheckbox(0, true); //Name
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 0 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,3,4,5,6,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0 }));
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        getSelenium().click(getPageElement("link=", TaskClassPresentation.TASK_BATCH_PRESENTATION_NAME, ": make report"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", TaskClassPresentation.TASK_BATCH_PRESENTATION_NAME, ": Confirm business trip notification"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        LogoutUser();
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        setGroupCheckbox(3, true);//ProcessId
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 0, 3 }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 3 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,4,5,6,7}));
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        for (TaskStub task : getTasks(stuff)) {
            getSelenium().click(
                    getPageElement("link=", TaskClassPresentation.TASK_BATCH_PRESENTATION_PROCESS_INSTANCE_ID, ": " + task.getProcessInstanceId()));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
        checkTasksDisplayed(stuff);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        LogoutUser();
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setGroupCheckbox(3, false);//ProcessId
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 0 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {3,1,2,4,5,6,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 3 }));
        checkTasksDisplayed(stuff);
        setGroupCheckbox(3, true);//ProcessId
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 0, 3 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {1,2,4,5,6,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 3 }));
        checkTasksDisplayed(stuff);
        setGroupCheckbox(0, false); //Name
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 3 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {0,1,2,4,5,6,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 3, 0 }));
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        for (TaskStub task : getTasks(stuff)) {
            getSelenium().click(
                    getPageElement("link=", TaskClassPresentation.TASK_BATCH_PRESENTATION_PROCESS_INSTANCE_ID, ": " + task.getProcessInstanceId()));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
        checkTasksDisplayed(stuff);
        getSelenium().type("editableFieldsValues", "report_theme");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setDisplayPosition(0, 1); // Variable filter field visible
        checkTasksDisplayed(stuff);
        assertTrue(getTasks(stuff).size() == 4);
        setGroupCheckbox(0, true);//Variable
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 4, 0 }));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {0,1,2,4,5,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 4, 0, 1 }));
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        setSortingPosition(4, 3); //Process Id -- this lead to Variable=1sort, ProcessId=2sort
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 0, 4, 1 }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 0, 4 }));
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        getSelenium().click(getPageElement("link=", "batch_presentation.task.variable", " 'report_theme':"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "batch_presentation.task.variable", " 'report_theme': test1"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        getSelenium().click(getPageElement("link=", "batch_presentation.task.variable", " 'report_theme': test2"));
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        assertFalse(getSelenium().isElementPresent("link=make report"));
        assertFalse(getSelenium().isElementPresent("link=Confirm business trip notification"));
        for (TaskStub task : getTasks(stuff)) {
            getSelenium().click(
                    getPageElement("link=", TaskClassPresentation.TASK_BATCH_PRESENTATION_PROCESS_INSTANCE_ID, ": " + task.getProcessInstanceId()));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
        checkTasksDisplayed(stuff);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setGroupCheckbox(0, false);//Variable
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] { 4 }));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 4, 0, 1 }));
        setGroupCheckbox(4, false);//ProcessId
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToGroupIds(), new int[] {}));
        //assertTrue(Arrays.equals(getBatchPresentation().getFieldsToDisplayIds(), new int [] {3,6,0,1,2,4,5,7}));
        assertTrue(Arrays.equals(getBatchPresentation().getFieldsToSortIds(), new int[] { 4, 0, 1 }));
        checkTasksDisplayed(stuff);
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        LogoutUser();
    }

    public void testFiltering() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        checkTasksDisplayed(stuff);
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkTasksDisplayed(stuff);
        setFilterCriteria(0, new String[] { "make report" });
        assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(0)));
        assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(0))).getFilterTemplates()[0].equals("make report"));
        assertFalse("Filtering by make report enabled, but Confirm business trip notification found.", getSelenium().isElementPresent(
                "link=Confirm business trip notification"));
        checkTasksDisplayed(stuff);
        LogoutUser();
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(0)));
        assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(0))).getFilterTemplates()[0].equals("make report"));
        assertFalse("Filtering by make report enabled, but Confirm business trip notification found.", getSelenium().isElementPresent(
                "link=Confirm business trip notification"));
        checkTasksDisplayed(stuff);

        {
            unsetFilterCriteria(0);
            setFilterCriteria(1, new String[] { "Task for report making" });
            assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
            assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(1)));
            assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(1))).getFilterTemplates()[0]
                    .equals("Task for report making"));
            assertFalse("Filtering by make report enabled, but Confirm business trip notification found.", getSelenium().isElementPresent(
                    "link=Confirm business trip notification"));
            checkTasksDisplayed(stuff);
            LogoutUser();
            LoginUser(stuff, "123");
            getSelenium().click("//img[@alt='[>]']");
            getSelenium().waitForPageToLoad(pageLoadTimeout);
            assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
            assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(1)));
            assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(1))).getFilterTemplates()[0]
                    .equals("Task for report making"));
            assertFalse("Filtering by make report enabled, but Confirm business trip notification found.", getSelenium().isElementPresent(
                    "link=Confirm business trip notification"));
            checkTasksDisplayed(stuff);
            unsetFilterCriteria(1);
        }

        setFilterCriteria(0, new String[] { "Confirm business trip notification" });
        assertFalse("Filtering by Confirm business trip notification enabled, but make report.", getSelenium().isElementPresent("link=make report"));
        checkTasksDisplayed(stuff);
        unsetFilterCriteria(0);
        assertTrue("All tasks must be visible.", getSelenium().isElementPresent("link=make report"));
        assertTrue("All tasks must be visible.", getSelenium().isElementPresent("link=Confirm business trip notification"));
        checkTasksDisplayed(stuff);
        getSelenium().type("editableFieldsValues", "report_theme");
        getSelenium().click("dispatch");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setDisplayPosition(0, 1); // Variable filter field visible
        checkTasksDisplayed(stuff);
        setFilterCriteria(0, new String[] { "test1" });
        assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(0)));
        assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(0))).getFilterTemplates()[0].equals("test1"));
        assertTrue(getTasks(stuff).size() == 1);
        checkTasksDisplayed(stuff);
        setFilterCriteria(0, new String[] { "test2" });
        assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(0)));
        assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(0))).getFilterTemplates()[0].equals("test2"));
        assertTrue(getTasks(stuff).size() == 2);
        checkTasksDisplayed(stuff);
        LogoutUser();
        LoginUser(stuff, "123");
        assertTrue(getBatchPresentation().getFilteredFieldsMap().size() == 1);
        assertTrue(getBatchPresentation().getFilteredFieldsMap().containsKey(new Integer(0)));
        assertTrue((getBatchPresentation().getFilteredFieldsMap().get(new Integer(0))).getFilterTemplates()[0].equals("test2"));
        assertTrue(getTasks(stuff).size() == 2);
        checkTasksDisplayed(stuff);
        LogoutUser();
    }

    public void testAssignTask() throws Exception {
        getSelenium().open("/wfe/");
        initTestConfiguration();
        LoginUser(stuff, "123");
        getSelenium().click("//img[@alt='[>]']");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        setSortingPosition(3, 1);
        checkAssignTaskCheckboxes(new boolean[] { false, false, false, true }, getTasks(stuff), getActor(stuff));
        checkTasksDisplayed(stuff);
        for (TaskStub task : getTasks(stuff)) {
            getSelenium()
                    .check("//input[@name='strIds' and @value='" + task.getId() + ":" + task.getName() + ":" + getActor(stuff).getId() + "']");
        }
        getSelenium().click("submitButton");
        getSelenium().waitForPageToLoad(pageLoadTimeout);
        checkAssignTaskCheckboxes(new boolean[] { true, true, true, true }, getTasks(stuff), getActor(stuff));
        LogoutUser();
        LoginUser(stuff, "123");
        checkAssignTaskCheckboxes(new boolean[] { true, true, true, true }, getTasks(stuff), getActor(stuff));
        for (TaskStub task : getTasks(stuff)) {
            assertTrue("All task must be assigned to attila. (" + task.getProcessInstanceId() + ")", task.getTargetActorName().equals(stuff));
        }
        assertTrue("All task must be assigned to marcus. No task for gaiua", getTasks("gaiua").size() == 0);
        assertFalse("All tasks is assigned to marcus and assignTask button must be disabled.", getSelenium().isEditable("submitButton"));
    }

    public class EnvImpl extends EnvBaseImpl {

        public String getConfirmationMessage() {
            return null;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            try {
                return DelegateFactory.getInstance().getProfileService().getProfile(getSubject()).getActiveBatchPresentation(
                        WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public PageContext getPageContext() {
            return null;
        }

        @Override
        public Subject getSubject() {
            try {
                return DelegateFactory.getInstance().getAuthenticationService().authenticate(stuff, "123");
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String getURL(Object object) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) throws AuthorizationException, AuthenticationException {
            return false;
        }

        @Override
        public Object getTaskVariable(Object object, IdentifiableExtractor taskIdExtractor, String variableName) {
            try {
                return DelegateFactory.getInstance().getExecutionService().getVariable(getSubject(),
                        ((TaskStub) object).getId(), variableName);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return "";
        }
    }

    protected void initTestConfiguration() throws Exception {
        LoginUser("nero", "123");
        startReportProcess("test1");
        startReportProcess("test2");
        startReportProcess("test2");
        startBusinesstripProcess(false, "Bissenestrip reason", "Bissenesstrip boss comment");
        LogoutUser();
    }

    protected Subject getSubject() throws Exception {
        return DelegateFactory.getInstance().getAuthenticationService().authenticate(stuff, "123");
    }

    protected void checkAssignTaskCheckboxes(boolean[] isChecked, List<TaskStub> tasks, Actor actor) {
        if (tasks.size() != isChecked.length) {
            throw new InternalApplicationException("Task count not equals to expected.");
        }
        for (int i = 0; i < isChecked.length; ++i) {
            String elementXPath = "//input[@name='strIds' and @value='" + tasks.get(i).getId() + ":" + tasks.get(i).getName() + ":" + actor.getId() + "']";
            boolean b = isCheckboxChecked(elementXPath);
            getSelenium().click(getPageElement("link=", "manage_tasks", ""));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
            if (b != isChecked[i] || getSelenium().isEditable(elementXPath) == b) {
                throw new InternalApplicationException("Assign task checkbox for task " + tasks.get(i).getName() + " oreder: " + i + "is incorrect ("
                        + !isChecked[i] + ").");
            }
            getSelenium().click(getPageElement("link=", "manage_tasks", ""));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
    }

    protected BatchPresentation getBatchPresentation() throws Exception {
        return DelegateFactory.getInstance().getProfileService().getProfile(getSubject()).getActiveBatchPresentation(
                WFProfileStrategy.PROCESS_TASK_BATCH_PRESENTATION_ID);
    }

    protected void checkTasksDisplayed(String name) throws Exception {
        List<TaskStub> tasks = getTasks(name);
        BatchPresentation batch = getBatchPresentation();
        for (TaskStub task : tasks) {
            StringBuilder element = new StringBuilder();
            element.append("//tr[");
            for (int pos = 0; pos < batch.getDisplayedFieldsDescription().length; ++pos) {
                FieldDescriptor field = batch.getDisplayedFieldsDescription()[pos];
                TDBuilder loadedTDBuilder = (TDBuilder) field.getTDBuilder();
                boolean isLink = true;
                if (loadedTDBuilder instanceof TaskRoleTDBuilder || loadedTDBuilder instanceof TaskVariableTDBuilder
                        || loadedTDBuilder instanceof TaskDeadlineTDBuilder) {
                    isLink = false;
                }
                String value = loadedTDBuilder.getValue(task, new EnvImpl());
                value = value == null ? "" : value;
                if (!isLink) {
                    element.append("td[position()=" + (pos + 2 + batch.getFieldsToGroupIds().length) + " and .='").append(value).append("']");
                } else {
                    element.append("td[position()=" + (pos + 2 + batch.getFieldsToGroupIds().length) + "]/a='").append(value).append("'");
                }
                element.append(" and ");
            }
            String elementStr = element.toString().substring(0, element.length() - 5) + "]";

            if (!getSelenium().isElementPresent(elementStr)) {
                throw new InternalApplicationException("Task presentation is incorrect " + task.getName() + " " + task.getProcessInstanceId());
            }
            getSelenium().click(getPageElement("link=", "manage_tasks", ""));
            getSelenium().waitForPageToLoad(pageLoadTimeout);
        }
    }
}
