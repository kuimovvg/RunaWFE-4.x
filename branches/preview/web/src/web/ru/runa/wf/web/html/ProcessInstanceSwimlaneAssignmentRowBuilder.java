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
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.Executor;
import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.af.web.Resources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.RowBuilder;

/**
 */
public class ProcessInstanceSwimlaneAssignmentRowBuilder implements RowBuilder {

    private final int enabledRowCount;

    private final Iterator<Map.Entry<String, List<Executor>>> iterator;

    private final PageContext pageContext;

    public ProcessInstanceSwimlaneAssignmentRowBuilder(Map<String, List<Executor>> executorsMap, PageContext pageContext) {
        this.pageContext = pageContext;
        iterator = executorsMap.entrySet().iterator();
        enabledRowCount = executorsMap.size();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public TR buildNext() {
        TR tr = new TR();
        Map.Entry<String, List<Executor>> entry = iterator.next();
        String stateName = entry.getKey();
        List<Executor> executors = entry.getValue();

        TD stateTd = new TD(stateName);
        tr.addElement(stateTd);
        stateTd.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD executorsTd = new TD();
        tr.addElement(executorsTd);
        executorsTd.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        Table table = new Table();
        executorsTd.addElement(table);
        table.setClass(ru.runa.common.web.Resources.CLASS_INVISIBLE_LIST_TABLE);

        for (Executor executor : executors) {
            TR actorTR = new TR();
            table.addElement(actorTR);
            TD actorTD = new TD();
            actorTR.addElement(actorTD);
            String url = Commons.getActionUrl(Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, String.valueOf(executor.getId()),
                    pageContext, PortletUrl.Render);
            actorTD.addElement(new A(url, ExecutorNameConverter.getName(executor, pageContext)));
            // actorTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        }
        return tr;
    }

    public int getEnabledRowsCount() {
        return enabledRowCount;
    }
}
