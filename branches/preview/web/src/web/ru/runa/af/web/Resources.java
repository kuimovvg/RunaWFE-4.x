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
package ru.runa.af.web;

/**
 * Created on 16.08.2004
 */
public class Resources {

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
    public static final int VALIDATOR_NAME_LENGTH = 255;
    public static final int VALIDATOR_DESCRIPTION_LENGTH = 255;
    public static final int VALIDATOR_FULL_NAME_LENGTH = 255;
    public static final int VALIDATOR_PASSWORD_LENGTH = 255;
    public static final int VALIDATOR_EMAIL = 255;
}
