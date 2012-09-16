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
package ru.runa.commons.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ru.runa.commons.ApplicationContextFactory;

/**
 * Created on 16.12.2004
 * TODO: remove this
 */
public final class HibernateSessionFactory {

//    public synchronized static SessionFactory getSessionFactory(String configurationName, boolean isReplicate) throws HibernateException {
//        SessionFactory factory = factoryMap.get(configurationName + isReplicate);
//        if (factory == null) {
//            AnnotationConfiguration configuration = getConfiguration(configurationName, isReplicate);
//            Interceptor interceptor = null;
//            try {
//                interceptor = (Interceptor) Loader.loadObject("ru.runa.wf.WFRunaHibernateInterceptor", null);
//            } catch (Exception e) {
//                interceptor = new RunaHibernateIntercepter();
//            }
//            configuration.setInterceptor(interceptor);
//            factory = configuration.buildSessionFactory();
//            if (!interceptor.getClass().equals(RunaHibernateIntercepter.class)) {
//                factoryMap.put(configurationName + isReplicate, factory);
//            }
//        }
//        return factory;
//    }

//
//    public synchronized static AnnotationConfiguration getConfiguration(String configurationName, boolean isReplicate) throws HibernateException {
//        AnnotationConfiguration configuration = configurationMap.get(configurationName + isReplicate);
//        if (configuration == null) {
//            configuration = (AnnotationConfiguration) new AnnotationConfiguration().configure(configurationName);
//            configurationMap.put(configurationName + isReplicate, configuration);
//            if (isReplicate) {
//                configuration.buildMappings();
//                Iterator<PersistentClass> iter = configuration.getClassMappings();
//                while (iter.hasNext()) {
//                    PersistentClass persistentClass = iter.next();
//                    KeyValue val = persistentClass.getIdentifier();
//                    if (val instanceof SimpleValue) {
//                        SimpleValue vv = (SimpleValue) val;
//                        vv.setIdentifierGeneratorStrategy("increment");
//                        vv.setNullValue("undefined");
//                    }
//                    persistentClass.setLazy(false);
//                }
//            }
//        }
//        return configuration;
//    }

    public static Session openSession() {
        return ApplicationContextFactory.getCurrentSession();
    }

    public synchronized static Session openSession(String configurationName, boolean isReplicate) throws HibernateException {
        return openSession();
    }

    public static Session getSession() {
        return openSession();
    }

    public static void closeSession(boolean commitTransaction) throws HibernateException {
    }

}
