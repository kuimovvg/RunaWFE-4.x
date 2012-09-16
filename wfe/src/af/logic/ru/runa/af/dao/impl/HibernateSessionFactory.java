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
package ru.runa.af.dao.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ru.runa.InternalApplicationException;

public class HibernateSessionFactory {

    public static Session openSession() {
        try {
            return ru.runa.commons.hibernate.HibernateSessionFactory.openSession();
        } catch (HibernateException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static Session openSession(boolean isReplicate) {
            return ru.runa.commons.hibernate.HibernateSessionFactory.openSession();
    }

    public static Session openArchiveSession() {
        return null;
//        try {
//            return ru.runa.commons.hibernate.HibernateSessionFactory.openArchiveSession(true);
//        } catch (HibernateException e) {
//            throw new InternalApplicationException(e);
//        }
    }

    public static Session openArchiveSession(boolean isReplicate) {
//        try {
//            return ru.runa.commons.hibernate.HibernateSessionFactory.openArchiveSession(isReplicate);
//        } catch (HibernateException e) {
//            throw new InternalApplicationException(e);
//        }
        return null;
    }

    public static Session getSession() {
        return ru.runa.commons.hibernate.HibernateSessionFactory.getSession();
    }

    public static Session getArchiveSession() {
        //return ru.runa.commons.hibernate.HibernateSessionFactory.getArchiveSession();
        return null;
    }

    public static void closeSession(boolean commitTransaction) {
    }

    public static void closeArchiveSession(boolean commitTransaction) {
    }
}
