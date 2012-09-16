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
package ru.runa.commons.email;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 30.06.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class EmailResources extends ResourceCommons {

    public EmailResources(String propertyBundleName) {
        super(propertyBundleName);
    }

    public EmailResources(byte[] propertyBundleName) {
        super(propertyBundleName);
    }

    private static final String SMTP_SERVER = "smtp.server";

    private static final String SMTP_SERVER_PORT = "smtp.server.port";

    private static final String SMTP_USER = "smtp.user";

    private static final String SMTP_PASSWORD = "smtp.password";

    private static final String CONTENT_TYPE = "content.type";

    private static final String FROM = "from";

    private static final String SUBJECT = "subject";

    private static final String REPLY_TO = "reply.to";

    protected static final String TO = "to";

    private static final String CC = "cc";

    private static final String BCC = "bcc";

    private static final String authReq = "authReq";

    private static final String useSSL = "useSSL";

    private static final String WFUser = "WFUser";
    private static final String WFUserPass = "WFUserPass";

    private static final String EmailSenderClass = "sender.class";

    private static final String SendEmailNotify = "smtp.SendNotifiction";

    private static final String EmailTemplateFile = "email.template";

    public String getSMTPServerAddress() {
        return readPropertyIfExist(SMTP_SERVER);
    }

    public String getFROM() {
        return readPropertyIfExist(FROM);
    }

    public String getREPLAY_TO() {
        return readPropertyIfExist(REPLY_TO);
    }

    public String getTO() {
        return readPropertyIfExist(TO);
    }

    public String getCC() {
        return readPropertyIfExist(CC);
    }

    public String getBCC() {
        return readPropertyIfExist(BCC);
    }

    public String getSUBJECT() {
        return readPropertyIfExist(SUBJECT);
    }

    public String getSmtpUsername() {
        return readPropertyIfExist(SMTP_USER);
    }

    public String getSmtpPassword() {
        return readPropertyIfExist(SMTP_PASSWORD);
    }

    public String getContentType() {
        String contentType = readPropertyIfExist(CONTENT_TYPE);
        return contentType != null ? contentType : "text/plain";
    }

    public String getSMTPServerPort() {
        String port = readPropertyIfExist(SMTP_SERVER_PORT);
        return port != null ? port : "25";
    }

    public boolean isAuthRequired() {
        String boolStr = readPropertyIfExist(authReq);
        if (boolStr == null) {
            return false;
        }
        if (boolStr.equalsIgnoreCase("true") || boolStr.equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }

    public boolean isUsingSSL() {
        String boolStr = readPropertyIfExist(useSSL);
        if (boolStr == null) {
            return false;
        }
        if (boolStr.equalsIgnoreCase("true") || boolStr.equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }

    public String getWFUser() {
        return readPropertyIfExist(WFUser);
    }

    public String getWFUserPass() {
        return readPropertyIfExist(WFUserPass);
    }

    public String getEmailSenderClassName() {
        String className = readPropertyIfExist(EmailSenderClass);
        if (className != null && className.length() != 0) {
            return className;
        }
        return "ru.runa.wf.email.EmailSenderImpl";
    }

    public boolean isEmailNotifySending() {
        String boolStr = readPropertyIfExist(SendEmailNotify);
        if (boolStr == null) {
            return false;
        }
        if (boolStr.equalsIgnoreCase("true") || boolStr.equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }

    public String getEmailTemplate() {
        return readPropertyIfExist(EmailTemplateFile);

    }
}
