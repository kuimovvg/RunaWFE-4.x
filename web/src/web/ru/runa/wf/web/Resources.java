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
package ru.runa.wf.web;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 30.09.2004
 * 
 */
public class Resources extends ResourceCommons {
    private static final String WF_PROPERTIES = "web";

    public static final String ACTION_MAPPING_MANAGE_DEFINITION = "/manage_process_definition";

    public static final String ACTION_MAPPING_MANAGE_TASK = "/submit_task";

    public static final String ACTION_MAPPING_MANAGE_INSTANCE = "/manage_process_instance";

    public static final String ACTION_SHOW_GRAPH_INSTANCE = "/show_graph_instance";

    public static final String ACTION_SHOW_GRAPH_HISTORY = "/show_graph_history";

    public static final String ACTION_MAPPING_START_INSTANCE = "/startProcessInstance";

    public static final String ACTION_MAPPING_SUBMIT_TASK_DISPATCHER = "/submitTaskDispatcher";

    public static final String ACTION_MAPPING_REDEPLOY_PROCESS_DEFINITION = "/redeploy_process_definition";

    public static final String UNAUTHORIZED_EXECUTOR_NAME = "label.executor.unauthorized";

    public static final String NON_EXISTING_EXECUTOR_NAME = "label.executor.non_existing";

    private static final String TASK_FORM_FILE_TYPE_PREFIX = "ru.runa.wf.client.task.form.file.type";

    private static final String START_FORM_FILE_TYPE_PREFIX = "ru.runa.wf.client.start.form.file.type";

    private static final String HIGHTLIGHT_REQUIRED_FIELDS = "highlightRequiredFields";

    public static final String FORWARD_SUCCESS_DISPLAY_START_FORM = "success_display_start_form";

    public static final String FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST = "failure_process_definition_does_not_exist";
    public static final String FORWARD_FAILURE_PROCESS_INSTANCE_DOES_NOT_EXIST = "failure_process_instance_does_not_exist";

    public static final String START_INSTANCE_IMAGE = "/images/start.gif";
    public static final String START_INSTANCE_DISABLED_IMAGE = "/images/start-disabled.gif";

    public static final String HIDDEN_ONE_TASK_INDICATOR = "one_task_hidden_field";
    public static final String HIDDEN_TASK_SWIMLANE = "taskSwimlane_hidden_field";
    public static final String HIDDEN_ACTOR_ID_INPUT_NAME = "actorId";

    private Resources() {
        super(WF_PROPERTIES);
    }

    public static String getTaskFormBuilderClassName(String formFileType) {
        return readProperty(TASK_FORM_FILE_TYPE_PREFIX + "." + formFileType, WF_PROPERTIES);
    }

    public static String getStartFormBuilderClassName(String formFileType) {
        return readProperty(START_FORM_FILE_TYPE_PREFIX + "." + formFileType, WF_PROPERTIES);
    }

    public static boolean highlightRequiredFields() {
        return Boolean.valueOf(readPropertyIfExist(HIGHTLIGHT_REQUIRED_FIELDS, WF_PROPERTIES, Boolean.FALSE.toString()));
    }
}
