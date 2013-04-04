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

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.task.dto.WfTask;

public class ProcessSwimlaneAssignmentRowBuilder implements RowBuilder {
    private final Iterator<WfTask> iterator;
    private final PageContext pageContext;

    public ProcessSwimlaneAssignmentRowBuilder(List<WfTask> activeTasks, PageContext pageContext) {
        this.pageContext = pageContext;
        this.iterator = activeTasks.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public TR buildNext() {
        TR tr = new TR();
        WfTask task = iterator.next();

        TD stateTd = new TD(task.getName());
        tr.addElement(stateTd);
        stateTd.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD executorsTd = new TD();
        tr.addElement(executorsTd);
        executorsTd.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        if (task.getOwner() != null) {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, task.getOwner().getId(),
                    pageContext, PortletUrlType.Render);
            executorsTd.addElement(new A(url, ExecutorNameConverter.getName(task.getOwner(), pageContext)));
        }
        return tr;
    }
}
