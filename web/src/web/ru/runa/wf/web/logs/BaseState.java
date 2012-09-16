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
package ru.runa.wf.web.logs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.Element;
import org.apache.ecs.html.A;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.bpm.context.log.VariableUpdateLog;
import ru.runa.bpm.graph.def.Action;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.graph.def.Transition;
import ru.runa.bpm.graph.log.MessageNodeLog;
import ru.runa.bpm.graph.log.ProcessInstanceCreateLog;
import ru.runa.bpm.graph.log.ProcessInstanceEndLog;
import ru.runa.bpm.graph.log.ProcessStateLog;
import ru.runa.bpm.graph.node.Decision;
import ru.runa.bpm.logging.log.ProcessLog;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.bpm.taskmgmt.log.TaskAssignLog;
import ru.runa.bpm.taskmgmt.log.TaskEndLog;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.IdentityType;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.commons.InfoHolder;
import ru.runa.commons.JBPMLazyLoaderHelper;
import ru.runa.wf.FileVariable;
import ru.runa.wf.web.html.WorkflowFormProcessingException;
import ru.runa.wf.web.html.vartag.FileVariableValueDownloadVarTag;

class BaseState {
    private static Log log = LogFactory.getLog(BaseState.class);
    private static Map<Class<? extends ProcessLog>, Class<? extends BaseState>> stateTransitions = new HashMap<Class<? extends ProcessLog>, Class<? extends BaseState>>();

    protected final PageContext pageContext;
    protected final Subject subject;

    private final Map<String, String> assignedSwimlane = new HashMap<String, String>();
    private final Map<Long, Set<Long>> dynamicGroups = new HashMap<Long, Set<Long>>();

    private final BaseState parent;

    static {
        stateTransitions.put(ProcessInstanceCreateLog.class, ProcessStartState.class);
        stateTransitions.put(ProcessInstanceEndLog.class, ProcessCompleteState.class);
        stateTransitions.put(TaskEndLog.class, TaskEndState.class);
        stateTransitions.put(ProcessStateLog.class, SubprocessStartState.class);
        stateTransitions.put(TaskAssignLog.class, AssignmentState.class);
        stateTransitions.put(MessageNodeLog.class, MessageNodeState.class);
    }

    private BaseState(PageContext pageContext, Subject subject) {
        parent = null;
        this.pageContext = pageContext;
        this.subject = subject;
        initPlacehlders(pageContext);
    }

    protected BaseState(BaseState parent) {
        this.parent = parent;
        pageContext = parent.pageContext;
        subject = parent.subject;
    }

    private transient String PH_SWIMLINE_NAME = null;
    private transient String PH_SWIMLINE_VALUE = null;
    private transient String PH_EXECUTOR_NAME = null;
    private transient String PH_SUBSTITUTOR_NAME = null;
    private transient String PH_NODE_NAME = null;
    private transient String PH_NODE_TYPE = null;
    private transient String PH_ACTION = null;
    private transient String PH_ACTION_TYPE = null;
    private transient String PH_ACTION_CONF = null;
    private transient String PH_TRANSITION_NAME = null;
    private transient String PH_TRANSITION_FROM = null;
    private transient String PH_TRANSITION_TO = null;
    private transient String PH_VARIABLE_NAME = null;
    private transient String PH_VARIABLE_OLD = null;
    private transient String PH_VARIABLE_NEW = null;

    public static List<Element> acceptLog(Subject subject, PageContext pageContext, LogIterator logs) {
        List<Element> result = new ArrayList<Element>();
        BaseState state = new BaseState(pageContext, subject);
        while (logs.hasNext()) {
            List<Element> dispatchResult = state.dispatchLog(stateTransitions, logs.next(), logs);
            if (dispatchResult != null) {
                result.addAll(dispatchResult);
            }
        }
        return result;
    }

    protected List<Element> acceptLog(ProcessLog currentLog, LogIterator logs) {
        return null;
    }

    protected final List<Element> dispatchLog(Map<Class<? extends ProcessLog>, Class<? extends BaseState>> stateTransitions, ProcessLog currentLog,
            LogIterator logs) {
        try {
            if (stateTransitions.containsKey(currentLog.getClass())) {
                BaseState clazz = stateTransitions.get(currentLog.getClass()).getDeclaredConstructor(BaseState.class).newInstance(this);
                return clazz.acceptLog(currentLog, logs);
            }
        } catch (Exception e) {
            log.warn("", e);
        }
        return null;
    }

