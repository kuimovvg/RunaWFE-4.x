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
package ru.runa.wf.web.html;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.af.web.Resources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.ActorStub;
import ru.runa.wf.SwimlaneStub;
import ru.runa.wf.web.form.SwimlaneForm;

import com.google.common.base.Strings;

public class ProcessInstanceSwimlaneRowBuilder implements RowBuilder {
    private final PageContext pageContext;

    private final List<SwimlaneStub> swimlaneStubs;

    private int currentIndex = 0;

    private final Long processInstanceId;

    private final Map<String, String> orgFunctionMappings;

    public ProcessInstanceSwimlaneRowBuilder(Long processInstanceId, List<SwimlaneStub> swimlaneStubs, Map<String, String> map, PageContext pageContext) {
        this.processInstanceId = processInstanceId;
        this.swimlaneStubs = swimlaneStubs;
        this.pageContext = pageContext;
        orgFunctionMappings = map;
    }

    public boolean hasNext() {
        return currentIndex < swimlaneStubs.size();
    }

    public TR buildNext() {
        TR tr = new TR();
        SwimlaneStub swimlaneStub = swimlaneStubs.get(currentIndex++);

        TD nameTD = new TD(swimlaneStub.getName());
        tr.addElement(nameTD);
        nameTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD assignedToActorTD = new TD();
        tr.addElement(assignedToActorTD);
        assignedToActorTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        if (swimlaneStub.isAssigned()) {
            if (ActorStub.UNAUTHORIZED_ACTOR_STUB == swimlaneStub.getExecutor()) {
                assignedToActorTD.addElement(Messages.getMessage(ru.runa.wf.web.Resources.UNAUTHORIZED_EXECUTOR_NAME, pageContext));
            } else if (ActorStub.NOT_EXISTING_ACTOR_STUB == swimlaneStub.getExecutor()) {
                assignedToActorTD.addElement(Messages.getMessage(ru.runa.wf.web.Resources.NON_EXISTING_EXECUTOR_NAME, pageContext));
            } else {
                String url = Commons.getActionUrl(Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, String.valueOf(swimlaneStub
                        .getExecutor().getId()), pageContext, PortletUrl.Render);
                assignedToActorTD.addElement(new A(url, ExecutorNameConverter.getName(swimlaneStub.getExecutor(), pageContext)));
            }
        }

        TD organizationFunctionTD = new TD();
        tr.addElement(organizationFunctionTD);
        organizationFunctionTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        Map<String, String> params = new HashMap<String, String>();
        params.put(IdForm.ID_INPUT_NAME, String.valueOf(processInstanceId));
        params.put(SwimlaneForm.SWIMLANE_ID_INPUT_NAME, String.valueOf(swimlaneStub.getId()));
        String url = Commons.getActionUrl(Resources.ACTION_MAPPING_DISPLAY_SWIMLANE, params, pageContext, PortletUrl.Action);
        String orgFunction = getOrgFunction(swimlaneStub);
        organizationFunctionTD.addElement(new A(url, orgFunction));

        return tr;
    }

    private String getOrgFunction(SwimlaneStub swimlaneStub) {
        String orgFunction = swimlaneStub.getOrgFunction();
        if (Strings.isNullOrEmpty(orgFunction)) {
            return Messages.getMessage("label.unset_empty.value", pageContext);
        }
        String[] orgFunctionParts = orgFunction.split("\\(");
        if (orgFunctionParts.length == 2) {
            String mapping = orgFunctionMappings.get(orgFunctionParts[0].trim());
            if (mapping != null) {
                orgFunction = mapping + " (" + orgFunctionParts[1];
            }
        }
        return orgFunction;
    }

    public int getEnabledRowsCount() {
        return swimlaneStubs.size();
    }
}
