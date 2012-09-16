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
package ru.runa.wf.presentation;

import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.dialect.Dialect;

import ru.runa.af.Permission;
import ru.runa.af.presentation.ClassPresentation;
import ru.runa.af.presentation.DBSource;
import ru.runa.af.presentation.DefaultDBSource;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.FieldFilterMode;
import ru.runa.af.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.exe.variableinstance.DateInstance;
import ru.runa.bpm.context.exe.variableinstance.DoubleInstance;
import ru.runa.bpm.context.exe.variableinstance.LongInstance;
import ru.runa.bpm.context.exe.variableinstance.StringInstance;
import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.commons.ApplicationContextFactory;

/**
 * Created on 22.10.2005
 * 
 */
public class ProcessInstanceClassPresentation extends ClassPresentation {

    private static class VariableDBSource extends DefaultDBSource {
        public VariableDBSource(Class sourceObject) {
            this(sourceObject, "value");
        }

        public VariableDBSource(Class sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".id=" + alias + ".processInstance";
        }
    }

    private static class StringVariableDBSource extends DefaultDBSource {
        public StringVariableDBSource(Class sourceObject) {
            this(sourceObject, "value");
        }

        public StringVariableDBSource(Class sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getValueDBPath(String alias) {
            Dialect dialect = ApplicationContextFactory.getConfiguration().buildSettings().getDialect();
            String typeName = dialect.getCastTypeName(Hibernate.STRING.sqlType());
            return alias == null ? valueDBPath : "CAST(" + alias + "." + valueDBPath + " AS " + typeName + ")";
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".id=" + alias + ".processInstance";
        }
    }

    private static final long serialVersionUID = 892321673035423607L;

    public static final String PROCESS_INSTANCE_BATCH_PRESENTATION_ID = "batch_presentation.process_instance.id";

    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_NAME = "batch_presentation.process_instance.definition_name";

    public static final String PROCESS_INSTANCE_BATCH_PRESENTATION_STARTED = "batch_presentation.process_instance.started";

    public static final String PROCESS_INSTANCE_BATCH_PRESENTATION_ENDED = "batch_presentation.process_instance.ended";

    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION = "batch_presentation.process_instance.definition_version";

    public static final String TASK_VARIABLE = ClassPresentation.editable_prefix + "name:batch_presentation.process_instance.variable";

    private static final DBSource[] variableClasses;

    static {
        variableClasses = new DBSource[] { new VariableDBSource(VariableInstance.class, null), new VariableDBSource(DateInstance.class),
            new VariableDBSource(DoubleInstance.class), new VariableDBSource(LongInstance.class), new StringVariableDBSource(StringInstance.class) };
    }

    private static final ClassPresentation INSTANCE = new ProcessInstanceClassPresentation();

    private ProcessInstanceClassPresentation() {
        super(ProcessInstance.class, "", true, new FieldDescriptor[] {
                //                         display name                                   field type                                     DB source                                  isSort         filter mode                              get value/show in web                                    getter param                          
            new FieldDescriptor(PROCESS_INSTANCE_BATCH_PRESENTATION_ID, Integer.class.getName(),
                        new DefaultDBSource(ProcessInstance.class, "id"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "id" }),
                new FieldDescriptor(PROCESS_DEFINITION_BATCH_PRESENTATION_NAME, String.class.getName(), new DefaultDBSource(ProcessInstance.class,
                        "processDefinition.name"), true, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                        new Permission(), "name" }),
                new FieldDescriptor(PROCESS_INSTANCE_BATCH_PRESENTATION_STARTED, Date.class.getName(), new DefaultDBSource(ProcessInstance.class,
                        "start"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessInstanceStartDateTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_INSTANCE_BATCH_PRESENTATION_ENDED, Date.class.getName(),
                        new DefaultDBSource(ProcessInstance.class, "end"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessInstanceEndDateTDBuilder", new Object[] {}),
                new FieldDescriptor(PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION, Integer.class.getName(), new DefaultDBSource(
                        ProcessInstance.class, "processDefinition.version"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "version" }),
                new FieldDescriptor(ClassPresentation.filterable_prefix + "batch_presentation.process_instance.id",
                        AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(ProcessInstance.class, "hierarchySubProcess"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.RootProcessInstanceTDBuilder", new Object[] {}, true),
                new FieldDescriptor(TASK_VARIABLE, VariableInstance.class.getName(), variableClasses, true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessInstanceVariableTDBuilder", new Object[] {}, true/*THIS FIELD IS WEAK*/) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
