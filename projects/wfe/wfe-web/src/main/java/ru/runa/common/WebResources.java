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
package ru.runa.common;

import java.util.Properties;

import ru.runa.wfe.commons.ClassLoaderUtil;

/**
 * Created on 30.09.2004
 * 
 */
public class WebResources {
    private static final Properties PROPERTIES = ClassLoaderUtil.getPropertiesNotNull("wfe.web.properties");

    public static final String ACTION_MAPPING_UPDATE_EXECUTOR = "/manage_executor";
    public static final String ACTION_MAPPING_MANAGE_EXECUTORS = "/manage_executors";
    public static final String ACTION_MAPPING_MANAGE_SYSTEM = "/manage_system";
    public static final String ACTION_MAPPING_MANAGE_RELATION = "/manage_relation";

    public static final String ACTION_MAPPING_MANAGE_EXECUTOR_RIGHT_RELATION = "/manage_executor_relation_right";
    public static final String ACTION_MAPPING_MANAGE_EXECUTOR_LEFT_RELATION = "/manage_executor_relation_left";

    public static final String ACTION_MAPPING_DISPLAY_SWIMLANE = "/display_swimlane";

    /* default local action forwards */
    public static final String FORWARD_FAILURE_EXECUTOR_DOES_NOT_EXIST = "failure_executor_does_not_exist";
    /* Validation rules */
    public static final int VALIDATOR_STRING_255 = 255;

    public static final String ACTION_MAPPING_MANAGE_DEFINITION = "/manage_process_definition";
    public static final String ACTION_MAPPING_MANAGE_PROCESS = "/manage_process";
    public static final String ACTION_SHOW_PROCESS_GRAPH = "/show_process_graph";
    public static final String ACTION_SHOW_GRAPH_HISTORY = "/show_graph_history";
    public static final String ACTION_MAPPING_START_PROCESS = "/startProcess";
    public static final String ACTION_MAPPING_SUBMIT_TASK_DISPATCHER = "/submitTaskDispatcher";
    public static final String ACTION_MAPPING_REDEPLOY_PROCESS_DEFINITION = "/redeploy_process_definition";
    public static final String UNAUTHORIZED_EXECUTOR_NAME = "label.executor.unauthorized";
    public static final String NON_EXISTING_EXECUTOR_NAME = "label.executor.non_existing";
    public static final String FORWARD_SUCCESS_DISPLAY_START_FORM = "success_display_start_form";
    public static final String FORWARD_FAILURE_PROCESS_DEFINITION_DOES_NOT_EXIST = "failure_process_definition_does_not_exist";
    public static final String FORWARD_FAILURE_PROCESS_DOES_NOT_EXIST = "failure_process_does_not_exist";

    public static final String START_PROCESS_IMAGE = "/images/start.gif";
    public static final String START_PROCESS_DISABLED_IMAGE = "/images/start-disabled.gif";

    public static final String HIDDEN_ONE_TASK_INDICATOR = "one_task_hidden_field";
    public static final String HIDDEN_TASK_PREVIOUS_OWNER_ID = "taskOwnerId_hidden_field";

    public static String getStringProperty(String name) {
        return PROPERTIES.getProperty(name);
    }

    public static String getStringProperty(String name, String defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(result);
    }

    public static int getIntegerProperty(String name, int defaultValue) {
        String result = getStringProperty(name);
        if (result == null) {
            return defaultValue;
        }
        return Integer.parseInt(result);
    }

    public static String getTaskFormBuilderClassName(String formFileType) {
        return getStringProperty("task.form.builder." + formFileType);
    }

    public static String getStartFormBuilderClassName(String formFileType) {
        return getStringProperty("task.form.builder.start." + formFileType);
    }

    public static boolean isHighlightRequiredFields() {
        return getBooleanProperty("task.form.highlightRequiredFields", false);
    }

    public static int getDiagramRefreshInterval() {
        return getIntegerProperty("process.graph.autoRefreshInterval.seconds", 0);
    }

    public static boolean isGroupBySubprocessEnabled() {
        return getBooleanProperty("group.subprocess.enabled", false);
    }

    public static boolean isShowGraphMode() {
        return getBooleanProperty("process.showGraphMode", false);
    }

    public static boolean isNTLMSupported() {
        return getBooleanProperty("ntlm.enabled", false);
    }

    public static boolean isKrbSupported() {
        return getBooleanProperty("kerberos.enabled", false);
    }

    public static String getDomainName() {
        return getStringProperty("ntlm.domain");
    }

    public static String getVersion() {
        return getStringProperty("version", "UNDEFINED");
    }

    public static boolean isVersionDisplay() {
        return getBooleanProperty("version.display", false);
    }

    public static boolean isAutoShowForm() {
        return getBooleanProperty("task.form.autoShowNext", false);
    }

    public static String getAdditionalLinks() {
        try {
            String className = getStringProperty("menu.additional_links", null);
            if (className != null && className.length() > 0) {
                Class<?> getter = ClassLoaderUtil.loadClass(className);
                return getter.getDeclaredMethod("getAdditionalLinks", (Class[]) null).invoke(getter, (Object[]) null).toString();
            }
        } catch (Exception e) {

        }
        return "";
    }

    public static int getViewLogsLimitLinesCount() {
        return getIntegerProperty("view.logs.limit.lines.count", 10000);
    }

    public static int getViewLogsAutoReloadTimeout() {
        return getIntegerProperty("view.logs.timeout.autoreload.seconds", 15);
    }

    public static boolean isDisplayVariablesJavaType() {
        return getBooleanProperty("process.variables.displayJavaType", true);
    }
}
