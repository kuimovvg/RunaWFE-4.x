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
package ru.runa.wf.web.ftl.method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

import com.google.common.base.Objects;

import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public class AjaxGroupMembersTag extends AjaxJsonFreemarkerTag {
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
            html.append(exportScript("scripts/AjaxGroupMembersTag.js", substitutions, true));
            html.append("<span class=\"ajaxGroupMembers\" id=\"ajaxGroupMembers_").append(groupVarName).append("\">");
            html.append("<select id=\"").append(groupVarName).append("\" name=\"").append(groupVarName).append("\">");
            List<Group> groups = (List<Group>) Delegates.getExecutorService().getExecutors(user, BatchPresentationFactory.GROUPS.createNonPaged());
            Group defaultGroup = getSavedValue(Group.class, groupVarName);
            if (defaultGroup == null && groups.size() > 0) {
                defaultGroup = groups.get(0);
            }
            if (groups.size() == 0) {
                html.append("<option value=\"\">No groups</option>");
            }
            for (Group group : groups) {
                html.append("<option value=\"ID").append(group.getId()).append("\"");
                if (Objects.equal(defaultGroup, group)) {
                    html.append(" selected");
                }
                html.append(">").append(group.getName()).append("</option>");
            }
            html.append("</select>");
            html.append("<select id=\"").append(userVarName).append("\" name=\"").append(userVarName).append("\">");
            if (defaultGroup != null) {
                List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, defaultGroup);
                Actor defaultActor = getSavedValue(Actor.class, userVarName);
                if (defaultActor == null && actors.size() > 0) {
                    defaultActor = actors.get(0);
                }
                if (actors.size() == 0) {
                    html.append("<option value=\"\">No users in this group</option>");
                } else {
                    html.append("<option value=\"\">None</option>");
                }
                for (Actor actor : actors) {
                    html.append("<option value=\"ID").append(actor.getId()).append("\"");
                    if (Objects.equal(defaultActor, actor)) {
                        html.append(" selected");
                    }
                    html.append(">").append(actor.getFullName()).append("</option>");
                }
            } else {
                html.append("<option value=\"\"></option>");
            }
            html.append("</select>");
            html.append("</span>");
            return html.toString();
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        JSONArray json = new JSONArray();
        Group group = TypeConversionUtil.convertTo(Group.class, request.getParameter("groupId"));
        List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, group);
        if (actors.size() == 0) {
            json.add(createJsonObject(null, "No users in this group"));
        } else {
            json.add(createJsonObject(null, "None"));
        }
        for (Actor actor : actors) {
            json.add(createJsonObject(actor.getId(), actor.getFullName()));
        }
        return json;
    }

    private JSONObject createJsonObject(Long id, String name) {
        JSONObject object = new JSONObject();
        object.put("id", id != null ? "ID" + id : "");
        object.put("name", name);
        return object;
    }

}
