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

import javax.portlet.PortletSession;
import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;

import ru.runa.common.web.Commons;
import ru.runa.common.web.InvalidSessionException;

/**
 * Helper class for managing objects in HttpSession Created on 17.08.2004
 * 
 */
public class SubjectHttpSessionHelper {

    private static final String ACTOR_SUBJECT_ATTRIBUTE_NAME = Subject.class.getName();

    public static void addActorSubject(Subject subject, HttpSession session) {
        session.setAttribute(ACTOR_SUBJECT_ATTRIBUTE_NAME, subject);
    }

    public static void addActorSubject(Subject subject, PortletSession session) {
        session.setAttribute(ACTOR_SUBJECT_ATTRIBUTE_NAME, subject);
    }

    public static void removeActorSubject(HttpSession session) {
        session.removeAttribute(ACTOR_SUBJECT_ATTRIBUTE_NAME);
    }

    public static Subject getActorSubject(HttpSession session) {
        try {
            Subject subject = (Subject) Commons.getSessionAttribute(session, ACTOR_SUBJECT_ATTRIBUTE_NAME);
            if (subject == null) {
                throw new InvalidSessionException("Session does not contain subject.");
            }
            return subject;
        } catch (IllegalStateException e) {
            throw new InvalidSessionException("Session does not contain subject.");
        }
    }
}
