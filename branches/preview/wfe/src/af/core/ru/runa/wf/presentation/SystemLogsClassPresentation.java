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

import ru.runa.af.Executor;
import ru.runa.af.Permission;
import ru.runa.af.log.SystemLog;
import ru.runa.af.presentation.ClassPresentation;
import ru.runa.af.presentation.DBSource;
import ru.runa.af.presentation.DefaultDBSource;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.FieldFilterMode;
import ru.runa.af.presentation.filter.AnywhereStringFilterCriteria;

/**
 * Presentation class to access {@link SystemLog} objects.
 */
public class SystemLogsClassPresentation extends ClassPresentation {

    /**
     * Data source to sort and filter by executor name. 
     */
    static class ActorDBSource extends DefaultDBSource {
        public ActorDBSource() {
            super(Executor.class, "name");
        }

        @Override
        public String getJoinExpression(String alias) {
            return ClassPresentation.classNameSQL + ".actorCode=" + alias + ".code";
        }
    }

    /**
     * Singleton.
     */
    private static final ClassPresentation INSTANCE = new SystemLogsClassPresentation();

    /**
     * System log identity.
     */
    private static final String SYSTEM_LOG_ID = "batch_presentation.system_log.id";

    /**
     * System log actor.
     */
    private static final String SYSTEM_LOG_ACTOR = "batch_presentation.system_log.actor";

    /**
     * System log time.
     */
    private static final String SYSTEM_LOG_TIME = "batch_presentation.system_log.time";

    /**
     * System log type. 
     */
    private static final String SYSTEM_LOG_TYPE = "batch_presentation.system_log.type";

    /**
     * System log message. 
     */
    private static final String SYSTEM_LOG_MESSAGE = "batch_presentation.system_log.message";

    /**
     * Creates instance of presentation class to access {@link SystemLog} objects.
     */
    private SystemLogsClassPresentation() {
        super(
                SystemLog.class,
                "",
                true,
                new FieldDescriptor[] {
                        new FieldDescriptor(SYSTEM_LOG_ID, Integer.class.getName(), new DefaultDBSource(SystemLog.class, "id"), true,
                                FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "id", true }),
                        new FieldDescriptor(SYSTEM_LOG_TIME, Date.class.getName(), new DefaultDBSource(SystemLog.class, "actionTime"), true,
                                FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "actionTime",
                                        true }),
                        new FieldDescriptor(SYSTEM_LOG_ACTOR, AnywhereStringFilterCriteria.class.getName(), new DBSource[] { new ActorDBSource() },
                                true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.SystemLogActorTDBuilder", new Object[] {}, true),
                        new FieldDescriptor(SYSTEM_LOG_TYPE, SystemLogTypeFilterCriteria.class.getName(), new DefaultDBSource(SystemLog.class,
                                "class"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.SystemLogTypeTDBuilder", new Object[] {}),
                        new FieldDescriptor(SYSTEM_LOG_MESSAGE, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(SystemLog.class,
                                "id"), false, FieldFilterMode.NONE, "ru.runa.wf.web.html.SystemLogTDBuilder", new Object[] {}) });
    }

    /**
     * Returns instance of {@link SystemLogsClassPresentation}.
     * @return Instance of {@link SystemLogsClassPresentation}.
     */
    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
