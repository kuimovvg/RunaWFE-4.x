/*
 * This file is part of the RUNA WFE project.
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
package ru.runa.batchpresentation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;

import ru.runa.InternalApplicationException;
import ru.runa.af.ASystem;
import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.ExecutorAlreadyInGroupException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.SystemPermission;
import ru.runa.af.UnapplicablePermissionException;
import ru.runa.af.WeakPasswordException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.presentation.filter.FilterFormatException;
import ru.runa.common.web.html.format.FilterFormatsFactory;
import ru.runa.commons.ApplicationContextFactory;
import ru.runa.commons.ArraysCommons;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionNameMismatchException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceStub;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.WorkflowSystemPermission;
import ru.runa.wf.form.VariablesValidationException;
import ru.runa.wf.service.WebWfServiceTestHelper;

import com.google.common.collect.Lists;

/**
 * Created on 24.05.2005
 * 
 */
public class BatchPresentationTestHelper extends WebWfServiceTestHelper {
    public static final int SORT_EXECUTOR_NAME = 0;

    public static final int SORT_EXECUTOR_DESCRIPTION = 1;

    public static final int SORT_EXECUTOR_NAME_DESCRIPTION = 2;

    public static final int SORT_PROCESS_INSTANCE_ID = 0;

    public static final int SORT_PROCESS_INSTANCE_NAME = 1;

    public static final int SORT_PROCESS_INSTANCE_START_DATE = 2;

    public static final int SORT_PROCESS_INSTANCE_END_DATE = 3;

    public static final int SORT_PROCESS_DEFINITION_NAME = 0;

    public static final int SORT_PROCESS_DEFINITION_DESCRIPTION = 1;

    public static final int SORT_PROCESS_DEFINITION_VERSION = 2;

    public static final int SORT_PROCESS_DEFINITION_VERSION_NAME = 3;

    public static final int SORT_TASK_STATE_NAME = 0;

    public static final int SORT_TASK_STATE_DESCRIPTION = 1;

    public static final int SORT_TASK_DEFINITION_NAME = 2;

    private final String[] processDefinitionNames;

    private String[] processDefinitionDescriptions;

    private Long[] processDefinitionVersions;

    private final String[] processDefinitionUrls;

    private final byte[][] processDefinitionBytes;

    private Long[] processInstanceIds;

    private Date[] processInstanceStartDates;

    private Date[] processInstanceEndDates;

    private List<ProcessDefinition> processDefinitions = Lists.newArrayList();

    private List<ProcessInstanceStub> processInstances;

    private List<Group> testGroups;

    private String[] testGroupsNames;

    private String[] testGroupsDescriptions;

    private List<Executor> testExecutors;

    private String[] testExecutorsNames;

    private String[] testExecutorsDescriptions;

    private Executor subjectedActor;

    private final String viewerActorName = "ViewActor" + BatchPresentationTestHelper.class.getName();

    private Subject viewerSubject;

    private Group subjectedGroup;

    private Comparator<String> stringComparator = null;

