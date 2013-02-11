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
package ru.runa.af.web.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;

import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdsForm;

/**
 * @author petrmikheev
 * @struts:form name = "botTasksForm"
 */
public class BotTasksForm extends IdsForm {
    private static final long serialVersionUID = 1L;

    public static final String BOT_TASK_INPUT_NAME_PREFIX = "task(";

    public static final String NAME_INPUT_NAME = ").name";

    public static final String HANDLER_INPUT_NAME = ").handler";

    public static final String CONFIG_INPUT_NAME = ").config";

    public static final String CONFIG_FILE_INPUT_NAME = ").configFile";

    private Map<Long, Object> tasksMap;

    public Map<Long, Object> getTasksMap() {
        return tasksMap;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        tasksMap = new HashMap<Long, Object>();
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (getTasksMap() == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_WEB_CLIENT_NULL_VALUE));
        }
        return errors;
    }

    public void setTask(String taskId, Object taskObject) {
        tasksMap.put(new Long(taskId), taskObject);
    }

    public BotTaskForm getBotTaskForm(Long id) {
        BotTaskForm task = (BotTaskForm) tasksMap.get(id);
        if (task == null) {
            task = new BotTaskForm();
            setTask(id.toString(), task);
        }
        return task;
    }

    static public class BotTaskForm {
        private String name;

        private String handler;

        private String config;

        private FormFile configFile;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public FormFile getConfigFile() {
            return configFile;
        }

        public void setConfigFile(FormFile configFile) {
            this.configFile = configFile;
        }
    }
}
