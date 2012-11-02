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

import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.form.SwimlaneForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ProcessSwimlaneRowBuilder implements RowBuilder {
    private final PageContext pageContext;

    private final List<WfSwimlane> wfSwimlanes;

    private int currentIndex = 0;

    private final Long processId;

    public ProcessSwimlaneRowBuilder(Long processId, List<WfSwimlane> wfSwimlanes, PageContext pageContext) {
        this.processId = processId;
        this.wfSwimlanes = wfSwimlanes;
        this.pageContext = pageContext;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < wfSwimlanes.size();
    }

    @Override
    public TR buildNext() {
        TR tr = new TR();
        WfSwimlane wfSwimlane = wfSwimlanes.get(currentIndex++);

        TD nameTD = new TD(wfSwimlane.getDefinition().getName());
        tr.addElement(nameTD);
        nameTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD assignedToActorTD = new TD();
        tr.addElement(assignedToActorTD);
        assignedToActorTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        if (wfSwimlane.getExecutor() == null) {
        } else if (Actor.UNAUTHORIZED_ACTOR.getName().equals(wfSwimlane.getExecutor().getName())) {
            assignedToActorTD.addElement(Messages.getMessage(ru.runa.common.WebResources.UNAUTHORIZED_EXECUTOR_NAME, pageContext));
        } else {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME,
                    wfSwimlane.getExecutor().getId(), pageContext, PortletUrlType.Render);
            assignedToActorTD.addElement(new A(url, ExecutorNameConverter.getName(wfSwimlane.getExecutor(), pageContext)));
        }

        TD organizationFunctionTD = new TD();
        tr.addElement(organizationFunctionTD);
        organizationFunctionTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, processId);
        params.put(SwimlaneForm.SWIMLANE_NAME_INPUT_NAME, wfSwimlane.getDefinition().getName());
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_DISPLAY_SWIMLANE, params, pageContext, PortletUrlType.Action);
        String swimlaneInitializer = wfSwimlane.getDefinition().getDisplayOrgFunction();
        if (Strings.isNullOrEmpty(swimlaneInitializer)) {
            swimlaneInitializer = Messages.getMessage("label.unset_empty.value", pageContext);
        }
        organizationFunctionTD.addElement(new A(url, swimlaneInitializer));
        return tr;
    }

    public int getEnabledRowsCount() {
        return wfSwimlanes.size();
    }
}
