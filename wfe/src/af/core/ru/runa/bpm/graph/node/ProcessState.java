/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.graph.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.FlyweightAttribute;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

import ru.runa.bpm.context.def.VariableMapping;
import ru.runa.bpm.context.exe.ContextInstance;
import ru.runa.bpm.graph.def.Event;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.bpm.jpdl.xml.JpdlXmlReader;
import ru.runa.bpm.jpdl.xml.Parsable;
import ru.runa.commons.ApplicationContextFactory;

import com.google.common.collect.Lists;

public class ProcessState extends Node implements Parsable {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(ProcessState.class);

    @Override
    public NodeType getNodeType() {
        return NodeType.SubProcess;
    }

    protected ExecutableProcessDefinition subProcessDefinition;
    protected List<VariableMapping> variableMappings = Lists.newArrayList();
    protected String subProcessName;

    // event types
    // //////////////////////////////////////////////////////////////

    public static final String[] supportedEventTypes = new String[] { Event.EVENTTYPE_SUBPROCESS_CREATED, Event.EVENTTYPE_SUBPROCESS_END,
            Event.EVENTTYPE_NODE_ENTER, Event.EVENTTYPE_NODE_LEAVE, Event.EVENTTYPE_BEFORE_SIGNAL, Event.EVENTTYPE_AFTER_SIGNAL };

    @Override
    public String[] getSupportedEventTypes() {
        return supportedEventTypes;
    }

    // xml
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void read(ExecutableProcessDefinition processDefinition, Element processStateElement, JpdlXmlReader jpdlReader) {
        Element subProcessElement = processStateElement.element("sub-process");

        if (subProcessElement != null) {

            String binding = subProcessElement.attributeValue("binding");
            if ("late".equalsIgnoreCase(binding)) {
                subProcessName = subProcessElement.attributeValue("name");
            } else {

                SubProcessResolver subProcessResolver = ApplicationContextFactory.getSubProcessResolver();
                subProcessDefinition = subProcessResolver.findSubProcess(subProcessElement);

                // in case this is a self-recursive process invocation...
                if (subProcessDefinition == null) {
                    String subProcessName = subProcessElement.attributeValue("name");
                    if (subProcessName.equals(processDefinition.getName())) {
                        subProcessDefinition = processDefinition;
                    } else {
                        this.subProcessName = subProcessName; // Make it
                                                              // 'late'...
                    }
                }
            }
        }

        if (subProcessDefinition != null) {
            log.debug("subprocess for process-state '" + name + "' bound to " + subProcessDefinition);
        } else if (subProcessName != null) {
            log.debug("subprocess for process-state '" + name + "' will be late bound to " + subProcessName);
        } else {
            log.debug("subprocess for process-state '" + name + "' not yet bound");
        }

        this.variableMappings.addAll(jpdlReader.readVariableMappings(processStateElement));
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        Token superProcessToken = executionContext.getToken();

        // if this process has late binding
        if ((subProcessDefinition == null) && (subProcessName != null)) {
            SubProcessResolver subProcessResolver = ApplicationContextFactory.getSubProcessResolver();
            List<FlyweightAttribute> attributes = new ArrayList<FlyweightAttribute>();
            attributes.add(new FlyweightAttribute("name", subProcessName));
            Element subProcessElement = new DefaultElement("sub-process");
            subProcessElement.setAttributes(attributes);
            subProcessDefinition = subProcessResolver.findSubProcess(subProcessElement);
        }
        if (subProcessDefinition == null) {
            if (subProcessName == null) {
                subProcessName = "";
            }
            throw new RuntimeException("Unable to start subprocess '" + subProcessName + "' because it does not exist");
        }
        // create the subprocess
        ProcessInstance subProcessInstance = superProcessToken.createSubProcessInstance(subProcessDefinition);

        // fire the subprocess created event
        fireEvent(Event.EVENTTYPE_SUBPROCESS_CREATED, executionContext);

        ContextInstance superContextInstance = executionContext.getContextInstance();
        ContextInstance subContextInstance = subProcessInstance.getContextInstance();
        for (VariableMapping variableMapping : variableMappings) {
            // if this variable mapping is readable
            if (variableMapping.isReadable()) {
                // the variable is copied from the super process variable
                // name
                // to the sub process mapped name
                String variableName = variableMapping.getName();
                Object value = superContextInstance.getVariable(variableName, superProcessToken);
                String mappedName = variableMapping.getMappedName();
                log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value);
                if (value != null) {
                    subContextInstance.setVariable(executionContext, mappedName, value);
                }
            }
        }

        ApplicationContextFactory.getCurrentSession().save(new StartedSubprocesses(superProcessToken.getProcessInstance(), subProcessInstance, this));
        // send the signal to start the subprocess
        subProcessInstance.signal(executionContext);
    }

    @Override
    public void leave(ExecutionContext executionContext, Transition transition) {
        performLeave(executionContext, transition);
        // call the subProcessEndAction
        super.leave(executionContext, getDefaultLeavingTransition());
    }

    // We replaced the normal log generation of super.leave() by creating the
    // log above in the leave method
    // and overriding the addNodeLog method with an empty version
    @Override
    protected void addNodeLog(Token token) {
    }

    protected void performLeave(ExecutionContext executionContext, Transition transition) {
        ProcessInstance subProcessInstance = executionContext.getSubProcessInstance();

        Token superProcessToken = subProcessInstance.getSuperProcessToken();

        ContextInstance superContextInstance = executionContext.getContextInstance();
        ContextInstance subContextInstance = subProcessInstance.getContextInstance();

        for (VariableMapping variableMapping : variableMappings) {
            // if this variable access is writable
            if (variableMapping.isWritable()) {
                // the variable is copied from the sub process mapped name
                // to the super process variable name
                String mappedName = variableMapping.getMappedName();
                Object value = subContextInstance.getVariable(mappedName);
                String variableName = variableMapping.getName();
                log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                if (value != null) {
                    superContextInstance.setVariable(executionContext, variableName, value, superProcessToken);
                }
            }
        }

        // fire the subprocess ended event
        fireEvent(Event.EVENTTYPE_SUBPROCESS_END, executionContext);

        // remove the subprocess reference
        superProcessToken.setSubProcessInstance(null);

        {
            Criteria criteria = ApplicationContextFactory.getCurrentSession().createCriteria(StartedSubprocesses.class);
            criteria.add(Expression.eq("subProcessInstance", subProcessInstance));
            criteria.add(Expression.eq("processInstance", superProcessToken.getProcessInstance()));
            criteria.add(Expression.eq("node", this));
            if (criteria.list().isEmpty()) {
                ApplicationContextFactory.getCurrentSession().save(
                        new StartedSubprocesses(superProcessToken.getProcessInstance(), subProcessInstance, this));
            }
        }
        // We replaced the normal log generation of super.leave() by creating
        // the log here
        // and overriding the addNodeLog method with an empty version
        superProcessToken.addLog(new ProcessStateLog(this, superProcessToken.getNodeEnterDate(), new Date(), subProcessInstance));
    }

    public String getSubProcessName() {
        if (subProcessName != null) {
            return subProcessName;
        }
        if (subProcessDefinition != null) {
            return subProcessDefinition.getName();
        }
        return null;
    }

}
