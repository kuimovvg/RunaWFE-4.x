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
package ru.runa.wfe.bot;

import javax.security.auth.Subject;

/**
 * Created on 03.03.2005
 * 
 */
public abstract class BotRunner {
    private final Subject subject;

    public BotRunner(Subject subject) {
        this.subject = subject;
    }

    public abstract void execute() throws Exception;

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "class: " + getClass().getName() + " \n  subject: " + subject;
    }
}
