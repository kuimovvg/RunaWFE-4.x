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
package ru.runa.common.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created 14.05.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class Messages {
    public static final String BUTTON_ADD = "button.add";
    public static final String BUTTON_APPLY = "button.apply";
    public static final String BUTTON_CANCEL = "button.cancel";
    public static final String BUTTON_REMOVE = "button.remove";
    public static final String BUTTON_SAVE = "button.save";
    public static final String BUTTON_SAVE_AS = "button.save_as";

    public static final String BUTTON_ADD_SUBSTITUTION = "button.add_substitution";
    public static final String BUTTON_ADD_TERMINATOR = "button.add_terminator";

    public static final String TITLE_RELATIONS = "title.relations";
    public static final String LINK_CREATE_RELATION = "link.create_relation";
    public static final String LABEL_CREATE_RELATION_TO = "label.create_relation.to";
    public static final String LABEL_CREATE_RELATION_FROM = "label.create_relation.from";
    public static final String LINK_CREATE_RELATION_GROUP = "link.create_relation_group";
    public static final String LABEL_CREATE_RELATION_GROUP_NAME = "label.create_relation_group.name";
    public static final String LABEL_CREATE_RELATION_GROUP_DESCRIPTION = "label.create_relation_group.description";
    public static final String TITLE_EXECUTOR_RIGHT_RELATIONS = "title.executor.right.relations";
    public static final String TITLE_EXECUTOR_LEFT_RELATIONS = "title.executor.left.relations";

    public static final String MESSAGE_RELATION_GROUP_EXISTS = "label.relation_group.exists";
    public static final String MESSAGE_RELATION_GROUP_DOESNOT_EXISTS = "label.relation_group.not_exists";

    public static final String TITLE_EXECUTORS = "title.executors";
    public static final String TITLE_PERMISSION_OWNERS = "title.permission_owners";
    public static final String TITLE_SUBSTITUTION_CRITERIA = "title.substitution_criteria";
    public static final String TITLE_ADD_EXECUTORS_TO_GROUP = "title.add_executors_to_group";
    public static final String TITLE_ADD_EXECUTOR_TO_GROUP = "title.add_executor_to_groups";
    public static final String TITLE_EXECUTOR_DETAILS = "title.executor_details";
    public static final String TITLE_EXECUTOR_GROUPS = "title.executor_groups";
    public static final String TITLE_GROUP_MEMBERS = "title.group_members";
    public static final String TITLE_ACTOR_PASSWORD = "title.actor_password";
    public static final String TITLE_ACTOR_STATUS = "title.actor_status";
    public static final String TITLE_GRANT_PERMISSION = "title.grant_permission";

    public static final String TITLE_PROCESS_DEFINITIONS = "title.process_definitions";
    public static final String TITLE_DEPLOY_DEFINITION = "title.deploy_definition";
    public static final String TITLE_REDEPLOY_DEFINITION = "title.redeploy_definition";
    public static final String BUTTON_VALIDATE_DEFINITION = "button.validate_definition";
    public static final String BUTTON_DEPLOY_DEFINITION = "button.deploy_definition";
    public static final String BUTTON_REDEPLOY_DEFINITION = "button.redeploy_definition";

    public static final String TITLE_PROCESS_INSTANCES = "title.process_instances";
    public static final String TITLE_INSANCE_SWINLANE_LIST = "title.instance_swimlane_list";
    public static final String TITLE_INSANCE_TASKS_LIST = "title.instance_tasks_list";
    public static final String TITLE_INSANCE_SUBPROCESS_LIST = "title.instance_subprocess_list";
    public static final String TITLE_INSANCE_VARIABLE_LIST = "title.instance_variable_list";
    public static final String TITLE_PROCESS_INSTANCE = "title.process_instance";
    public static final String TITLE_PROCESS_DEFINITION = "title.process_definition";
    public static final String TITLE_TASKS = "title.tasks";
    public static final String TITLE_PROCESS_GRAPH = "title.process_graph";

    public static final String TITLE_HISTORY = "title.history";
    public static final String TITLE_SYSTEM_HISTORY = "title.system.history";

    public static final String BUTTON_CREATE_ACTOR = "button.create_actor";
    public static final String BUTTON_CREATE_GROUP = "button.create_group";

    public static final String BUTTON_FORM = "button.form";
    public static final String BUTTON_COMPLETE = "button.complete";
    public static final String BUTTON_LOGOUT = "button.logout";

    public static final String BUTTON_CANCEL_INSTANCE = "button.cancel_instance";
    public static final String BUTTON_UNDEPLOY_DEFINITION = "button.undeploy_definition";

    public static final String BUTTON_ACCEPT_TASK = "button.accept_task";

    public static final String LABEL_PASSWORD = "label.password";
    public static final String LABEL_PASSWORD_CONFIRM = "label.password_confirm";

    public static final String LABEL_EXECUTOR_NAME = "label.executor_name";
    public static final String LABEL_EXECUTOR_DESCRIPTION = "label.executor_description";
    public static final String LABEL_ACTOR_FULL_NAME = "label.actor_fullname";
    public static final String LABEL_ACTOR_CODE = "label.actor_code";
    public static final String LABEL_ACTOR_IS_ACTIVE = "label.actor_is_active";
    public static final String LABEL_ACTOR_EMAIL = "label.actor_email";
    public static final String LABEL_ACTOR_PHONE = "label.actor_phone";
    public static final String LABEL_GROUP_AD = "label.group_ad";

    public static final String DYNAMIC_GROUP_NAME = "dynamic_group.name";
    public static final String PROCESS_STARTER_NAME = "process_starter.name";

    public static final String LABEL_START_INSTANCE = "label.start_instance";
    public static final String LABEL_PROPERTIES = "label.properties";

    public static final String LABEL_REDEPLOY_PROCESS_DEFINIION = "label.redeploy_process_definition";

    public static final String LABEL_VIEW_SIZE = "label.view_size";
    public static final String LABEL_FIELD_NAMES = "label.field_name";
    public static final String LABEL_DISPLAY_POSITION = "label.display_position";
    public static final String LABEL_SORTING_TYPE = "label.sorting_type";
    public static final String LABEL_SORTING_POSITION = "label.sorting_position";
    public static final String LABEL_FILTER_CRITERIA = "label.filter_criteria";
    public static final String LABEL_FILTER_ENABLE = "label.filter_enable";
    public static final String LABEL_GROUPING = "label.grouping";
    public static final String LABEL_NONE = "label.none";
    public static final String LABEL_ASC = "label.asc";
    public static final String LABEL_DESC = "label.desc";

    public static final String LABEL_CHOOSE_PAGE = "label.choose_page";
    public static final String LABEL_TOTAL = "label.total";
    public static final String LABEL_PAGING_NEXT_PAGE = "label.paging_next_page";
    public static final String LABEL_PAGING_PREV_PAGE = "label.paging_prev_page";
    public static final String LABEL_PAGING_NEXT_RANGE = "label.paging_next_range";
    public static final String LABEL_PAGING_PREV_RANGE = "label.paging_prev_range";

    public static final String LABEL_SWIMLANE_NAME = "label.swimlane_name";
    public static final String LABEL_SWIMLANE_ASSIGNMENT = "label.swimlane_assigned_to";
    public static final String LABEL_SWIMLANE_ORGFUNCTION = "label.swimlane_organization_function";
    public static final String SUBSTITUTION_ALWAYS = "substitution.always";
    public static final String SUBSTITUTION_OUT_OF_DATE = "substitution.out.of.date.error";

    public static final String LABEL_STATE_NAME = "label.state_name";
    public static final String LABEL_PARENT_PROCESS = "label.parent_process";
    public static final String LABEL_SUB_PROCESS = "label.sub_process";
    public static final String LABEL_SWIMLANE = "label.swimlane";
    public static final String LABEL_VARIABLES = "label.variables";

    public static final String LABEL_VARIABLE_NAME = "label.variable_name";
    public static final String LABEL_VARIABLE_VALUE = "label.variable_value";
    public static final String LABEL_VARIABLE_TYPE = "label.variable_type";

    public static final String LABEL_SHOW_CONTROLS = "label.show_controls";
    public static final String LABEL_HIDE_CONTROLS = "label.hide_controls";

    public static final String LABEL_SUBSTITUTORS = "label.substitutors";
    public static final String LABEL_SUBSTITUTORS_CRITERIA = "label.substitutors_criteria";
    public static final String LABEL_SUBSTITUTORS_ENABLED = "label.substitutors_enabled";

    public static final String LABEL_SUBSTITUTION_CRITERIA_NAME = "label.substitution_criteria_name";
    public static final String LABEL_SUBSTITUTION_CRITERIA_TYPE = "label.substitution_criteria_type";
    public static final String LABEL_SUBSTITUTION_CRITERIA_CONF = "label.substitution_criteria_conf";

    public static final String VAR_TAG_DATE_TIME_INPUT_LOCALIZATION_JS_PATH = "var_tag.date_time_input.localization_js";

    /* error messages */
    public static final String EXCEPTION_WEB_CLIENT_LOGIN_FAILED = "login.failed";

    public static final String EXCEPTION_WEB_CLIENT_INTERNAL = "internal.exception";

    public static final String EXCEPTION_WEB_CLIENT_UNKNOWN = "unknown.exception";

    public static final String EXCEPTION_WEB_CLIENT_SESSION_INVALID = "session.invalid";

    public static final String EXCEPTION_WEB_CLIENT_TABLE_VIEW_SETUP_FORMAT_INCORRECT = "view.setup.format.invalid";

    public static final String EXCEPTION_WEB_CLIENT_AUTHORIZATION = "authorization.exception";
    public static final String EXCEPTION_WEB_CLIENT_AUTHENTICATION = "authentication.exception";

    public static final String EXCEPTION_WEB_CLIENT_PASSWORD_IS_WEAK = "executor.weak.password";
    public static final String EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_EXISTS = "executor.already.exists.exception";
    public static final String EXCEPTION_WEB_CLIENT_EXECUTOR_DOES_NOT_EXISTS = "executor.does.not.exists.exception";
    public static final String EXCEPTION_ACTOR_DOES_NOT_EXISTS = "ru.runa.wf.web.actor.does.not.exists.exception";
    public static final String EXCEPTION_GROUP_DOES_NOT_EXISTS = "ru.runa.wf.web.group.does.not.exists.exception";
    public static final String EXCEPTION_WEB_CLIENT_EXECUTOR_ALREADY_IN_GROUP = "executor.already.in.group";
    public static final String EXCEPTION_WEB_CLIENT_EXECUTOR_NOT_IN_GROUP = "executor.not.in.group";

    public static final String EXCEPTION_BROWSER_CAPABILITIES = "browser.incompatible.exception";

    public static final String EXCEPTION_BOTSTATION_ALREADY_EXISTS = "botstation.already.exists.exception";
    public static final String EXCEPTION_BOTSTATION_DOESNOT_EXISTS = "botstation.doesnot.exists.exception";
    public static final String EXCEPTION_BOT_ALREADY_EXISTS = "bot.already.exists.exception";
    public static final String EXCEPTION_BOT_DOESNOT_EXISTS = "bot.doesnot.exists.exception";
    public static final String EXCEPTION_BOT_TASK_ALREADY_EXISTS = "bot.task.already.exists.exception";
    public static final String EXCEPTION_BOT_TASK_DOESNOT_EXISTS = "bot.task.doesnot.exists.exception";

    public static final String ERROR_WEB_CLIENT_NULL_VALUE = "emptyvalue";
    public static final String ERROR_WEB_CLIENT_VALIDATION = "validation.error";
    public static final String ERROR_WEB_CLIENT_PASSWORDS_NOT_MATCH = "executor.passwords.not.match";

    public static final String ERROR_WEB_CLIENT_DEFINITION_ALREADY_EXISTS = "definition.already.exists.error";

    public static final String ERROR_WEB_CLIENT_DEFINITION_DOES_NOT_EXIST = "definition.does.not.exist.error";
    public static final String ERROR_WEB_CLIENT_DEFINITION_NAME_MISMATCH = "definition.name.mismatch.error";
    public static final String ERROR_WEB_CLIENT_INSTANCE_DOES_NOT_EXIST = "instance.does.not.exist.error";
    public static final String ERROR_WEB_CLIENT_TASK_DOES_NOT_EXIST = "task.does.not.exist.error";
    public static final String EXCEPTION_WEB_CLIENT_DEFINITION_FILE_NOT_FOUND = "definition.file.not.found";
    public static final String EXCEPTION_WEB_CLIENT_VARTAG_TYPE_MISMATCH = "vartag.type.mismatch";

    public static final String DEFINITION_ARCHIVE_FORMAT_ERROR = "definition.archive.format.error";
    public static final String DEFINITION_FILE_FORMAT_ERROR = "definition.file.format.error";
    public static final String DEFINITION_DEPRECATED_FORMAT_ERROR = "definition.deprecated.format.error";
    public static final String DEFINITION_FILE_DOES_NOT_EXIST_ERROR = "definition.file.does.not.exist.error";
    public static final String DEFINITION_DELEGATION_CLASS_CAN_NOT_BE_FOUND_ERROR = "definition.delegation.class.can.not.be.found.error";
    public static final String EXCEPTION_DEFINITION_TYPE_NOT_PRESENT = "definition.type.not.present";
    public static final String DEFINITION_VARIABLE_NOT_PRESENT = "definition.variable.not.found";

    public static final String TASK_COMPLETED = "task.completed";
    public static final String PROCESS_INSTANCE_STARTED = "process.instance.started";
    public static final String PROCESS_INSTANCE_CANCELDED = "process.instance.canceled";

    public static final String TASK_FORM_NOT_DEFINED_ERROR = "task.form.not.defined.error";

    public static final String MESSAGE_WEB_CLIENT_VARIABLE_FORMAT_ERROR = "variable.format.error";
    public static final String MESSAGE_WEB_CLIENT_VALIDATION_ERROR = "validation.form.error";

    public static final String TASK_FORM_ERROR = "task.form.error";

    public static final String LABEL_PROCESS_VARIABLE_NAME = "label.process_variable_name";
    public static final String LABEL_PROCESS_VARIABLE_VALUE = "label.process_variable_value";
    public static final String LABEL_PROCESS_VARIABLE_FILTER_ON = "label.is_process_variable_filter_on";

    public static final String TASK_WAS_ALREADY_ACCEPTED = "task.was.already.accepted";

    public static final String DEFINITION_HAS_UNEXISTENT_SUBPROCESS = "definition.has.unexistent.subprocess";
    public static final String INSTANCE_HAS_SUPER_PROCESS = "instance.has.super.process";

    public static final String BUTTON_BOT_STATION_CONFIGURE_PERMISSION = "button.bot_station_configure";
    public static final String BUTTON_DELETE_BOT_STATION = "button.delete_bot_station";
    public static final String BUTTON_ADD_BOT_STATION = "button.add_bot_station";
    public static final String BUTTON_ADD_BOT = "button.add_bot";
    public static final String BUTTON_SAVE_BOT = "button.save_bot";
    public static final String BUTTON_DEPLOY_BOT = "button.deploy_bot";
    public static final String LABEL_REPLACE_BOT_TASKS = "label.replace_bot_tasks";
    public static final String BUTTON_SAVE_BOT_STATION = "button.save_bot_station";
    public static final String BUTTON_DEPLOY_BOT_STATION = "button.deploy_bot_station";
    public static final String BUTTON_DELETE_BOT = "button.delete_bot";
    public static final String TITLE_BOT_STATIONS = "title.bot_stations";
    public static final String TITLE_BOT_LIST = "title.bot_list";
    public static final String TITLE_BOT_TASK_LIST = "title.bot_task_list";
    public static final String TITLE_ADD_BOT_STATION = "title.add_bot_station";
    public static final String TITLE_ADD_BOT = "title.add_bot";
    public static final String LABEL_BOT_STATION_NAME = "label.bot_station_name";
    public static final String LABEL_BOT_STATION_ADDRESS = "label.bot_station_address";
    public static final String LABEL_BOT_PASSWORD = "label.bot_password";
    public static final String LABEL_BOT_NAME = "label.bot_name";
    public static final String LABEL_BOT_TIMEOUT = "label.bot_timeout";
    public static final String LABEL_BOT_TASK_NAME = "label.bot_task_name";
    public static final String LABEL_BOT_TASK_HANDLER = "label.bot_task_handler";
    public static final String LABEL_BOT_TASK_CONFIG = "label.bot_task_config";
    public static final String TITLE_BOT_STATION_DETAILS = "title.bot_station_details";
    public static final String TITLE_BOT_STATION_STATUS = "title.bot_station_status";
    public static final String TITLE_BOT_DETAILS = "title.bot_details";
    public static final String LABEL_UNKNOWN_BOT_HANDLER = "label.unknown_bot_handler";
    public static final String LABEL_BOT_WFE_USER = "label.bot_wfe_user";
    public static final String LABEL_BOT_TASK_CONFIG_DOWNLOAD = "label.bot_task_config.download";
    public static final String LABEL_BOT_TASK_CONFIG_EDIT = "label.bot_task_config.edit";
    public static final String MESSAGE_BOTSTATION_ON = "button.botstation_on";
    public static final String MESSAGE_BOTSTATION_OFF = "button.botstation_off";
    public static final String MESSAGE_PERIODIC_BOTS_INVOCATION_ON = "button.periodic_bots_invocation_on";
    public static final String MESSAGE_PERIODIC_BOTS_INVOCATION_OFF = "button.periodic_bots_invocation_off";
    public static final String MESSAGE_INCORRECT_BOT_ARCHIVE = "label.incorrect_bot_archive";
    public static final String BUTTON_STOP_PERIODIC_BOTS_INVOCATION = "button.stop_periodic_bots_invocation";
    public static final String BUTTON_START_PERIODIC_BOTS_INVOCATION = "button.start_periodic_bots_invocation";

    public static final String LABEL_SHOW_HISTORY = "label.manage_history";
    public static final String LABEL_SHOW_GRAPH_HISTORY = "label.manage_graph_history";

    public static final String HISTORY_PH_EXECUTOR = "history.placeholders.executor";
    public static final String HISTORY_PH_SUBSTITUTOR = "history.placeholders.substitutor";
    public static final String HISTORY_PH_NODE_NAME = "history.placeholders.node_name";
    public static final String HISTORY_PH_NODE_TYPE = "history.placeholders.node_type";
    public static final String HISTORY_PH_SWIMLANE_NAME = "history.placeholders.swimlane_name";
    public static final String HISTORY_PH_SWIMLANE_VALUE = "history.placeholders.swimlane_value";
    public static final String HISTORY_PH_ACTION = "history.placeholders.action";
    public static final String HISTORY_PH_ACTION_TYPE = "history.placeholders.action_type";
    public static final String HISTORY_PH_ACTION_CONF = "history.placeholders.action_conf";
    public static final String HISTORY_PH_TRANSITION_NAME = "history.placeholders.transition_name";
    public static final String HISTORY_PH_TRANSITION_FROM = "history.placeholders.transition_from";
    public static final String HISTORY_PH_TRANSITION_TO = "history.placeholders.transition_to";
    public static final String HISTORY_PH_VARIABLE_NAME = "history.placeholders.variable_name";
    public static final String HISTORY_PH_VARIABLE_OLD = "history.placeholders.variable_old";
    public static final String HISTORY_PH_VARIABLE_NEW = "history.placeholders.variable_new";

    public static final String HISTORY_SYSTEM_PH_PI = "history.system.placeholders.process_instance";
    public static final String HISTORY_SYSTEM_PH_PD = "history.system.placeholders.process_definition";
    public static final String HISTORY_SYSTEM_PH_VERSION = "history.system.placeholders.version";

    public static final String HISTORY_NODE_STATE = "history.node.state";
    public static final String HISTORY_NODE_TASK = "history.node.task";
    public static final String HISTORY_NODE_DECISION = "history.node.decision";
    public static final String HISTORY_NODE_JOIN = "history.node.join";
    public static final String HISTORY_NODE_FORK = "history.node.fork";
    public static final String HISTORY_NODE_END = "history.node.end";
    public static final String HISTORY_NODE_PROCESS = "history.node.process";
    public static final String HISTORY_NODE_SEND_MESSAGE = "history.node.send_message";
    public static final String HISTORY_NODE_RECEIVE_MESSAGE = "history.node.receive_message";

    public static final String HISTORY_DYNAMIC_GROUP_CREATED = "history.dynamic_group.created";
    public static final String HISTORY_NODE_ENTER = "history.node_enter";
    public static final String HISTORY_PROCESS_START = "history.process_start";
    public static final String HISTORY_REASSIGN_SWIMLANE = "history.reassign_swimlane";
    public static final String HISTORY_TASK_START = "history.task_start";
    public static final String HISTORY_TASK_DONE = "history.task_done";
    public static final String HISTORY_TASK_DONE_BY_TIMER = "history.task_done_by_timer";
    public static final String HISTORY_FIRE_ACTION = "history.fire_action";
    public static final String HISTORY_ASSIGN_SWIMLANE = "history.assign_swimlane";
    public static final String HISTORY_ASSIGN_VARIABLE = "history.assign_variable";
    public static final String HISTORY_CHANGE_VARIABLE = "history.change_variable";
    public static final String HISTORY_CANCEL_PROCESS = "history.cancel_process";
    public static final String HISTORY_END_PROCESS = "history.end_process";
    public static final String HISTORY_SUBPROCESS_START = "history.subprocess_started";
    public static final String HISTORY_SUBPROCESS_COMPLETE = "history.subprocess_complete";

    public static final String HISTORY_SYSTEM_PI_DELETED = "history.system.pi_delete";
    public static final String HISTORY_SYSTEM_PD_DELETED = "history.system.pd_delete";
    public static final String HISTORY_SYSTEM_PD_LAST_VERSION_EXCEPTION = "history.system.pd_last_version_exception";
    public static final String HISTORY_SYSTEM_PD_PI_EXIST = "history.system.pd_pi_exist_exception";

    public static final String CONF_POPUP_BUTTON_OK = "confirmpopup.button.ok";
    public static final String CONF_POPUP_BUTTON_CANCEL = "confirmpopup.button.cancel";
    public static final String CONF_POPUP_CONFIRM_ACTION = "confirmpopup.confirm.action";
    public static final String CONF_POPUP_REMOVE_EXECUTORS = "confirmpopup.remove.executors";
    public static final String CONF_POPUP_REMOVE_EXECUTORS_FROM_GROUPS = "confirmpopup.remove.executorsfromgroups";
    public static final String CONF_POPUP_REMOVE_BOT = "confirmpopup.remove.bot";
    public static final String CONF_POPUP_REMOVE_BOT_STATION = "confirmpopup.remove.botstation";
    public static final String CONF_POPUP_DEPLOY_PROCESSDEFINIION = "confirmpopup.deploy.processdefinition";
    public static final String CONF_POPUP_REDEPLOY_PROCESSDEFINIION = "confirmpopup.redeploy.processdefinition";
    public static final String CONF_POPUP_UNDEPLOY_PROCESSDEFINIION = "confirmpopup.undeploy.processdefinition";
    public static final String CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA = "confirmpopup.remove.substitutioncriteria";
    public static final String CONF_POPUP_CANCEL_PROCESSINSTANCE = "confirmpopup.cancel.processinstance";
    public static final String CONF_POPUP_ACCEPT_TASK = "confirmpopup.accept.task";
    public static final String CONF_POPUP_EXECUTE_TASK = "confirmpopup.execute.task";
    public static final String CONF_POPUP_START_INSTANCE = "confirmpopup.start.instance";
    public static final String CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ALL = "confirmpopup.substitutioncriteria.button.all";
    public static final String CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ONLY = "confirmpopup.substitutioncriteria.button.only";

    public static final String SYSTEM_LOG_UNDEFINED_TYPE = "history.system.type.undefined";

    public static final String LABEL_SUBSTITUTION_CRITERIA_USED_BY = "label.substitutioncriteria.usedby";

    private Messages() {
    }

    public static String getMessage(String key, PageContext pageContext) {
        try {
            String value = Commons.getMessage(key, pageContext);
            if (value == null) {
                value = '!' + key + '!';
            }
            return value;
        } catch (JspException e) {
            return '!' + key + '!';
        }
    }
}
