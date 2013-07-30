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

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class ProcessSwimlaneAssignmentRowBuilder implements RowBuilder {
    private final User user;
    private final Iterator<WfTask> iterator;
    private final PageContext pageContext;

    public ProcessSwimlaneAssignmentRowBuilder(User user, List<WfTask> activeTasks, PageContext pageContext) {
        this.user = user;
        this.pageContext = pageContext;
        iterator = activeTasks.iterator();
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
        executorsTd.addElement(HTMLUtils.createExecutorElement(user, pageContext, task.getOwner()));
        return tr;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
