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
package ru.runa.bpm.context.def;

import java.io.Serializable;

/**
 * specifies access to a variable. Variable access is used in 3 situations: 1)
 * process-state 2) script 3) task controllers
 */
public class VariableMapping implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ACCESS_SELECTOR = "selector";
    public static final String ACCESS_MULTIINSTANCELINK = "multiinstancelink";
    public static final String ACCESS_REQUIRED = "required";
    public static final String ACCESS_WRITE = "write";
    public static final String ACCESS_READ = "read";

    protected String name;
    protected String mappedName;
    protected String access;

    public VariableMapping(String variableName, String mappedName, String access) {
        this.name = variableName;
        this.mappedName = mappedName;
        this.access = access;
    }

    public boolean isReadable() {
        return hasAccess(ACCESS_READ);
    }

    public boolean isWritable() {
        return hasAccess(ACCESS_WRITE);
    }

    public boolean isRequired() {
        return hasAccess(ACCESS_REQUIRED);
    }

    public boolean isMultiinstanceLink() {
        return hasAccess(ACCESS_MULTIINSTANCELINK);
    }

    public boolean isPropertySelector() {
        return hasAccess(ACCESS_SELECTOR);
    }

    /**
     * verifies if the given accessLiteral is included in the access text.
     */
    public boolean hasAccess(String accessLiteral) {
        if (access == null) {
            return false;
        }
        return access.indexOf(accessLiteral) != -1;
    }

    public String getAccess() {
        return access;
    }

    /**
     * the mapped name. The mappedName defaults to the variableName in case no
     * mapped name is specified.
     */
    public String getMappedName() {
        return mappedName;
    }

    public String getName() {
        return name;
    }

}
