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

import org.apache.struts.action.ActionForm;

/**
 * @author: petrmikheev
 * 
 * @struts:form name = "botForm"
 */
public class BotForm extends ActionForm {
    private static final long serialVersionUID = -5742270733607674118L;

    public static final String USER_NAME = "wfeUser";
    public static final String PASSWORD = "wfePassword";
    public static final String BOT_STATION_ID = "botStationID";
    public static final String BOT_ID = "botID";
    public static final String BOT_TIMEOUT = "botTimeout";

    private String wfeUser;
    private String wfePassword;
    private Long botID;
    private Long botStationID;
    private Long botTimeout;

    public String getWfeUser() {
        return wfeUser;
    }

    public void setWfeUser(String botName) {
        wfeUser = botName;
    }

    public String getWfePassword() {
        return wfePassword;
    }

    public void setWfePassword(String botPassword) {
        wfePassword = botPassword;
    }

    public Long getBotStationID() {
        return botStationID;
    }

    public void setBotStationID(Long botStationID) {
        this.botStationID = botStationID;
    }

    public Long getBotID() {
        return botID;
    }

    public void setBotID(Long botID) {
        this.botID = botID;
    }

    public Long getBotTimeout() {
        return botTimeout;
    }

    public void setBotTimeout(Long botTimeout) {
        this.botTimeout = botTimeout;
    }
}
