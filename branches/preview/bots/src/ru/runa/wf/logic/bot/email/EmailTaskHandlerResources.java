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
package ru.runa.wf.logic.bot.email;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 30.06.2005
 * @deprecated 
 */
public class EmailTaskHandlerResources extends ResourceCommons {

    private final String propertyBundleName;

    public EmailTaskHandlerResources(String propertyBundleName) {
        super(propertyBundleName);
        this.propertyBundleName = propertyBundleName;
    }

    private static final String SMTP_SERVER = "smtp.server";

    private static final String SMTP_SERVER_PORT = "smtp.server.port";

    private static final String SMTP_USER = "smtp.user";

    private static final String SMTP_PASSWORD = "smtp.password";

    private static final String CONTENT_TYPE = "content.type";

    private static final String FROM = "from";

    private static final String SUBJECT = "subject";

    private static final String REPLY_TO = "reply.to";

    private static final String TO = "to";

    private static final String CC = "cc";

    private static final String BCC = "bcc";

    public String getSMTPServerAddress() {
        return readPropertyIfExist(SMTP_SERVER, propertyBundleName);
    }

    public String getFROM() {
        return readPropertyIfExist(FROM, propertyBundleName);
    }

    public String getREPLAY_TO() {
        return readPropertyIfExist(REPLY_TO, propertyBundleName);
    }

    public String getTO() {
        return readPropertyIfExist(TO, propertyBundleName);
    }

    public String getCC() {
        return readPropertyIfExist(CC, propertyBundleName);
    }

    public String getBCC() {
        return readPropertyIfExist(BCC, propertyBundleName);
    }

    public String getSUBJECT() {
        return readPropertyIfExist(SUBJECT, propertyBundleName);
    }

    public String getSmtpUsername() {
        return readPropertyIfExist(SMTP_USER, propertyBundleName);
    }

    public String getSmtpPassword() {
        return readPropertyIfExist(SMTP_PASSWORD, propertyBundleName);
    }

    public String getContentType() {
        String contentType = readPropertyIfExist(CONTENT_TYPE, propertyBundleName);
        return contentType != null ? contentType : "text/plain";
    }

    public String getSMTPServerPort() {
        String port = readPropertyIfExist(SMTP_SERVER_PORT, propertyBundleName);
        return port != null ? port : "25";
    }
}
