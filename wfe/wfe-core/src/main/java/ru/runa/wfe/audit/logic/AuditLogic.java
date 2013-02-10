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
package ru.runa.wfe.audit.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Audit logic.
 * 
 * @author dofs
 * @since 4.0
 */
public class AuditLogic extends CommonLogic {
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private ProcessLogDAO processLogDAO;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;

    public void login(User user, ASystem system) {
        checkLoginAllowed(user, system);
    }

    protected void checkLoginAllowed(User user, ASystem system) {
        checkPermissionAllowed(user, system, SystemPermission.LOGIN_TO_SYSTEM);
    }

    public void logout(User user, ASystem system) {
    }

    public ProcessLogs getProcessLogs(User user, ProcessLogFilter filter) {
        // TODO checkPermissionAllowed(user, identifiable, permission);
        Preconditions.checkNotNull(filter.getProcessId(), "filter.processId");
        ProcessLogs result = new ProcessLogs(filter.getProcessId());
        result.addLogs(processLogDAO.getAll(filter.getProcessId()));
        if (filter.isIncludeSubprocessLogs()) {
            ru.runa.wfe.execution.Process process = processDAO.getNotNull(filter.getProcessId());
            for (ru.runa.wfe.execution.Process subprocess : nodeProcessDAO.getSubprocessesRecursive(process)) {
                result.addLogs(processLogDAO.getAll(subprocess.getId()));
            }
        }
        return result;
    }

    /**
     * Load system logs according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs.
     * @return Loaded system logs.
     */
    public List<SystemLog> getSystemLogs(User user, BatchPresentation batchPresentation) {
        checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getBatch(true);
    }

    /**
     * Load system logs count according to {@link BatchPresentation}.
     * 
     * @param user
     *            Requester user.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs count.
     * @return System logs count.
     */
    public int getSystemLogsCount(User user, BatchPresentation batchPresentation) {
        checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.READ);
        BatchPresentationHibernateCompiler compiler = PresentationCompilerHelper.createAllSystemLogsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

}