    private synchronized void initPlacehlders(PageContext pageContext) {
        if (PH_SWIMLINE_NAME != null) {
            return;
        }
        PH_SWIMLINE_NAME = Messages.getMessage(Messages.HISTORY_PH_SWIMLANE_NAME, pageContext);
        PH_SWIMLINE_VALUE = Messages.getMessage(Messages.HISTORY_PH_SWIMLANE_VALUE, pageContext);
        PH_EXECUTOR_NAME = Messages.getMessage(Messages.HISTORY_PH_EXECUTOR, pageContext);
        PH_SUBSTITUTOR_NAME = Messages.getMessage(Messages.HISTORY_PH_SUBSTITUTOR, pageContext);
        PH_NODE_NAME = Messages.getMessage(Messages.HISTORY_PH_NODE_NAME, pageContext);
        PH_NODE_TYPE = Messages.getMessage(Messages.HISTORY_PH_NODE_TYPE, pageContext);
        PH_ACTION = Messages.getMessage(Messages.HISTORY_PH_ACTION, pageContext);
        PH_ACTION_TYPE = Messages.getMessage(Messages.HISTORY_PH_ACTION_TYPE, pageContext);
        PH_ACTION_CONF = Messages.getMessage(Messages.HISTORY_PH_ACTION_CONF, pageContext);
        PH_TRANSITION_NAME = Messages.getMessage(Messages.HISTORY_PH_TRANSITION_NAME, pageContext);
        PH_TRANSITION_FROM = Messages.getMessage(Messages.HISTORY_PH_TRANSITION_FROM, pageContext);
        PH_TRANSITION_TO = Messages.getMessage(Messages.HISTORY_PH_TRANSITION_TO, pageContext);
        PH_VARIABLE_NAME = Messages.getMessage(Messages.HISTORY_PH_VARIABLE_NAME, pageContext);
        PH_VARIABLE_OLD = Messages.getMessage(Messages.HISTORY_PH_VARIABLE_OLD, pageContext);
        PH_VARIABLE_NEW = Messages.getMessage(Messages.HISTORY_PH_VARIABLE_NEW, pageContext);
    }

    protected String getNodeType(Node node) {
        if (node.getNodeType().equals(Node.NodeType.Decision)) {
            return Messages.getMessage(Messages.HISTORY_NODE_DECISION, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.Fork)) {
            return Messages.getMessage(Messages.HISTORY_NODE_FORK, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.Join)) {
            return Messages.getMessage(Messages.HISTORY_NODE_JOIN, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.StartState)) {
            return Messages.getMessage(Messages.HISTORY_NODE_PROCESS, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.EndState)) {
            return Messages.getMessage(Messages.HISTORY_NODE_END, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.State)) {
            return Messages.getMessage(Messages.HISTORY_NODE_STATE, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.Task)) {
            return Messages.getMessage(Messages.HISTORY_NODE_TASK, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.SubProcess)) {
            return Messages.getMessage(Messages.HISTORY_NODE_PROCESS, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.MultiInstance)) {
            return Messages.getMessage(Messages.HISTORY_NODE_PROCESS, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.SendMessage)) {
            return Messages.getMessage(Messages.HISTORY_NODE_SEND_MESSAGE, pageContext);
        } else if (node.getNodeType().equals(Node.NodeType.ReceiveMessage)) {
            return Messages.getMessage(Messages.HISTORY_NODE_RECEIVE_MESSAGE, pageContext);
        }

        return JBPMLazyLoaderHelper.getClass(node).getName();
    }

    protected final String replacePlaceholders(String key, TaskInstance taskInstance, String executorId, Action action, Transition transition,
            String substitutorId) {
        if (parent != null) {
            return parent.replacePlaceholders(key, taskInstance, executorId, action, transition, substitutorId);
        }
        String message = Messages.getMessage(key, pageContext);
        message = replacePlaceholders(message, taskInstance);
        message = replacePlaceholders(message, action);
        message = replacePlaceholders(message, transition);
        message = replaceExecutor(message, executorId, substitutorId);
        return message;
    }

    private String replaceConditional(String source, String condition, boolean isCondTrue) {
        if (!source.contains("if({" + condition + "})")) {
            return source;
        }
        if (!isCondTrue) {
            StringBuilder retVal = new StringBuilder();
            int ifIdx = -1;
            int endIdx = -1;
            while ((ifIdx = source.indexOf("if({" + condition + "})")) != -1) {
                endIdx = source.indexOf("endif({" + condition + "})") + ("endif({" + condition + "})").length();
                retVal.append(source.substring(0, ifIdx));
                source = source.substring(endIdx);
            }
            retVal.append(source);
            return retVal.toString();
        } else {
            return source.replaceAll("(end)?if\\(\\{" + condition + "\\}\\)", "");
        }
    }

