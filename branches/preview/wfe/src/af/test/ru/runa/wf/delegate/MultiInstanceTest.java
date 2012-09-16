package ru.runa.wf.delegate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.Actor;
import ru.runa.af.Group;
import ru.runa.af.Relation;
import ru.runa.af.authenticaion.SubjectPrincipalsHelper;
import ru.runa.delegate.DelegateFactory;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.TaskStub;
import ru.runa.wf.presentation.WFProfileStrategy;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;

public class MultiInstanceTest extends ServletTestCase {
    private WfServiceTestHelper th;

    private Group group1 = null;
    private Relation relation1 = null;
    private Actor actor1 = null;
    private Actor actor2 = null;
    private Actor actor3 = null;
    private Actor actor4 = null;
    private ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();

    public static Test suite() {
        return new TestSuite(MultiInstanceTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(MultiInstanceTest.class.getName());
        group1 = th.createGroupIfNotExist("group1", MultiInstanceTest.class.getName());
        relation1 = th.createRelation("relation1", MultiInstanceTest.class.getName());
        actor1 = th.createActorIfNotExist("actor1", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor1, group1);
        actor2 = th.createActorIfNotExist("actor2", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor2, group1);
        actor3 = th.createActorIfNotExist("actor3", MultiInstanceTest.class.getName());
        th.addExecutorToGroup(actor3, group1);
        actor4 = th.createActorIfNotExist("relationparam1", MultiInstanceTest.class.getName());
        th.addRelationPair(relation1.getName(), actor1, actor4);
        th.addRelationPair(relation1.getName(), actor2, actor4);
        th.addRelationPair(relation1.getName(), actor3, actor4);
        th.deployValidProcessDefinition("multiinstance superprocess.par");
        th.deployValidProcessDefinition("multiinstance subprocess.par");
        th.deployValidProcessDefinition("MultiInstance - MainProcess.par");
        th.deployValidProcessDefinition("MultiInstance - SubProcess.par");
        th.deployValidProcessDefinition("MultiInstance - TypeMainProcess.par");
    }

    @Override
    protected void tearDown() throws Exception {
        if (relation1 != null) {
            th.removeRelation(relation1.getId());
        }
        th.undeployValidProcessDefinition("MultiInstance - MainProcess");
        th.undeployValidProcessDefinition("MultiInstance - TypeMainProcess");
        th.undeployValidProcessDefinition("MultiInstance - SubProcess");
        th.undeployValidProcessDefinition("multiinstance superprocess");
        th.undeployValidProcessDefinition("multiinstance subprocess");
        th.releaseResources();
        super.tearDown();
    }

    public void testSimple() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] { "d1", "d2", "d3" });
        variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
        variables.put("discriminator_rw", new String[] { "d1_rw", "d2_rw", "d3_rw" });
        long processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "d");
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(subject, task.getId(), "d_r"));
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(subject, task.getId(), "d_rw"));
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
        }
        ArrayAssert.assertEqualArrays("discriminator", 
                (Object[]) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator").get(0), new String[]{ "d1", "d2", "d3" });
        ArrayAssert.assertEqualArrays("discriminator_r", 
                (Object[]) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_r").get(0), new String[]{ "d1_r", "d2_r", "d3_r" });
        ArrayAssert.assertEqualArrays("discriminator_w", 
                (Object[]) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_w").get(0), new String[]{ "d1", "d2", "d3" });
        ArrayAssert.assertEqualArrays("discriminator_rw", 
                (Object[]) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_rw").get(0), new String[]{ "d1", "d2", "d3" });
    }

    public void testSimpleWithLists() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", Lists.newArrayList("d1", "d2", "d3"));
        variables.put("discriminator_r", Lists.newArrayList("d1_r", "d2_r", "d3_r"));
        variables.put("discriminator_rw", Lists.newArrayList("d1_rw", "d2_rw", "d3_rw"));
        long processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "d");
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(subject, task.getId(), "d_r"));
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(subject, task.getId(), "d_rw"));
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
        }
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_r").get(0), Lists.newArrayList(
                "d1_r", "d2_r", "d3_r" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_w").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_rw").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
    }

    public void testNullDiscriminator() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] { "d1", "d2", "d3" });
        variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
        long processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "d");
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(subject, task.getId(), "d_r"));
            assertEquals(null, (String) executionService.getVariable(subject, task.getId(), "d_rw"));
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
        }
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator").get(0), Lists.newArrayList(
                "d1", "d2", "d3"));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_r").get(0), Lists.newArrayList(
                "d1_r", "d2_r", "d3_r" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_w").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_rw").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
    }

    public void testNullDiscriminator2() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        long processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        assertTrue(executionService.getProcessInstanceStub(subject, processId).isEnded());

        variables.put("discriminator", new String[] {});
        variables.put("discriminator_r", new String[] {});
        processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        assertTrue(executionService.getProcessInstanceStub(subject, processId).isEnded());
    }

    public void testManySubprocessInToken() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("Variable1", "Variable for subprocess 1");
        variables.put("Variable2", "Variable for subprocess 2");
        variables.put("multi", new String[] { "sub-mult 1", "sub-mult 2", "sub-mult 3" });
        long processId = executionService.startProcessInstance(subject, "MultiInstance - MainProcess", variables);

        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 1", (String) executionService.getVariable(subject, tasks.get(0).getId(), "Variable1"));
        executionService.completeTask(subject, tasks.get(0).getId(), tasks.get(0).getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                new HashMap<String, Object>());

        tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<TaskStub>() {
            @Override
            public int compare(TaskStub o1, TaskStub o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        int idx = 1;
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "Variable1");
            assertEquals("sub-mult " + idx, descriminatorValue);
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
            ++idx;
        }

        tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<TaskStub>() {
            @Override
            public int compare(TaskStub o1, TaskStub o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        idx = 1;
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "Variable1");
            assertEquals("sub-mult " + idx, descriminatorValue);
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
            ++idx;
        }

        tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(1, tasks.size());
        assertEquals("Variable for subprocess 2", (String) executionService.getVariable(subject, tasks.get(0).getId(), "Variable1"));
        executionService.completeTask(subject, tasks.get(0).getId(), tasks.get(0).getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                new HashMap<String, Object>());

        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "multiOut").get(0), Lists.newArrayList(
                "sub-mult 1", "sub-mult 2", "sub-mult 3" ));
    }

    public void testDifferentTypes() throws Exception {
        //internalDifferentTypes(new Date());
        internalDifferentTypes(new Long(1));
        internalDifferentTypes(new Double(1));
    }

    public void testAllTypes() throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("Variable1", "group1");
        variables.put("Variable2", "relation1");
        variables.put("Variable3", "relationparam1");
        variables.put("multi", new String[] { "sub-mult 1", "sub-mult 2", "sub-mult 3" });
        executionService.startProcessInstance(subject, "MultiInstance - TypeMainProcess", variables);

        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        Collections.sort(tasks, new Comparator<TaskStub>() {
            @Override
            public int compare(TaskStub o1, TaskStub o2) {
                return o1.getId() < o2.getId() ? -1 : o1.getId() == o2.getId() ? 0 : 1;
            }
        });
        int idx = 1;
        for (TaskStub task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(subject, task.getId(), "Variable1");
            assertEquals("sub-mult " + idx, discriminatorValue);
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
            ++idx;
        }

        ArrayList<String> actorList = new ArrayList<String>();
        actorList.add(String.valueOf(actor1.getCode()));
        actorList.add(String.valueOf(actor2.getCode()));
        actorList.add(String.valueOf(actor3.getCode()));
        tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        idx = 1;
        for (TaskStub task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(subject, task.getId(), "Variable1");
            assertTrue(actorList.contains(discriminatorValue));
            actorList.remove(actorList.indexOf(discriminatorValue));
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
            ++idx;
        }
        assertTrue(actorList.isEmpty());

        actorList.add(String.valueOf(actor1.getCode()));
        actorList.add(String.valueOf(actor2.getCode()));
        actorList.add(String.valueOf(actor3.getCode()));
        tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY.getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        idx = 1;
        for (TaskStub task : tasks) {
            String discriminatorValue = (String) executionService.getVariable(subject, task.getId(), "Variable1");
            assertTrue(actorList.contains(discriminatorValue));
            actorList.remove(actorList.indexOf(discriminatorValue));
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(),
                    new HashMap<String, Object>());
            ++idx;
        }
        assertTrue(actorList.isEmpty());
    }

    private void internalDifferentTypes(Object varValue) throws Exception {
        Subject subject = th.getAdminSubject();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("discriminator", new String[] { "d1", "d2", "d3" });
        variables.put("discriminator_r", new String[] { "d1_r", "d2_r", "d3_r" });
        variables.put("discriminator_rw", new String[] { "d1_rw", "d2_rw", "d3_rw" });
        long processId = executionService.startProcessInstance(subject, "multiinstance superprocess", variables);
        List<TaskStub> tasks = executionService.getTasks(subject, WFProfileStrategy.TASK_DEFAULT_BATCH_PRESENTATION_FACTORY
                .getDefaultBatchPresentation());
        assertEquals(3, tasks.size());
        for (TaskStub task : tasks) {
            String descriminatorValue = (String) executionService.getVariable(subject, task.getId(), "d");
            assertEquals(descriminatorValue + "_r", (String) executionService.getVariable(subject, task.getId(), "d_r"));
            assertEquals(descriminatorValue + "_rw", (String) executionService.getVariable(subject, task.getId(), "d_rw"));
            Map<String, Object> var = new HashMap<String, Object>();
            var.put("d_rw", varValue);
            var.put("d_w", varValue);
            executionService.completeTask(subject, task.getId(), task.getName(), SubjectPrincipalsHelper.getActor(subject).getId(), var);
        }
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator").get(0), Lists.newArrayList(
                "d1", "d2", "d3" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_r").get(0), Lists.newArrayList(
                "d1_r", "d2_r", "d3_r" ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_w").get(0), Lists.newArrayList(
                varValue, varValue, varValue ));
        ArrayAssert.assertEqualArrays((List<Object>) executionService.getVariable(subject, Lists.newArrayList(processId), "discriminator_rw").get(0), Lists.newArrayList(
                varValue, varValue, varValue ));
    }
}
