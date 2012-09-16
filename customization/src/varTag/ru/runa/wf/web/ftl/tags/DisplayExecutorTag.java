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

import ru.runa.af.Executor;
import ru.runa.af.service.ExecutorService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayExecutorTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String executorIdVarName = getParameterAs(String.class, 0);
        Long executorId = getVariableAs(Long.class, executorIdVarName, true);

        ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
        try {
            Executor executor = executorService.getExecutor(subject, executorId);
            return executor.getName();
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }
}
