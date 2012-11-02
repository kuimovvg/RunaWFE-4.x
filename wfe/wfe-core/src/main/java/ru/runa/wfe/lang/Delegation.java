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
package ru.runa.wfe.lang;

import java.io.Serializable;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.handler.IConfigurable;

public class Delegation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String className;
    private String configuration;
    private transient Object instance;

    public Delegation() {
    }

    public Delegation(String className, String configuration) {
        this.className = className;
        this.configuration = configuration;
    }

    public <T extends IConfigurable> T getInstance() throws Exception {
        if (instance == null) {
            IConfigurable configurable = ApplicationContextFactory.createAutowiredBean(className);
            configurable.setConfiguration(configuration);
            instance = configurable;
        }
        return (T) instance;
    }

    public String getConfiguration() {
        return configuration;
    }

    @Override
    public String toString() {
        return className + "(" + configuration + ")";
    }

}