    // Replace {state, state_type, swimlane} placeholders
    private String replacePlaceholders(String source, TaskInstance taskInstance) {
        source = replaceConditional(source, PH_SWIMLINE_NAME, taskInstance != null && taskInstance.getSwimlaneInstance() != null);
        source = replaceConditional(source, PH_NODE_NAME, taskInstance != null);
        source = replaceConditional(source, PH_NODE_TYPE, taskInstance != null);
        if (taskInstance == null) {
            return source;
        }
        if (taskInstance.getSwimlaneInstance() != null) {
            source = source.replaceAll("\\{" + PH_SWIMLINE_NAME + "\\}", Matcher.quoteReplacement(taskInstance.getSwimlaneInstance().getName()))
                    .replaceAll(
                            "\\{" + PH_SWIMLINE_VALUE + "\\}",
                            Matcher.quoteReplacement(assignedSwimlane.get(taskInstance.getSwimlaneInstance().getName()) == null ? ("\\{"
                                    + getPH_EXECUTOR_NAME() + "\\}") : getExecutorLink(subject, assignedSwimlane.get(taskInstance
                                    .getSwimlaneInstance().getName()))));
        }
        return source.replaceAll("\\{" + PH_NODE_NAME + "\\}", Matcher.quoteReplacement(taskInstance.getTask().getNode().getName())).replaceAll(
                "\\{" + PH_NODE_TYPE + "\\}", Matcher.quoteReplacement(getNodeType(taskInstance.getTask().getNode())));
    }

