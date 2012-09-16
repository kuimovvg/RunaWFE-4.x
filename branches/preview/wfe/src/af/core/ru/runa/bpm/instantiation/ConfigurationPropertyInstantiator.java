/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.instantiation;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.InternalApplicationException;

public class ConfigurationPropertyInstantiator implements Instantiator {
    private static final Log log = LogFactory.getLog(ConfigurationPropertyInstantiator.class);

    private static final Class<?>[] parameterTypes = new Class[] { String.class };

    public <T extends Object> T instantiate(Class<T> clazz, String configuration) {
        try {
            T newInstance = clazz.newInstance();
            // set the configuration with the bean-style setter
            Method setter = clazz.getMethod("setConfiguration", parameterTypes);
            setter.setAccessible(true);
            if (configuration != null && configuration.trim().startsWith("<![CDATA[")) { // TODO remove in future
                configuration = configuration.trim();
                configuration = configuration.substring(9, configuration.length() - 3);
            }
            setter.invoke(newInstance, new Object[] { configuration });
            return newInstance;
        } catch (Exception e) {
            log.error("couldn't instantiate '" + clazz.getName() + "'", e);
            throw new InternalApplicationException(e);
        }
    }

}
