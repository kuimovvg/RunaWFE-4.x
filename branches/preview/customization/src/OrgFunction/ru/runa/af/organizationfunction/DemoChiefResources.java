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
package ru.runa.af.organizationfunction;

import java.util.Enumeration;

import ru.runa.commons.ResourceCommons;

/**
 * Created 19.05.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
class DemoChiefResources extends ResourceCommons {

    private static final String RESOURCE_NAME = "orgfunction.demo_chief";

    private DemoChiefResources() {
        super(RESOURCE_NAME);
    }

    public static Enumeration<String> getPatterns() {
        return getKeys(RESOURCE_NAME);
    }

    public static String getChiefName(String pattern) {
        return readProperty(pattern, RESOURCE_NAME);
    }
}