    private String replacePlaceholders(String source, Action action) {
        source = replaceConditional(source, PH_ACTION, action != null);
        source = replaceConditional(source, PH_ACTION_TYPE, action != null);
        source = replaceConditional(source, PH_ACTION_CONF, action != null);
        if (action == null) {
            return source;
        }
        source = source.replaceAll("\\{" + PH_ACTION + "\\}", Matcher.quoteReplacement(action.getDelegation() == null ? JBPMLazyLoaderHelper
                .getClass(action).getName() : action.getDelegation().getClassName()));
        String eventType = "-";
        if (action.getEvent() != null) {
            eventType = action.getEvent().getEventType();
        }
        source = source.replaceAll("\\{" + PH_ACTION_TYPE + "\\}", Matcher.quoteReplacement(eventType));
        source = source.replaceAll("\\{" + PH_ACTION_CONF + "\\}", Matcher.quoteReplacement(Matcher
                .quoteReplacement(action.getDelegation() == null ? "" : action.getDelegation().getConfiguration()
                        .replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;"))));
        return source;
    }

    protected final String replaceExecutor(String source, String executorId, String substitutorId) {
        if (parent != null) {
            return parent.replaceExecutor(source, executorId, substitutorId);
        }
        source = replaceConditional(source, getPH_EXECUTOR_NAME(), executorId != null);
        source = replaceConditional(source, PH_SUBSTITUTOR_NAME, substitutorId != null);
        return source.replaceAll("\\{" + getPH_EXECUTOR_NAME() + "\\}", Matcher.quoteReplacement(getExecutorLink(subject, executorId))).replaceAll(
                "\\{" + PH_SUBSTITUTOR_NAME + "\\}", Matcher.quoteReplacement(getExecutorLink(subject, substitutorId)));
    }

    private String replacePlaceholders(String source, Transition transition) {
        source = replaceConditional(source, "from_decision", transition != null && transition.getFrom() instanceof Decision);
        source = replaceConditional(source, PH_TRANSITION_NAME, transition != null);
        source = replaceConditional(source, PH_TRANSITION_FROM, transition != null);
        source = replaceConditional(source, PH_TRANSITION_TO, transition != null);
        source = replaceConditional(source, PH_NODE_NAME, transition != null);
        source = replaceConditional(source, PH_NODE_TYPE, transition != null);
        if (transition == null) {
            return source;
        }
        source = source.replaceAll("\\{" + PH_NODE_NAME + "\\}", Matcher.quoteReplacement(transition.getTo().getName())).replaceAll(
                "\\{" + PH_NODE_TYPE + "\\}", Matcher.quoteReplacement(getNodeType(transition.getTo())));
        return source.replaceAll("\\{" + PH_TRANSITION_NAME + "\\}", Matcher.quoteReplacement(transition.getName())).replaceAll(
                "\\{" + PH_TRANSITION_FROM + "\\}", Matcher.quoteReplacement(transition.getFrom().getName())).replaceAll(
                "\\{" + PH_TRANSITION_TO + "\\}", Matcher.quoteReplacement(transition.getTo().getName()));
    }

    protected final String replacePlaceholders(String key, VariableUpdateLog varLog) {
        if (parent != null) {
            return parent.replacePlaceholders(key, varLog);
        }
        String source = Messages.getMessage(key, pageContext);
        source = replaceConditional(source, PH_VARIABLE_NAME, varLog != null);
        source = replaceConditional(source, PH_VARIABLE_OLD, varLog != null);
        source = replaceConditional(source, PH_VARIABLE_NEW, varLog != null);
        if (varLog == null) {
            return source;
        }
        String oldValue = "";
        String newValue = "";
        if (varLog.getNewValue() instanceof FileVariable) {
            FileVariable fileVariableNew = (FileVariable) varLog.getNewValue();
            FileVariable fileVariableOld = (FileVariable) varLog.getOldValue();
            FileVariableValueDownloadVarTag vartag = new FileVariableValueDownloadVarTag(IdentityType.PROCESS_INSTANCE);
            Subject subject = SubjectHttpSessionHelper.getActorSubject(pageContext.getSession());
            try {
                // dirty hack!!!!!!!!!! getParameterMap() must return
                // immutable map, but it does not
                Object originalValue = pageContext.getRequest().getParameterMap().get(IdForm.ID_INPUT_NAME);
                pageContext.getRequest().getParameterMap().put(IdForm.ID_INPUT_NAME, String.valueOf(varLog.getToken().getProcessInstance().getId()));
                newValue = vartag.getHtml(subject, varLog.getVariableInstance().getName(), fileVariableNew, pageContext);
                oldValue = vartag.getHtml(subject, varLog.getVariableInstance().getName(), fileVariableOld, pageContext);
                pageContext.getRequest().getParameterMap().put(IdForm.ID_INPUT_NAME, originalValue);
            } catch (WorkflowFormProcessingException e) {
            } catch (AuthenticationException e) {
            }
        } else {
            oldValue = varLog.getOldValue() == null ? "" : varLog.getOldValue().toString();
            newValue = varLog.getNewValue() == null ? "" : varLog.getNewValue().toString();
        }
        return source.replaceAll("\\{" + PH_VARIABLE_NAME + "\\}", Matcher.quoteReplacement(varLog.getVariableInstance().getName())).replaceAll(
                "\\{" + PH_VARIABLE_OLD + "\\}", Matcher.quoteReplacement(oldValue)).replaceAll("\\{" + PH_VARIABLE_NEW + "\\}",
                Matcher.quoteReplacement(newValue));
    }

    protected String getExecutorLink(Subject subject, Long executorId) {
        if (parent != null) {
            return parent.getExecutorLink(subject, executorId);
        }
        Executor ex = null;
        try {
            ex = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService().getExecutor(subject, executorId);
        } catch (Exception e) {
            ex = null;
        }

        if (ex != null) {
            return new A(Commons.getActionUrl(ru.runa.af.web.Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, ex.getId(), pageContext,
                    PortletUrl.Render), ExecutorNameConverter.getName(ex, pageContext)).setClass(Resources.CLASS_LINK).toString();
        } else {
            if (dynamicGroups.containsKey(executorId)) {
                return Messages.getMessage(Messages.DYNAMIC_GROUP_NAME, pageContext) + executorId;
            }
            return "Unknown executor with id " + executorId;
        }
    }

    protected String getExecutorLink(Subject subject, String executorId) {
        if (parent != null) {
            return parent.getExecutorLink(subject, executorId);
        }
        Executor ex = getExecutor(subject, executorId);

        if (ex != null) {
            return new A(Commons.getActionUrl(ru.runa.af.web.Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, ex.getId(), pageContext,
                    PortletUrl.Render), ExecutorNameConverter.getName(ex, pageContext)).setClass(Resources.CLASS_LINK).toString();
        } else {
            if (executorId != null && executorId.charAt(0) == 'G' && dynamicGroups.containsKey(Long.parseLong(executorId.substring(1)))) {
                return Messages.getMessage(Messages.DYNAMIC_GROUP_NAME, pageContext) + executorId.substring(1);
            }
            return "Unknown executor with id " + executorId;
        }
    }

    private Executor getExecutor(Subject subject, String executorId) {
        try {
            ExecutorService executorService = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService();
            if (executorId == null || executorId.equals(InfoHolder.UNASSIGNED_SWIMLANE_VALUE)) {
                return null;
            }
            if (executorId.charAt(0) == 'G') {
                return executorService.getGroup(subject, Long.parseLong(executorId.substring(1)));
            } else {
                return executorService.getActorByCode(subject, Long.parseLong(executorId));
            }
        } catch (AuthorizationException e) {
            return null;
        } catch (AuthenticationException e) {
            return null;
        } catch (ExecutorOutOfDateException e) {
            return null;
        }
    }

    protected final Map<String, String> getAssignedSwimlanes() {
        if (parent != null) {
            return parent.getAssignedSwimlanes();
        }
        return assignedSwimlane;
    }

    protected void addDynamicGroup(Long groupId, Set<Long> executorIds) {
        if (parent != null) {
            parent.addDynamicGroup(groupId, executorIds);
        }
        dynamicGroups.put(groupId, executorIds);
    }

    protected String getPH_EXECUTOR_NAME() {
        if (parent != null) {
            return parent.getPH_EXECUTOR_NAME();
        }
        return PH_EXECUTOR_NAME;
    }
}
