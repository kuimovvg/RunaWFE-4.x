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
package ru.runa.common.web.tag;

import java.util.Enumeration;
import java.util.Locale;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 04.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class TabHeaderResource extends ResourceCommons {

    public static final String PROPERTY_FILE = "tab";

    private TabHeaderResource() {
        super(PROPERTY_FILE);
    }

    /** Keys are used as forwad maps */
    public static Enumeration<String> getAllKeys(Locale locale) {
        return getKeys(PROPERTY_FILE, locale);
    }

    public static String getHeaderName(String name, Locale locale) {
        return readProperty(name, PROPERTY_FILE, locale);
    }
}
