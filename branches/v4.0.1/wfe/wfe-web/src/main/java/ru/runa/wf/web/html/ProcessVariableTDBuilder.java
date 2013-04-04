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

import org.apache.ecs.html.TD;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wf.web.ftl.method.ViewUtil;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * @author Konstantinov Aleksey
 */
public class ProcessVariableTDBuilder implements TDBuilder {

    class ProcessIdExtractor implements IdentifiableExtractor {
        private static final long serialVersionUID = 1L;

        @Override
        public Identifiable getIdentifiable(final Object o, final Env env) {
            Process identifiable = new Process();
            identifiable.setId(((WfProcess) o).getId());
            return identifiable;
        }

    }

    String variableName = null;

    public ProcessVariableTDBuilder(String varName) {
        variableName = varName;
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfVariable variable = env.getProcessVariable(object, new ProcessIdExtractor(), variableName);
        if (variable.getValue() != null) {
            WfProcess process = (WfProcess) object;
            return ViewUtil.getVariableValueHtml(env.getUser(), new StrutsWebHelper(env.getPageContext()), process.getId(), variable);
        }
        return "";
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
