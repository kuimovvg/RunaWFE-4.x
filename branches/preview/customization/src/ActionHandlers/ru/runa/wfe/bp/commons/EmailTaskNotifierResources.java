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
package ru.runa.wfe.bp.commons;

import java.util.ArrayList;
import java.util.List;

import ru.runa.commons.email.EmailResources;

/**
 * 
 * Created on 28.10.2008
 * 
 * @author A. Shautsou
 * @version 1.0 Initial version
 */
public class EmailTaskNotifierResources extends EmailResources {
    private static final String SMTP_SENDNOTIFICATION = "smtp.sendNotification";

    private static final char COMMA = ',';

    private final List<String> tos;

    /**
     * Constructor
     * 
     * @param propertyBundleName
     */
    public EmailTaskNotifierResources(String propertyBundleName) {
        super(propertyBundleName);
        tos = new ArrayList<String>();
    }

    public boolean isSmtpSendNotification() {
        String sendNotification = readPropertyIfExist(SMTP_SENDNOTIFICATION);
        boolean result = false;
        if (sendNotification != null && Boolean.TRUE.toString().equalsIgnoreCase(sendNotification)) {
            result = true;
        }
        return result;
    }

    public String getTO() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < tos.size(); i++) {
            builder.append(tos.get(i));
            if (i < tos.size() - 1) {
                builder.append(COMMA);
            }
        }
        return builder.toString();
    }

    public void addTo(String email) {
        tos.add(email);
    }

    public void addTos(List<String> emails) {
        tos.addAll(emails);
    }
}
