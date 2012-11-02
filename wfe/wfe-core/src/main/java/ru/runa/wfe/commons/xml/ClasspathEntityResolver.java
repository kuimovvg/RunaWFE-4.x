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
package ru.runa.wfe.commons.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Created on 25.07.2005
 * 
 */
public class ClasspathEntityResolver implements EntityResolver {
    private static ClasspathEntityResolver INSTANCE = new ClasspathEntityResolver();

    private ClasspathEntityResolver() {
        //prevents instantiation of singleton class
    }

    public static final EntityResolver getInstance() {
        return INSTANCE;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        int index = systemId.lastIndexOf("/");
        if (index != -1) {
            return new InputSource(getClass().getResourceAsStream(systemId.substring(index)));
        }
        return null;
    }
}