    private class DefaultStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    private class MSSQLStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.startsWith("_")) {
                return -1;
            }
            if (o2.startsWith("_")) {
                return 1;
            }
            return o1.compareToIgnoreCase(o2);
        }
    }

    private class PostgreStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.startsWith("_")) {
                return -1;
            }
            if (o2.startsWith("_")) {
                return 1;
            }
            return o1.compareToIgnoreCase(o2);
        }
    }

    private class MySQLStringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            if (o1.startsWith("_")) {
                return 1;
            }
            if (o2.startsWith("_")) {
                return -1;
            }
            return o1.compareToIgnoreCase(o2);
        }
    }

    private class OracleStringComparator implements Comparator<String> {
        private int compareCharacter(char c1, char c2) {
            if (c1 == c2) {
                return 0;
            }
            if (c1 >= '0' && c1 <= '9') {
                if (c2 >= '0' && c2 <= '9') {
                    return c1 < c2 ? -1 : 1;
                } else {
                    return 1;
                }
            }
            if (c2 >= '0' && c2 <= '9') {
                return -1;
            }
            if (c1 == '_') {
                return 1;
            }
            if (c2 == '_') {
                return -1;
            }
            return c1 < c2 ? -1 : 1;
        }

        @Override
        public int compare(String o1, String o2) {
            o1 = o1.toUpperCase();
            o2 = o2.toUpperCase();
            int length = o1.length() < o2.length() ? o1.length() : o2.length();
            for (int idx = 0; idx < length; ++idx) {
                int cmp = compareCharacter(o1.charAt(idx), o2.charAt(idx));
                if (cmp == 0) {
                    continue;
                }
                return cmp;
            }
            if (o1.length() == o2.length()) {
                return 0;
            }
            if (o1.length() == length) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public BatchPresentationTestHelper(String prefix) throws IOException, ExecutorOutOfDateException, ExecutorAlreadyExistsException,
            AuthorizationException, AuthenticationException, InternalApplicationException, UnapplicablePermissionException, WeakPasswordException {
        super(prefix);
        processDefinitionUrls = new String[] { "batchTestProcess1.par", "batchTestProcess2.par", "batchTestProcess3.par", "batchTestProcess4.par",
                "batchTestProcess5.par" };
        processDefinitionNames = new String[] { "defins", "Ains_policy", "xixsx", "veirntex", "NoNamed_except_ins" };
        processDefinitionBytes = new byte[processDefinitionUrls.length][];
        for (int i = 0; i < processDefinitionUrls.length; i++) {
            processDefinitionBytes[i] = readBytesFromFile(processDefinitionUrls[i]);
        }
        if (ApplicationContextFactory.getDialectClassName().contains("SQLServerDialect")) {
            stringComparator = new MSSQLStringComparator();
        }
        if (ApplicationContextFactory.getDialectClassName().contains("PostgreSQLDialect")) {
            stringComparator = new PostgreStringComparator();
        }
        if (ApplicationContextFactory.getDialectClassName().contains("MySQLDialect")) {
            stringComparator = new MySQLStringComparator();
        }
        if (ApplicationContextFactory.getDialectClassName().contains("Oracle")) {
            stringComparator = new OracleStringComparator();
        }
        if (stringComparator == null) {
            stringComparator = new DefaultStringComparator();
        }
    }

    protected Map<Integer, FilterCriteria> adaptFilterFields(int[] filteredFields, String[] dumped, BatchPresentation batchPresentation) {
        Map<Integer, String[]> filters = new HashMap<Integer, String[]>();

        int allFieldsCount = batchPresentation.getAllFields().length;
        for (int i = 0; i < allFieldsCount; i++) {
            int index = ArraysCommons.findPosition(filteredFields, i);
            if (index > -1) {
                filters.put(new Integer(i), new String[] { dumped[index] });
            }
        }
        try {
            return FilterFormatsFactory.getParser().parse(batchPresentation, filters);
        } catch (FilterFormatException e) {
            return null;
        }
    }

    public void createSubjectedGroup() throws InternalApplicationException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        subjectedGroup = createGroup("Subjected_Group", "Group for settings children group");
    }

    public void addTestGroupsToSubjectedGroup() throws InternalApplicationException, ExecutorAlreadyInGroupException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        for (int i = 0; i < testGroups.size(); i++) {
            addExecutorToGroup(testGroups.get(i), getSubjectedGroup());
        }
    }

    public void addSubjectedActorToTestGroups() throws InternalApplicationException, ExecutorAlreadyInGroupException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        for (int i = 0; i < testGroups.size(); i++) {
            addExecutorToGroup(getSubjectedActor(), testGroups.get(i));
        }
    }

    @Override
    public void releaseResources() throws ExecutorOutOfDateException, AuthorizationException, AuthenticationException, InternalApplicationException {
        try {
            undeployProcessInstances();
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new InternalApplicationException("Process definition doesn't exists", e);
        } catch (SuperProcessInstanceExistsException e) {
            throw new InternalApplicationException("SuperProcessInstanceExistsException", e);
        }

        super.releaseResources();
    }

    public void createTestExecutors() throws InternalApplicationException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        testExecutorsNames = new String[7];
        testExecutorsDescriptions = new String[testExecutorsNames.length];
        testExecutorsNames[0] = "a";
        testExecutorsDescriptions[0] = "b";
        testExecutorsNames[1] = "adabaks";
        testExecutorsDescriptions[1] = "bdagfk";
        testExecutorsNames[2] = "6adakfhj";
        testExecutorsDescriptions[2] = "akfjhjb";
        testExecutorsNames[3] = "xxjadaha";
        testExecutorsDescriptions[3] = "xxb";
        testExecutorsNames[4] = "uadaid";
        testExecutorsDescriptions[4] = "tuiddarb";
        testExecutorsNames[5] = "990a";
        testExecutorsDescriptions[5] = "89jdgfnb";
        testExecutorsNames[6] = "food";
        testExecutorsDescriptions[6] = "llkb";

        testExecutors = Lists.newArrayList();
        for (int i = 0; i < testExecutorsNames.length; i++) {
            testExecutors.add(createActor(testExecutorsNames[i], testExecutorsDescriptions[i]));
        }
    }

    public List<TaskStub> getTasks() throws InternalApplicationException, AuthorizationException, AuthenticationException {
        return getExecutionService().getTasks(viewerSubject, getTaskBatchPresentation());
    }

    public void createTestGroups() throws InternalApplicationException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException {
        testGroupsNames = new String[7];
        testGroupsDescriptions = new String[testGroupsNames.length];
        testGroupsNames[0] = "a";
        testGroupsDescriptions[0] = "b";
        testGroupsNames[1] = "baks";
        testGroupsDescriptions[1] = "adjgfk";
        testGroupsNames[2] = "6hdsakfhj";
        testGroupsDescriptions[2] = "akfjhjahjb";
        testGroupsNames[3] = "xxa";
        testGroupsDescriptions[3] = "xxb";
        testGroupsNames[4] = "uid";
        testGroupsDescriptions[4] = "tarb";
        testGroupsNames[5] = "990a";
        testGroupsDescriptions[5] = "89jdgfnb";
        testGroupsNames[6] = "food";
        testGroupsDescriptions[6] = "llkb";

        testGroups = Lists.newArrayList();
        for (int i = 0; i < testGroupsNames.length; i++) {
            testGroups.add(createGroup(testGroupsNames[i], testGroupsDescriptions[i]));
        }
    }

    public void createViewerExecutor() throws ExecutorAlreadyExistsException, InternalApplicationException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException, WeakPasswordException {
        createActor(viewerActorName, "test_view_actor");
        List<Permission> p = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), p, ASystem.SYSTEM);
        DelegateFactory.getInstance().getExecutorService().setPassword(getAdminSubject(), getViewerActor(), "ACTOR_PWD");
        viewerSubject = DelegateFactory.getInstance().getAuthenticationService().authenticate(getViewerActor().getName(), "ACTOR_PWD");
        getAuthorizationService().setPermissions(adminSubject, getViewerActor(), Lists.newArrayList(Permission.READ), getAASystem());
    }

    public void createSubjectedExecutor() throws ExecutorAlreadyExistsException, InternalApplicationException, AuthorizationException,
            AuthenticationException {
        subjectedActor = createActor("SubjectedActor", "test_subjected_actor");
    }

    public void setREADPermissionsToViewerActorOnSubjectedActor() throws InternalApplicationException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(Permission.READ);
        if (subjectedActor != null) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, subjectedActor);
        }
    }

    public void setREADPermissionsToViewerActorOnSubjectedGroup() throws InternalApplicationException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(GroupPermission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);
        if (subjectedGroup != null) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, getSubjectedGroup());
        }
    }

    public void setREADPermissionsToViewerActorOnTestExecutors() throws InternalApplicationException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(Permission.READ);
        if (subjectedActor != null) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, subjectedActor);
        }
        getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, getAASystem());

        for (int i = 0; i < testExecutors.size(); i++) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, testExecutors.get(i));
        }
    }

    public void setREADPermissionsToViewerActorOnTestGroups() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(GroupPermission.READ, GroupPermission.LIST_GROUP);
        if (subjectedGroup != null) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, getSubjectedGroup());
        }
        getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), Lists.newArrayList(SystemPermission.READ), getAASystem());
        for (int i = 0; i < testGroups.size(); i++) {
            getAuthorizationService().setPermissions(getAdminSubject(), getViewerActor(), permissions, testGroups.get(i));
        }
    }

    public void setPermissionToTestExecutorsOnSubjectedActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(Permission.READ);
        for (int i = 0; i < testExecutors.size(); i++) {
            getAuthorizationService().setPermissions(getAdminSubject(), testExecutors.get(i), permissions, subjectedActor);
        }
    }

    public void unsetPermissionToTestExecutorsOnSubjectedActor() throws InternalApplicationException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList();
        for (int i = 0; i < testExecutors.size(); i++) {
            getAuthorizationService().setPermissions(getAdminSubject(), testExecutors.get(i), permissions, subjectedActor);
        }
    }

    public List<TaskStub> sortTasksArray(List<TaskStub> array, int sortMode, boolean isAsc) {
        List<TaskStub> clone = Lists.newArrayList(array);
        if (isAsc) {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_TASK_STATE_NAME:
                Collections.sort(clone, new TaskComparatorAscByStateName());
                break;
            case BatchPresentationTestHelper.SORT_TASK_STATE_DESCRIPTION:
                Collections.sort(clone, new TaskComparatorAscByStateDescription());
                break;
            case BatchPresentationTestHelper.SORT_TASK_DEFINITION_NAME:
                Collections.sort(clone, new TaskComparatorAscByProcessDefinitionName());
                break;
            }
        } else {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_TASK_STATE_NAME:
                Collections.sort(clone, new TaskComparatorDescByStateName());
                break;
            case BatchPresentationTestHelper.SORT_TASK_STATE_DESCRIPTION:
                Collections.sort(clone, new TaskComparatorDescByStateDescription());
                break;
            case BatchPresentationTestHelper.SORT_TASK_DEFINITION_NAME:
                Collections.sort(clone, new TaskComparatorDescByProcessDefinitionName());
                break;
            }
        }
        return clone;
    }

    public List<Executor> sortExecutorsArray(List<? extends Executor> array, int sortMode, boolean isAsc) {
        List<Executor> clone = Lists.newArrayList(array);
        if (isAsc) {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_EXECUTOR_NAME:
                Collections.sort(clone, new ExecutorComparatorAscByName());
                break;
            case BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION:
                Collections.sort(clone, new ExecutorComparatorAscByDescription());
                break;
            case BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION:
                Collections.sort(clone, new ExecutorComparatorAscByNameDescription());
                break;
            }
        } else {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_EXECUTOR_NAME:
                Collections.sort(clone, new ExecutorComparatorDescByName());
                break;
            case BatchPresentationTestHelper.SORT_EXECUTOR_DESCRIPTION:
                Collections.sort(clone, new ExecutorComparatorDescByDescription());
                break;
            case BatchPresentationTestHelper.SORT_EXECUTOR_NAME_DESCRIPTION:
                Collections.sort(clone, new ExecutorComparatorDescByNameDescription());
                break;
            }
        }
        return clone;
    }

    // public String[]
    // getTestExecutorsDescriptionsWithSubjectedAndViewerActors() throws
    // InternalApplicationException, ExecutorOutOfDateException,
    // AuthorizationException, AuthenticationException {
    // String[] executorDescriptions = new String[testExecutors.length + 2];
    // for (int i = 0; i < testExecutors.size(); i++) {
    // executorDescriptions[i] = testExecutorsNames[i];
    // }
    // executorDescriptions[testExecutors.length] =
    // subjectedActor.getDescription();
    // executorDescriptions[testExecutors.length + 1] =
    // getViewerActor().getDescription();
    // return executorDescriptions;
    // }

    // public String[] getTestExecutorsNamesWithSubjectedAndViewerActors()
    // throws InternalApplicationException, ExecutorOutOfDateException,
    // AuthorizationException, AuthenticationException {
    // String[] executorNames = new String[testExecutors.length + 2];
    // for (int i = 0; i < testExecutors.length; i++) {
    // executorNames[i] = testExecutorsNames[i];
    // }
    // executorNames[testExecutors.length] = subjectedActor.getName();
    // executorNames[testExecutors.length + 1] = getViewerActor().getName();
    // return executorNames;
    // }

    public List<Executor> getTestExecutorsWithSubjectedAndViewerActors() throws InternalApplicationException, ExecutorOutOfDateException,
            AuthorizationException, AuthenticationException {
        List<Executor> executors = Lists.newArrayList(testExecutors);
        executors.add(subjectedActor);
        executors.add(getViewerActor());
        return executors;
    }

    /**
     * @return Returns the subjectedActor.
     * @throws ExecutorOutOfDateException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Executor getSubjectedActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException {
        return getExecutorService().getActor(getAdminSubject(), subjectedActor.getId());
    }

    /**
     * @return Returns the testExecutors.
     */
    public List<Executor> getTestExecutors() {
        return testExecutors;
    }

    public List<Executor> getTestExecutorsWithNamePattern(String namePattern) {
        List<Executor> executors = new ArrayList<Executor>();
        String pattern = namePattern.replaceAll("_", ".");
        pattern = pattern.replaceAll("%", ".*");
        Pattern p = Pattern.compile("^" + pattern + "$");
        for (int i = 0; i < testExecutorsNames.length; i++) {
            Matcher m = p.matcher(testExecutorsNames[i]);
            if (m.matches()) {
                executors.add(testExecutors.get(i));
            }
        }
        return executors;
    }

    public List<ProcessDefinition> getProcessDefinitionsWithNamePattern(String namePattern) throws AuthenticationException,
            AuthorizationException {
        List<ProcessDefinition> processDefinitionsList = new ArrayList<ProcessDefinition>();
        String pattern = namePattern.replaceAll("_", ".");
        pattern = pattern.replaceAll("%", ".*");
        Pattern p = Pattern.compile("^" + pattern + "$");
        List<ProcessDefinition> definitions = getProcessDefinitions();
        for (ProcessDefinition processDefinitionDescriptor : definitions) {
            Matcher m = p.matcher(processDefinitionDescriptor.getName());
            if (m.matches()) {
                processDefinitionsList.add(processDefinitionDescriptor);
            }
        }
        return processDefinitionsList;
    }

    public ProcessInstanceStub[] getProcessInstancesWithNamePattern(String namePattern) throws InternalApplicationException, AuthorizationException,
            AuthenticationException {
        List<ProcessInstanceStub> processInstancesList = new ArrayList<ProcessInstanceStub>();
        String pattern = namePattern.replaceAll("_", ".");
        pattern = pattern.replaceAll("%", ".*");
        Pattern p = Pattern.compile("^" + pattern + "$");
        List<ProcessInstanceStub> processes = getProcessInstances();
        for (ProcessInstanceStub processInstanceStub : processes) {
            Matcher m = p.matcher(processInstanceStub.getName());
            if (m.matches()) {
                processInstancesList.add(processInstanceStub);
            }
        }
        return processInstancesList.toArray(new ProcessInstanceStub[] {});
    }

    public List<TaskStub> getTasksWithNamePattern(String namePattern) throws InternalApplicationException, AuthorizationException,
            AuthenticationException {
        List<TaskStub> taskStubs = new ArrayList<TaskStub>();
        String pattern = namePattern.replaceAll("_", ".");
        pattern = pattern.replaceAll("%", ".*");
        Pattern p = Pattern.compile("^" + pattern + "$");
        List<TaskStub> tasks = getTasks();
        for (TaskStub taskStub : tasks) {
            Matcher m = p.matcher(taskStub.getName());
            if (m.matches()) {
                taskStubs.add(taskStub);
            }
        }
        return taskStubs;
    }

    /**
     * @return Returns the testExecutorsDescriptions.
     */
    public String[] getTestExecutorsDescriptions() {
        return testExecutorsDescriptions;
    }

    /**
     * @return Returns the testExecutorsNames.
     */
    public String[] getTestExecutorsNames() {
        return testExecutorsNames;
    }

    public Actor getViewerActor() throws InternalApplicationException, ExecutorOutOfDateException, AuthorizationException, AuthenticationException {
        return (Actor) getExecutor(viewerActorName);
    }

    /**
     * @return Returns the testGroups.
     */
    public List<Group> getTestGroups() {
        return testGroups;
    }

    /**
     * @return Returns the testGroupsDescriptions.
     */
    public String[] getTestGroupsDescriptions() {
        return testGroupsDescriptions;
    }

    /**
     * @return Returns the testGroupsNames.
     */
    public String[] getTestGroupsNames() {
        return testGroupsNames;
    }

    /**
     * @return Returns the viewerSubject.
     */
    public Subject getViewerSubject() {
        return viewerSubject;
    }

    public ProcessDefinition[] getProcessDefinitionsWithVersion(int version) {
        List<ProcessDefinition> definitions = new ArrayList<ProcessDefinition>();
        for (ProcessDefinition processDefinitionDescriptor : definitions) {
            if (processDefinitionDescriptor.getVersion() == version) {
                definitions.add(processDefinitionDescriptor);
            }
        }
        return definitions.toArray(new ProcessDefinition[] {});
    }

    public void deployProcessInstances() throws ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException,
            InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException,
            UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(WorkflowSystemPermission.DEPLOY_DEFINITION);
        getAuthorizationService().setPermissions(adminSubject, getViewerActor(), permissions, ASystem.SYSTEM);
        for (int i = 0; i < processDefinitionBytes.length; i++) {
            getDefinitionService().deployProcessDefinition(viewerSubject, processDefinitionBytes[i], Lists.newArrayList("testProcess"));
        }
        processDefinitions = getDefinitionService().getLatestProcessDefinitionStubs(viewerSubject, getProcessDefinitionBatchPresentation());
        processDefinitionDescriptions = new String[processDefinitions.size()];
        processInstanceIds = new Long[processDefinitions.size()];
        processInstanceStartDates = new Date[processDefinitions.size()];
        processDefinitionVersions = new Long[processDefinitions.size()];

        for (int i = 0; i < processDefinitions.size(); i++) {
            int index = ArraysCommons.findPosition(processDefinitionNames, processDefinitions.get(i).getName());
            processDefinitionDescriptions[index] = processDefinitions.get(i).getDescription();
            processDefinitionVersions[index] = processDefinitions.get(i).getVersion();
        }
    }

    public void redeployProcessInstances() throws ProcessDefinitionDoesNotExistException, ProcessDefinitionArchiveException,
            ProcessDefinitionNameMismatchException, InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, UnapplicablePermissionException {
        List<Permission> permissions = Lists.newArrayList(ProcessDefinitionPermission.REDEPLOY_DEFINITION, ProcessDefinitionPermission.READ);
        int[] redeployments = { 1, 3 };

        for (int i = 0; i < redeployments.length; i++) {
            int index = redeployments[i];
            ProcessDefinition definition = getDefinitionService().getLatestProcessDefinitionStub(viewerSubject, processDefinitionNames[index]);
            getAuthorizationService().setPermissions(adminSubject, getViewerActor(), permissions, definition);
            getDefinitionService().redeployProcessDefinition(viewerSubject, definition.getNativeId(), processDefinitionBytes[index],
                    Lists.newArrayList("testProcess"));
        }

        processDefinitions = getProcessDefinitions();
        for (int i = 0; i < processDefinitions.size(); i++) {
            processDefinitionNames[i] = processDefinitions.get(i).getName();
            processDefinitionDescriptions[i] = processDefinitions.get(i).getDescription();
            processDefinitionVersions[i] = processDefinitions.get(i).getVersion();
        }
    }

    public void undeployProcessInstances() throws InternalApplicationException, AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SuperProcessInstanceExistsException {
        for (ProcessDefinition stub : processDefinitions) {
            getDefinitionService().undeployProcessDefinition(adminSubject, stub.getName());
        }
    }

    public void startProcessInstances() throws InternalApplicationException, ProcessDefinitionDoesNotExistException, UnapplicablePermissionException,
            ExecutorOutOfDateException, AuthorizationException, AuthenticationException, VariablesValidationException {

        for (int i = 0; i < processDefinitionNames.length; i++) {
            List<Permission> validPermissions = Lists.newArrayList(ProcessDefinitionPermission.START_PROCESS, ProcessDefinitionPermission.READ,
                    ProcessDefinitionPermission.READ_STARTED_INSTANCE);
            getAuthorizationService().setPermissions(adminSubject, getViewerActor(), validPermissions,
                    getDefinitionService().getLatestProcessDefinitionStub(adminSubject, processDefinitionNames[i]));
            getExecutionService().startProcessInstance(viewerSubject, processDefinitionNames[i]);
            uglySleep();
        }

        processInstances = getExecutionService().getProcessInstanceStubs(viewerSubject, getProcessInstanceBatchPresentation());
        for (int i = 0; i < processInstances.size(); i++) {
            processInstanceIds[i] = processInstances.get(i).getId();
            processInstanceStartDates[i] = processInstances.get(i).getStartDate();
        }
    }

    private void uglySleep() {
        try {
            // ugly hack to avoid timestamps collision on DBs that do not
            // support msec in timestamp field
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // we don't care
        }
    }

    public void endProcessInstances() throws InternalApplicationException, TaskDoesNotExistException, AuthorizationException,
            AuthenticationException, ExecutorOutOfDateException, VariablesValidationException {
        List<TaskStub> tasks = getTasks();
        for (TaskStub taskStub : tasks) {
            getExecutionService().completeTask(viewerSubject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(),
                    new HashMap<String, Object>());
            uglySleep();
        }
    }

    public List<ProcessInstanceStub> sortProcessInstanceArray(List<ProcessInstanceStub> array, int sortMode, boolean isAscending) {
        List<ProcessInstanceStub> clone = Lists.newArrayList(array);
        if (isAscending) {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_ID:
                Collections.sort(clone, new ProcessInstanceComparatorAscById());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_NAME:
                Collections.sort(clone, new ProcessInstanceComparatorAscByName());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_START_DATE:
                Collections.sort(clone, new ProcessInstanceComparatorAscByStartDate());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_END_DATE:
                Collections.sort(clone, new ProcessInstanceComparatorAscByStartDate());
                break;
            }
        } else {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_ID:
                Collections.sort(clone, new ProcessInstanceComparatorDescById());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_NAME:
                Collections.sort(clone, new ProcessInstanceComparatorDescByName());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_START_DATE:
                Collections.sort(clone, new ProcessInstanceComparatorDescByStartDate());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_INSTANCE_END_DATE:
                Collections.sort(clone, new ProcessInstanceComparatorDescByStartDate());
                break;
            }
        }
        return clone;
    }

    public List<ProcessDefinition> sortProcessDefinitionArray(List<ProcessDefinition> array, int sortMode, boolean isAscending) {
        List<ProcessDefinition> clone = Lists.newArrayList(array);
        if (isAscending) {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_NAME:
                Collections.sort(clone, new ProcessDefinitionComparatorAscByName());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_DESCRIPTION:
                Collections.sort(clone, new ProcessDefinitionComparatorAscByDescription());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_VERSION:
                Collections.sort(clone, new ProcessDefinitionComparatorAscByVersion());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_VERSION_NAME:
                Collections.sort(clone, new ProcessDefinitionComparatorAscByVersion());
                break;
            }
        } else {
            switch (sortMode) {
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_NAME:
                Collections.sort(clone, new ProcessDefinitionComparatorDescByName());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_DESCRIPTION:
                Collections.sort(clone, new ProcessDefinitionComparatorDescByDescription());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_VERSION:
                Collections.sort(clone, new ProcessDefinitionComparatorDescByVersion());
                break;
            case BatchPresentationTestHelper.SORT_PROCESS_DEFINITION_VERSION_NAME:
                Collections.sort(clone, new ProcessDefinitionComparatorAscByVersionName());
                break;
            }
        }
        return clone;
    }

    /**
     * @return Returns the subjectedGroup.
     * @throws ExecutorOutOfDateException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Group getSubjectedGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorOutOfDateException {
        return getExecutorService().getGroup(getAdminSubject(), subjectedGroup.getId());
    }

    /**
     * @return Returns the processDefinitions.
     * @throws AuthorizationException
     * @throws AuthenticationException
     */
    public List<ProcessDefinition> getProcessDefinitions() throws AuthenticationException, AuthorizationException {
        return getDefinitionService().getLatestProcessDefinitionStubs(viewerSubject, getProcessDefinitionBatchPresentation());
    }

    /**
     * @return Returns the processInstances.
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public List<ProcessInstanceStub> getProcessInstances() throws InternalApplicationException, AuthorizationException, AuthenticationException {
        return getExecutionService().getProcessInstanceStubs(viewerSubject, getProcessInstanceBatchPresentation());
    }

    /**
     * @return Returns the processDefinitionIds.
     */
    public Long[] getProcessInstanceIds() {
        return processInstanceIds;
    }

    /**
     * @return Returns the processDefinitionsEndDates.
     */
    public Date[] getProcessInstanceEndDates() {
        return processInstanceEndDates;
    }

    /**
     * @return Returns the processDefinitionsStartDates.
     */
    public Date[] getProcessInstanceStartDates() {
        return processInstanceStartDates;
    }

    /**
     * @return Returns the processDefinitionNames.
     */
    public String[] getProcessDefinitionNames() {
        return processDefinitionNames;
    }

    /**
     * @return Returns the processDefinitionDescriptions.
     */
    public String[] getProcessDefinitionDescriptions() {
        return processDefinitionDescriptions;
    }

    /**
     * @return Returns the processDefinitionVersions.
     */
    public Long[] getProcessDefinitionVersions() {
        return processDefinitionVersions;
    }

    class TaskComparatorDescByStateName extends TaskComparatorAscByStateName {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class TaskComparatorAscByStateName implements Comparator<TaskStub> {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return stringComparator.compare(o1.getName(), o2.getName());
        }
    }

    class TaskComparatorDescByStateDescription extends TaskComparatorAscByStateDescription {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class TaskComparatorAscByStateDescription implements Comparator<TaskStub> {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return stringComparator.compare(o1.getDescription(), o2.getDescription());
        }
    }

    class TaskComparatorDescByProcessDefinitionName extends TaskComparatorAscByProcessDefinitionName {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class TaskComparatorAscByProcessDefinitionName implements Comparator<TaskStub> {
        @Override
        public int compare(TaskStub o1, TaskStub o2) {
            return stringComparator.compare(o1.getProcessDefinitionName(), o2.getProcessDefinitionName());
        }
    }

    class ProcessDefinitionComparatorDescByName extends ProcessDefinitionComparatorAscByName {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return -super.compare(o1, o2);
        }
    }

    class ProcessDefinitionComparatorAscByName implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return stringComparator.compare(o1.getName(), o2.getName());
        }
    }

    class ProcessDefinitionComparatorDescByDescription extends ProcessDefinitionComparatorAscByDescription {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return -super.compare(o1, o2);
        }
    }

    class ProcessDefinitionComparatorAscByDescription implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return stringComparator.compare(o1.getDescription(), o2.getDescription());
        }
    }

    class ProcessDefinitionComparatorDescByVersion implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return o2.getVersion().intValue() - o1.getVersion().intValue();
        }
    }

    class ProcessDefinitionComparatorAscByVersion implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            return o1.getVersion().intValue() - o2.getVersion().intValue();
        }
    }

    class ProcessDefinitionComparatorAscByVersionName implements Comparator<ProcessDefinition> {
        @Override
        public int compare(ProcessDefinition o1, ProcessDefinition o2) {
            int res = o1.getVersion().intValue() - o2.getVersion().intValue();
            if (res == 0) {
                return stringComparator.compare(o1.getName(), o2.getName());
            }
            return res;
        }
    }

    class ProcessInstanceComparatorDescByName extends ProcessInstanceComparatorAscByName {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class ProcessInstanceComparatorAscByName implements Comparator<ProcessInstanceStub> {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            return stringComparator.compare(o1.getName(), o2.getName());
        }
    }

    class ProcessInstanceComparatorDescById extends ProcessInstanceComparatorAscById {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class ProcessInstanceComparatorAscById implements Comparator<ProcessInstanceStub> {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            if (o1.getId() == o2.getId()) {
                return 0;
            }
            boolean res = o1.getId() > o2.getId();
            if (res) {
                return 1;
            }
            return -1;
        }
    }

    class ProcessInstanceComparatorAscByStartDate implements Comparator<ProcessInstanceStub> {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            return o1.getStartDate().compareTo(o2.getStartDate());
        }
    }

    class ProcessInstanceComparatorDescByStartDate extends ProcessInstanceComparatorAscByStartDate {
        @Override
        public int compare(ProcessInstanceStub o1, ProcessInstanceStub o2) {
            return -super.compare(o1, o2);
        }
    }

    class ExecutorComparatorDescByName extends ExecutorComparatorAscByName {
        @Override
        public int compare(Executor o1, Executor o2) {
            return -super.compare(o1, o2);
        }
    }

    class ExecutorComparatorDescByDescription extends ExecutorComparatorAscByDescription {
        @Override
        public int compare(Executor o1, Executor o2) {
            return -super.compare(o1, o2);
        }
    }

    class ExecutorComparatorDescByNameDescription extends ExecutorComparatorAscByNameDescription {
        @Override
        public int compare(Executor o1, Executor o2) {
            return -super.compare(o1, o2);
        }
    }

    class ExecutorComparatorAscByName implements Comparator<Executor> {
        @Override
        public int compare(Executor o1, Executor o2) {
            return stringComparator.compare(o1.getName(), o2.getName());
        }
    }

    class ExecutorComparatorAscByNameDescription implements Comparator<Executor> {
        @Override
        public int compare(Executor o1, Executor o2) {
            int res = stringComparator.compare(o1.getName(), o2.getName());
            if (res == 0) {
                return stringComparator.compare(o1.getDescription(), o2.getDescription());
            }
            return res;
        }
    }

    class ExecutorComparatorAscByDescription implements Comparator<Executor> {
        @Override
        public int compare(Executor o1, Executor o2) {
            return stringComparator.compare(o1.getDescription(), o2.getDescription());
        }
    }
}
