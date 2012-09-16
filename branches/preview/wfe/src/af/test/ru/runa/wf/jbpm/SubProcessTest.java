package ru.runa.wf.jbpm;

import org.apache.cactus.ServletTestCase;

public class SubProcessTest extends ServletTestCase {

    public void testSubProcessing() throws Exception {
    }

    /*
    public static TestHelper helper = new TestHelper(new String[] { "process/superprocess.xml", "process/subprocess.xml" });

    public void testSubProcessing() throws Exception {
        // retrieve the definition
        Definition definition = executionService.getLatestDefinition("the super process");

        // start a process instance
        Map variables = new HashMap();
        variables.put("a", new Long(2));
        variables.put("b", new Long(3));

        ProcessInstance pi = executionService.startProcessInstance(definition.getId(), variables);

        // Assembler assembler = new PropertyAssembler(
        // "root.subProcessInstance.root" );
        // executionService.getProcessInstance( pi.getId(), assembler );
        executionService.getProcessInstance(pi.getId());

        TokenImpl superRoot = (TokenImpl) pi.getRoot();
        variables = executionService.getVariables(superRoot.getId());
        assertNull(superRoot.getActorId());
        assertEquals("sub process state", superRoot.getState().getName());
        assertEquals(new Long(2), variables.get("a"));
        assertEquals(new Long(3), variables.get("b"));
        assertNull(variables.get("s"));

        TokenImpl subProcessToken = (TokenImpl) pi.getRoot().getSubProcessInstance().getRoot();
        assertNotNull(subProcessToken);
        assertEquals("enter third term", subProcessToken.getState().getName());

        variables = new HashMap();
        variables.put("cc", new Long(33));
        executionService.endOfState(subProcessToken.getId(), variables);

        pi = (ProcessInstanceImpl) executionService.getProcessInstance(pi.getId());
        superRoot = (TokenImpl) pi.getRoot();
        variables = executionService.getVariables(superRoot.getId());
        assertEquals(new Long(2), variables.get("a"));
        assertEquals(new Long(3), variables.get("b"));
        assertEquals(new Long(38), variables.get("s"));
    }
    */
}
