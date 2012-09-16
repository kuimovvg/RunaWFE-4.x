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

package ru.runa.commons;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * Creates instances for given class name using default constractor and puts it to cache. 
 * 
 * Created on Apr 6, 2006
 *
 */
public class ReflectionObjectInstanceCache {
    private final Map<String, Object> map = new HashMap<String, Object>();

    /**
     * Clears pool.
     */
    public void clear() {
        map.clear();
    }

    /**
     * 
     * @param className
     * @return instance of object for given class name
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public Object getInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Object result = map.get(className);
        if (result == null) {
            return getInstance(Class.forName(className));
        }
        return result;
    }

    /**
     * 
     * @param clazz
     * @return instance of object for given class
     * @throws ClassNotFoundException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public Object getInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object result = map.get(clazz.getName());
        if (result == null) {
            result = clazz.newInstance();
            ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(result);
            map.put(clazz.getName(), result);
        }
        return result;
    }

}
