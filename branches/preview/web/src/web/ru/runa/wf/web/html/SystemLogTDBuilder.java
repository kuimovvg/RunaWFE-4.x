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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;

import ru.runa.af.log.ProcessDefinitionDeleteExceptionLog;
import ru.runa.af.log.ProcessDefinitionDeleteLog;
import ru.runa.af.log.ProcessInstanceDeleteLog;
import ru.runa.af.log.SystemLog;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TDBuilder;

/**
 * {@link TDBuilder} implementation to show system log as human readable message.
 */
public class SystemLogTDBuilder implements TDBuilder {

    /**
     * Process instance place holder name. All occurrences of this place holder will be replaced with process instance identity.   
     */
    private static String PH_PROCESS_INSTANCE = null;

    /**
     * Process definition name place holder name. All occurrences of this place holder will be replaced with process definition name.   
     */
    private static String PH_PROCESS_DEFINITION = null;

    /**
     * Process definition version place holder name. All occurrences of this place holder will be replaced with process definition version.   
     */
    private static String PH_VERSION = null;

    private static synchronized void initPlacehlders(PageContext pageContext) {
        if (PH_PROCESS_INSTANCE != null) {
            return;
        }
        PH_PROCESS_INSTANCE = Messages.getMessage(Messages.HISTORY_SYSTEM_PH_PI, pageContext);
        PH_PROCESS_DEFINITION = Messages.getMessage(Messages.HISTORY_SYSTEM_PH_PD, pageContext);
        PH_VERSION = Messages.getMessage(Messages.HISTORY_SYSTEM_PH_VERSION, pageContext);
    }

    @Override
    public TD build(Object object, Env env) throws JspException {
        TD result = new TD(getValue(object, env));
        result.setClass(Resources.CLASS_LIST_TABLE_TD);
        return result;
    }

    @Override
    public String getValue(Object object, Env env) {
        initPlacehlders(env.getPageContext());
        SystemLog log = (SystemLog) object;
        if (log instanceof ProcessInstanceDeleteLog) {
            return Messages.getMessage(Messages.HISTORY_SYSTEM_PI_DELETED, env.getPageContext()).replaceAll("\\{" + PH_PROCESS_INSTANCE + "\\}",
                    String.valueOf(log.getId()));
        } else if (log instanceof ProcessDefinitionDeleteLog) {
            ProcessDefinitionDeleteLog processDefinitionDeleteLog = (ProcessDefinitionDeleteLog) log;
            String name = processDefinitionDeleteLog.getName();
            Long version = processDefinitionDeleteLog.getVersion();
            return Messages.getMessage(Messages.HISTORY_SYSTEM_PD_DELETED, env.getPageContext()).replaceAll("\\{" + PH_PROCESS_DEFINITION + "\\}",
                    name).replaceAll("\\{" + PH_VERSION + "\\}", String.valueOf(version));
        } else if (log instanceof ProcessDefinitionDeleteExceptionLog) {
            ProcessDefinitionDeleteExceptionLog processDefinitionDeleteExceptionLog = (ProcessDefinitionDeleteExceptionLog) log;
            String name = processDefinitionDeleteExceptionLog.getName();
            Long version = processDefinitionDeleteExceptionLog.getVersion();
            if (processDefinitionDeleteExceptionLog.getProcessInstanceExist()) {
                Messages.getMessage(Messages.HISTORY_SYSTEM_PD_PI_EXIST, env.getPageContext())
                        .replaceAll("\\{" + PH_PROCESS_DEFINITION + "\\}", name).replaceAll("\\{" + PH_VERSION + "\\}", String.valueOf(version));
            }
            if (processDefinitionDeleteExceptionLog.getLastVersion()) {
                return Messages.getMessage(Messages.HISTORY_SYSTEM_PD_LAST_VERSION_EXCEPTION, env.getPageContext()).replaceAll(
                        "\\{" + PH_PROCESS_DEFINITION + "\\}", name).replaceAll("\\{" + PH_VERSION + "\\}", String.valueOf(version));
            }
        }
        return "Unsupported log instance";
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
