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

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.audit.dao.AuditDAO;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.execution.dao.ExecutionDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationHibernateCompiler;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.SystemPermission;

import com.google.common.base.Preconditions;

/**
 * Created on 14.03.2005
 * 
 */
public class AuditLogic extends CommonLogic {
    @Autowired
    private AuditDAO auditDAO;
    @Autowired
    private ExecutionDAO executionDAO;

    public void login(Subject subject, ASystem system) throws AuthorizationException, AuthenticationException {
        checkLoginAllowed(subject, system);
    }

    protected void checkLoginAllowed(Subject subject, ASystem system) throws AuthorizationException, AuthenticationException {
        checkPermissionAllowed(subject, system, SystemPermission.LOGIN_TO_SYSTEM);
    }

    public void logout(Subject subject, ASystem system) {
    }

    public ProcessLogs getProcessLogs(Subject subject, ProcessLogFilter filter) {
        // TODO checkPermissionAllowed(subject, identifiable, permission);
        Preconditions.checkNotNull(filter.getProcessId(), "filter.processId");
        ProcessLogs result = new ProcessLogs(filter.getProcessId());
        result.addLogs(auditDAO.getProcessLogs(filter.getProcessId()));
        if (filter.isIncludeSubprocessLogs()) {
            ru.runa.wfe.execution.Process process = executionDAO.getProcessNotNull(filter.getProcessId());
            for (ru.runa.wfe.execution.Process subprocess : executionDAO.getSubprocessesRecursive(process)) {
                result.addLogs(auditDAO.getProcessLogs(subprocess.getId()));
            }
        }
        return result;
    }

    /**
     * Load system logs according to {@link BatchPresentation}.
     * 
     * @param subject
     *            Requester subject.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs.
     * @return Loaded system logs.
     */
    public List<SystemLog> getSystemLogs(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException {
        checkPermissionAllowed(subject, ASystem.INSTANCE, SystemPermission.READ);
        return new BatchPresentationHibernateCompiler(batchPresentation).getBatch(true);
    }

    /**
     * Load system logs count according to {@link BatchPresentation}.
     * 
     * @param subject
     *            Requester subject.
     * @param batchPresentation
     *            {@link BatchPresentation} to load logs count.
     * @return System logs count.
     */
    public int getSystemLogsCount(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException {
        checkPermissionAllowed(subject, ASystem.INSTANCE, SystemPermission.READ);
        return new BatchPresentationHibernateCompiler(batchPresentation).getCount();
    }

}
