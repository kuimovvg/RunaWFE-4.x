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
package ru.runa.wf.dbpatch;

import ru.runa.commons.dbpatch.DBPatch;

public class RestoreJobsFromLog implements DBPatch {

    public void apply(boolean isArchiveDBinit) {
        throw new UnsupportedOperationException();
//        Session session = isArchiveDBinit ? HibernateSessionFactory.getArchiveSession() : HibernateSessionFactory.getSession();
//        Query query = session
//                .createQuery("SELECT p FROM ru.runa.bpm.graph.log.ActionLog as p WHERE p.token.processInstance.end is null ORDER BY p.date");
//        List<ActionLog> logs = query.list();
//        Map<Token, ActionLog> actions = new HashMap<Token, ActionLog>();
//        for (ActionLog aLog : logs) {
//            if (!CreateTimerAction.class.isAssignableFrom(JBPMLazyLoaderHelper.getClass((aLog).getAction()))) {
//                continue;
//            }
//            if (aLog.getToken().getEnd() != null || aLog.getToken().getProcessInstance().getEnd() != null) {
//                continue;
//            }
//            if (actions.get(aLog.getToken()) == null) {
//                actions.put(aLog.getToken(), aLog);
//            }
//        }
//
//        for (Map.Entry<Token, ActionLog> entry : actions.entrySet()) {
//            Criteria criteriaTask = session.createCriteria(TaskInstance.class);
//            criteriaTask.add(Expression.eq("token", entry.getKey()));
//            criteriaTask.add(Expression.isNull("end"));
//            criteriaTask.add(Expression.eq("isCancelled", false));
//            criteriaTask.add(Expression.eq("isOpen", true));
//            TaskInstance task = (TaskInstance) (criteriaTask.uniqueResult());
//            if (task != null) {
//                Timer timer = new Timer(entry.getValue().getToken());
//                timer.setDueDate(((CreateTimerAction) JBPMLazyLoaderHelper.getImplementation(entry.getValue().getAction())).getDueDateDate(
//                        new ExecutionContext(entry.getValue().getToken()), entry.getValue().getDate()));
//                timer.setProcessInstance(entry.getValue().getToken().getProcessInstance());
//                timer.setSuspended(true);
//                timer.setRetries(0);
//                timer.setRepeat("0");
//                timer.setLockOwner(CreateTimerAction.keepAliveOwner);
//                timer.setName(task.getName());
//                Criteria criteriaJob = session.createCriteria(Timer.class);
//                criteriaJob.add(Expression.eq("processInstance", task.getProcessInstance()));
//                criteriaJob.add(Expression.eq("name", task.getName()));
//                criteriaJob.add(Expression.eq("lockOwner", CreateTimerAction.keepAliveOwner));
//                if (criteriaJob.uniqueResult() == null) {
//                    session.save(timer);
//                }
//            }
//        }
    }
}
