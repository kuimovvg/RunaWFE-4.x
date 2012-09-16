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
package ru.runa.wf.web;

import java.lang.reflect.InvocationTargetException;

import ru.runa.commons.ResourceCommons;

/**
 * Created on 5.07.2007
 *
 * @author Konstantinov A. (mailto:kana@ptc.ru)
 */
public class MainPageResources extends ResourceCommons {
    private static final String BUNDLE_NAME = "main_page";

    private static final String ADDITIONAL_LINKS = "ru.runa.web.additional_links";

    private MainPageResources() {
        super(BUNDLE_NAME);
    }

    public static String getAdditionalLinks() {
        readProperty(ADDITIONAL_LINKS, BUNDLE_NAME);
        try {
            Class getter = Class.forName(readProperty(ADDITIONAL_LINKS, BUNDLE_NAME));
            return getter.getDeclaredMethod("getAdditionalLinks", (Class[]) null).invoke(getter, (Object[]) null).toString();
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }
        return "";
    }
}
