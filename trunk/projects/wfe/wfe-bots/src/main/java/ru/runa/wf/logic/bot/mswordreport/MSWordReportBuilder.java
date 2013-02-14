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
package ru.runa.wf.logic.bot.mswordreport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public abstract class MSWordReportBuilder {
    protected final Log log = LogFactory.getLog(getClass());
    protected final MSWordReportTaskSettings settings;
    protected final IVariableProvider variableProvider;

    public MSWordReportBuilder(MSWordReportTaskSettings settings, IVariableProvider variableProvider) {
        this.settings = settings;
        this.variableProvider = variableProvider;
    }

    public abstract void build(String reportTemporaryFileName);

    protected String getVariableValue(BookmarkVariableMapping mapping) {
        WfVariable variable = variableProvider.getVariable(mapping.getVariableName());
        if (variable == null || variable.getValue() == null) {
            throw new MSWordReportException(MSWordReportException.VARIABLE_NOT_FOUND_IN_PROCESS, mapping.getVariableName());
        }
        return variable.getFormatNotNull().format(variable.getValue());
    }

}
