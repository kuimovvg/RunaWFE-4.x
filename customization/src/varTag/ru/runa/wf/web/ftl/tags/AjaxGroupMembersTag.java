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
package ru.runa.wf.web.ftl.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Charsets;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.ExecutorService;
import ru.runa.commons.IOCommons;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.ftl.AjaxFreemarkerTag;
import freemarker.template.TemplateModelException;

public class AjaxGroupMembersTag extends AjaxFreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() throws TemplateModelException {
        try {
            String groupVarName = getParameterAs(String.class, 0);
            String userVarName = getParameterAs(String.class, 1);
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("groupSelectorId", groupVarName);
            substitutions.put("userSelectorId", userVarName);
            StringBuffer html = new StringBuffer();
            html.append(exportScript("AjaxGroupMembersTag.js", substitutions));
            html.append("<div style=\"border: solid 1px green; background-color: #ffeeff; padding: 5px;\">");
            html.append("Choose user from group&nbsp;&nbsp;&nbsp;");
            html.append("<select id='").append(groupVarName).append("' name='").append(groupVarName).append("'>");
            List<Group> groups = getGroups();
            Long defaultGroupId = getSavedValue(Long.class, groupVarName);
            if (defaultGroupId == null && groups.size() > 0) {
                defaultGroupId = groups.get(0).getId();
            }
            if (groups.size() == 0) {
                html.append("<option value=''>No groups</option>");
            }
            for (Group group : groups) {
                html.append("<option value='").append(group.getId()).append("'");
                if (defaultGroupId == group.getId()) {
                    html.append(" selected");
                }
                html.append(">").append(group.getName()).append("</option>");
            }
            html.append("</select>");
            html.append("<select id='").append(userVarName).append("' name='").append(userVarName).append("'>");
            if (defaultGroupId != null) {
                List<Actor> actors = getActors(subject, defaultGroupId);
                Long defaultActorCode = getSavedValue(Long.class, groupVarName);
                if (defaultActorCode == null && actors.size() > 0) {
                    defaultActorCode = actors.get(0).getCode();
                }
                if (actors.size() == 0) {
                    html.append("<option value=''>No users in this group</option>");
                } else {
                    html.append("<option value=''>None</option>");
                }
                for (Actor actor : actors) {
                    html.append("<option value='").append(actor.getCode()).append("'");
                    if (defaultActorCode == actor.getCode()) {
                        html.append(" selected");
                    }
                    html.append(">").append(actor.getFullName()).append("</option>");
                }
            } else {
                html.append("<option value=''></option>");
            }
            html.append("</select><br/>");
            html.append("<div id='forErrors'></div>");
            return html.toString();
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

    @Override
    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        StringBuffer json = new StringBuffer("[");
        Long groupId = new Long(request.getParameter("groupId"));
        List<Actor> actors = getActors(subject, groupId);
        if (actors.size() == 0) {
            json.append("{code: '', name: 'No users in this group'}");
        } else {
            json.append("{code: '', name: 'None'}");
        }
        for (Actor actor : actors) {
            if (json.length() > 10) {
                json.append(", ");
            }
            json.append("{code:").append(actor.getCode()).append(", name: '").append(actor.getFullName()).append("'}");
        }
        json.append("]");
        response.getOutputStream().write(json.toString().getBytes(Charsets.UTF_8));
    }

    private List<Actor> getActors(Subject subject, Long groupId) throws TemplateModelException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            Group group = executorService.getGroup(subject, groupId);
            return executorService.getGroupActors(subject, group);
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

    private List<Group> getGroups() throws TemplateModelException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
            batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
            // TODO add delegate.getAllGroups
            List<Executor> executors = executorService.getAll(subject, batchPresentation);
            List<Group> groupList = new ArrayList<Group>();
            for (Executor executor : executors) {
                if (executor instanceof Group) {
                    groupList.add((Group) executor);
                }
            }
            return groupList;
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }
}
