/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
import ru.runa.af.presentation.SubstringDBSource;
import ru.runa.bpm.context.exe.VariableInstance;
import ru.runa.bpm.context.exe.variableinstance.DateInstance;
import ru.runa.bpm.context.exe.variableinstance.DoubleInstance;
import ru.runa.bpm.context.exe.variableinstance.LongInstance;
import ru.runa.bpm.context.exe.variableinstance.StringInstance;
import ru.runa.bpm.job.Job;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.commons.ApplicationContextFactory;

/**
 * Created on 22.10.2005
 */
public class TaskClassPresentation extends ClassPresentation {

    private static class VariableDBSource extends DefaultDBSource {
        public VariableDBSource(Class sourceObject) {
            this(sourceObject, "value");
        }

        public VariableDBSource(Class sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".processInstance=" + alias + ".processInstance";
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
            Dialect dialect = ApplicationContextFactory.getDialect();
            String typeName = dialect.getCastTypeName(Hibernate.STRING.sqlType());
            return alias == null ? valueDBPath : "CAST(" + alias + "." + valueDBPath + " AS " + typeName + ")";
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".processInstance=" + alias + ".processInstance";
        }
    }

    private static class DeadlineDBSource extends DefaultDBSource {
        public DeadlineDBSource(Class sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".token=" + alias + ".token and " + alias + ".lockOwner is null";
        }
    }

    public static final String TASK_BATCH_PRESENTATION_NAME = "batch_presentation.task.name";
    public static final String TASK_BATCH_PRESENTATION_DESCRIPTION = "batch_presentation.task.description";
    public static final String TASK_BATCH_PRESENTATION_DEFINITION_NAME = "batch_presentation.task.definition_name";
    public static final String TASK_BATCH_PRESENTATION_PROCESS_INSTANCE_ID = "batch_presentation.task.process_instance_id";
    public static final String TASK_OWNER = "batch_presentation.task.owner";
    public static final String TASK_SWIMLINE = "batch_presentation.task.swimlane";
    public static final String TASK_VARIABLE = ClassPresentation.editable_prefix + "name:batch_presentation.task.variable";
    public static final String TASK_DEADLINE = "batch_presentation.task.deadline";

    private static final DBSource[] variableClasses;

    static {
        variableClasses = new DBSource[] { new VariableDBSource(VariableInstance.class, null), new VariableDBSource(DateInstance.class),
            new VariableDBSource(DoubleInstance.class), new VariableDBSource(LongInstance.class), new StringVariableDBSource(StringInstance.class) };
    }

    private static final ClassPresentation INSTANCE = new TaskClassPresentation();

    private TaskClassPresentation() {
        super(TaskInstance.class, ClassPresentation.classNameSQL + ".endDate is null and " + classNameSQL + ".processInstance.endDate is null", false,
                new FieldDescriptor[] {
                        //                           display name                               field type                                        DB source                                                      isSort             filter mode                  get value/show in web                           getter parameters                          
                    new FieldDescriptor(TASK_BATCH_PRESENTATION_NAME, String.class.getName(), new DefaultDBSource(TaskInstance.class, "name"),
                                true, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder",
                                new Object[] { new Permission(), "name" }),
                        new FieldDescriptor(TASK_BATCH_PRESENTATION_DESCRIPTION, String.class.getName(), new SubstringDBSource(TaskInstance.class,
                                "token.node.description"), true, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                                new Permission(), "description" }),
                        new FieldDescriptor(TASK_BATCH_PRESENTATION_DEFINITION_NAME, String.class.getName(), new DefaultDBSource(TaskInstance.class,
                                "processInstance.processDefinition.name"), true, FieldFilterMode.DATABASE,
                                "ru.runa.wf.web.html.TaskProcessDefinitionTDBuilder", new Object[] {}),
                        new FieldDescriptor(TASK_BATCH_PRESENTATION_PROCESS_INSTANCE_ID, Integer.class.getName(), new DefaultDBSource(
                                TaskInstance.class, "processInstance.id"), true, FieldFilterMode.DATABASE,
                                "ru.runa.wf.web.html.TaskProcessInstanceIdTDBuilder", new Object[] {}),
                        new FieldDescriptor(TASK_OWNER, String.class.getName(), new DefaultDBSource(TaskInstance.class, "assignedActorId"), true,
                                FieldFilterMode.NONE, "ru.runa.wf.web.html.TaskOwnerTDBuilder", new Object[] {}),
                        new FieldDescriptor(TASK_SWIMLINE, String.class.getName(), new DefaultDBSource(TaskInstance.class, "swimlaneInstance.name"),
                                true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TaskRoleTDBuilder", new Object[] {}),
                        new FieldDescriptor(TASK_VARIABLE, VariableInstance.class.getName(), variableClasses, true, FieldFilterMode.DATABASE,
                                "ru.runa.wf.web.html.TaskVariableTDBuilder", new Object[] {}, true/*THIS FIELD IS WEAK*/),
                        new FieldDescriptor(TASK_DEADLINE, Date.class.getName(),
                                new DeadlineDBSource[] { new DeadlineDBSource(Job.class, "dueDate") }, true, FieldFilterMode.DATABASE,
                                "ru.runa.wf.web.html.TaskDeadlineTDBuilder", new Object[] {}, true/*THIS FIELD IS WEAK*/) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
