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

package ru.runa.notifier.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created on 2006
 * 
 * @author Gritsenko_S
 */

public class ResourcesManager {
    private static final Log log = LogFactory.getLog(ResourcesManager.class);

    public static final String LOGOUT_ACTION = "/logout.do";
    public static final String START_ACTION = "/manage_tasks.do";

    private static final String RESTART_RTN_ONCLOSE_PROPERTY = "restart.rtn.onclose";
    private static final String START_RTN_COMMAND_PROPERTY = "start.rtn.command";

    private static final String APPLICATION_NAME_PROPERTY = "application.name";

    private static final String TEASE_POPUP_PROPERTY = "popup.tease";

    private static final String TOOLTIP_POPUP_TASKS_PROPERTY = "popup.tasks";

    private static final String TOOLTIP_POPUP_NOT_LOGGED_PROPERTY = "popup.not.logged";

    private static final String TOOLTIP_POPUP_ERROR_PROPERTY = "popup.error";

    private static final String TOOLTIP_POPUP_NO_TASKS_PROPERTY = "popup.no.tasks";

    private static final String POPUP_NEWTASKS_PROPERTY = "popup.newtasks";

    private static final String USER_NAME_PROPERTY = "user.name";

    private static final String USER_PASSWORD_PROPERTY = "user.password";

    private static final String MESSAGE_LOGIN_PROPERTY = "login.message";

    private static final String MESSAGE_RETRY_PROPERTY = "retry.message";

    private static final String ERROR_LOGIN_PROPERTY = "error.login";

    private static final String ERROR_INTERNAL_PROPERTY = "error.internal";

    private static final String MENU_OPEN_PROPERTY = "menu.open";

    private static final String MENU_EXIT_PROPERTY = "menu.exit";

    private static final String CHECK_TASKS_TIMEOUT_PROPERTY = "check.tasks.timeout";

    private static final String SHOW_TRAY = "show.tray";

    private static final String BUNDLE_NAME = "application";

    private static final String POPUP_AUTOCLOSE_TIMEOUT_PROPERTY = "popup.autoclose.timeout";

    private static final String SOUNDS_ENABLED_PROPERTY = "sounds.enabled";

    private static final String ON_NEW_TASK_TRIGGER_COMMAND_PROPERTY = "onNewTask.trigger.command";

    private static final String UNREAD_TASKS_NOTIFICATION_TIMEOUT_PROPERTY = "unread.tasks.notification.timeout";

    private static ResourceBundle bundle;

    private static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        }
        return bundle;
    }

    public static String getProperty(String name) {
        return getBundle().getString(name);
    }

    public static boolean isRestartRtnOnClose() {
        try {
            String value = getBundle().getString(RESTART_RTN_ONCLOSE_PROPERTY);
            if (value == null) {
                return false;
            }
            if (value.compareToIgnoreCase("true") == 0) {
                return true;
            }
            return false;
        } catch (MissingResourceException e) {
            return false;
        }
    }

    public static String getStartRtnCommand() {
        return getBundle().getString(START_RTN_COMMAND_PROPERTY);
    }

    public static String getApplicationName() {
        return getBundle().getString(APPLICATION_NAME_PROPERTY);
    }

    public static String getTeasePopupText() {
        return getBundle().getString(TEASE_POPUP_PROPERTY);
    }

    public static String getTooltipPopupTasksText() {
        return getBundle().getString(TOOLTIP_POPUP_TASKS_PROPERTY);
    }

    public static String getTooltipPopupNotLoggedText() {
        return getBundle().getString(TOOLTIP_POPUP_NOT_LOGGED_PROPERTY);
    }

    public static String getTooltipPopupErrorText() {
        return getBundle().getString(TOOLTIP_POPUP_ERROR_PROPERTY);
    }

    public static String getTooltipPopupNoTasksText() {
        return getBundle().getString(TOOLTIP_POPUP_NO_TASKS_PROPERTY);
    }

    public static String getNewTasksPopupTitle() {
        return getBundle().getString(POPUP_NEWTASKS_PROPERTY);
    }

    public static String getUserName() {
        return getBundle().getString(USER_NAME_PROPERTY);
    }

    public static String getPasswordName() {
        return getBundle().getString(USER_PASSWORD_PROPERTY);
    }

    public static String getErrorLoginMessage() {
        return getBundle().getString(ERROR_LOGIN_PROPERTY);
    }

    public static String getErrorInternalMessage() {
        return getBundle().getString(ERROR_INTERNAL_PROPERTY);
    }

    public static String getLoginMessage() {
        return getBundle().getString(MESSAGE_LOGIN_PROPERTY);
    }

    public static String getRetryMessage() {
        return getBundle().getString(MESSAGE_RETRY_PROPERTY);
    }

    public static String getAuthenticationType() {
        return getBundle().getString("authentication.type");
    }

    public static String getMenuOpenName() {
        return getBundle().getString(MENU_OPEN_PROPERTY);
    }

    public static String getMenuExitName() {
        return getBundle().getString(MENU_EXIT_PROPERTY);
    }

    public static String getLoginRelativeUrl() {
        return getBundle().getString("login.relative.url");
    }

    public static String getHttpServerUrl() {
        return getBundle().getString("server.url");
    }

    public static boolean getShowTray() {
        try {
            String value = getBundle().getString(SHOW_TRAY);
            if (value == null) {
                return false;
            }
            if (value.compareToIgnoreCase("true") == 0) {
                return true;
            }
            return false;
        } catch (MissingResourceException e) {
            return true;
        }
    }

    public static boolean isLoginSilently() {
        try {
            return Boolean.parseBoolean(getBundle().getString("userinput.login.silently"));
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDefaultLogin() {
        try {
            return getBundle().getString("userinput.default.login");
        } catch (MissingResourceException e) {
            return "";
        }
    }

    public static String getDefaultPassword() {
        try {
            return getBundle().getString("userinput.default.password");
        } catch (MissingResourceException e) {
            return "";
        }
    }

    public static int getCheckTasksTimeout() {
        int timeout = 300;
        try {
            timeout = Integer.parseInt(getBundle().getString(CHECK_TASKS_TIMEOUT_PROPERTY));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse " + CHECK_TASKS_TIMEOUT_PROPERTY + " property", e);
        }
        return timeout * 1000;
    }

    public static int getAutoClosePopupTimeout() {
        int timeout = 6;
        try {
            timeout = Integer.parseInt(getBundle().getString(POPUP_AUTOCLOSE_TIMEOUT_PROPERTY));
        } catch (Exception e) {
            log.warn("Failed to parse " + POPUP_AUTOCLOSE_TIMEOUT_PROPERTY + " property", e);
        }
        return timeout * 1000;
    }

    public static boolean isSoundsEnabled() {
        try {
            return Boolean.parseBoolean(getBundle().getString(SOUNDS_ENABLED_PROPERTY));
        } catch (Exception e) {
            return false;
        }
    }

    public static String getOnNewTaskTriggerCommand() {
        try {
            return getBundle().getString(ON_NEW_TASK_TRIGGER_COMMAND_PROPERTY);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getUnreadTasksNotificationTimeout() {
        try {
            return 1000 * Integer.parseInt(getBundle().getString(UNREAD_TASKS_NOTIFICATION_TIMEOUT_PROPERTY));
        } catch (Exception e) {
            return 0;
        }
    }

}
