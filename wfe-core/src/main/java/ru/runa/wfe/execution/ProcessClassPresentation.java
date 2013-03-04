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
package ru.runa.wfe.execution;

import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.dialect.Dialect;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DBSource;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.impl.DateVariable;
import ru.runa.wfe.var.impl.DoubleVariable;
import ru.runa.wfe.var.impl.LongVariable;
import ru.runa.wfe.var.impl.StringVariable;

/**
 * Created on 22.10.2005
 * 
 */
public class ProcessClassPresentation extends ClassPresentation {
    public static final String BATCH_PRESENTATION_ID = "batch_presentation.process.id";
    public static final String BATCH_PRESENTATION_DEFINITION_NAME = "batch_presentation.process.definition_name";
    public static final String BATCH_PRESENTATION_STARTED = "batch_presentation.process.started";
    public static final String BATCH_PRESENTATION_ENDED = "batch_presentation.process.ended";
    public static final String BATCH_PRESENTATION_DEFINITION_VERSION = "batch_presentation.process.definition_version";
    public static final String TASK_VARIABLE = editable_prefix + "name:batch_presentation.process.variable";
    private static final DBSource[] variableClasses;

    static {
        variableClasses = new DBSource[] { new VariableDBSource(Variable.class, null), new VariableDBSource(DateVariable.class),
                new VariableDBSource(DoubleVariable.class), new VariableDBSource(LongVariable.class),
                new StringVariableDBSource(StringVariable.class) };
    }

    private static final ClassPresentation INSTANCE = new ProcessClassPresentation();

    private static class VariableDBSource extends DefaultDBSource {
        public VariableDBSource(Class<?> sourceObject) {
            this(sourceObject, "storableValue");
        }

        public VariableDBSource(Class<?> sourceObject, String valueDBPath) {
            super(sourceObject, valueDBPath);
        }

        @Override
        public String getJoinExpression(String alias) {
            return classNameSQL + ".id=" + alias + ".process";
        }
    }

    private static class StringVariableDBSource extends DefaultDBSource {
        public StringVariableDBSource(Class<?> sourceObject) {
            this(sourceObject, "storableValue");
        }

        public StringVariableDBSource(Class<?> sourceObject, String valueDBPath) {
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
            return classNameSQL + ".id=" + alias + ".process";
        }
    }

    private ProcessClassPresentation() {
        super(Process.class, "", true, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode get
                // value/show in web getter param
                new FieldDescriptor(BATCH_PRESENTATION_ID, Integer.class.getName(), new DefaultDBSource(Process.class, "id"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "id" }),
                new FieldDescriptor(BATCH_PRESENTATION_DEFINITION_NAME, String.class.getName(),
                        new DefaultDBSource(Process.class, "deployment.name"), true, FieldFilterMode.DATABASE,
                        "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "name" }),
                new FieldDescriptor(BATCH_PRESENTATION_STARTED, Date.class.getName(), new DefaultDBSource(Process.class, "startDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessStartDateTDBuilder", new Object[] {}),
                new FieldDescriptor(BATCH_PRESENTATION_ENDED, Date.class.getName(), new DefaultDBSource(Process.class, "endDate"), true,
                        FieldFilterMode.DATABASE, "ru.runa.wf.web.html.ProcessEndDateTDBuilder", new Object[] {}),
                new FieldDescriptor(BATCH_PRESENTATION_DEFINITION_VERSION, Integer.class.getName(), new DefaultDBSource(Process.class,
                        "deployment.version"), true, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] {
                        new Permission(), "version" }),
                new FieldDescriptor(TASK_VARIABLE, Variable.class.getName(), variableClasses, true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.ProcessVariableTDBuilder", new Object[] {}, true),
                new FieldDescriptor(filterable_prefix + "batch_presentation.process.id", AnywhereStringFilterCriteria.class.getName(),
                        new DefaultDBSource(Process.class, "hierarchySubProcess"), true, FieldFilterMode.DATABASE,
                        "ru.runa.wf.web.html.RootProcessTDBuilder", new Object[] {}, true) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
