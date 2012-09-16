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
package ru.runa.af;

import ru.runa.commons.ResourceCommons;

/**
 * This Resource class is used by both logic and dao layers in future if we devide logic from dao ResourcesDAO will be introduced Created on 25.03.2005
 *
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public final class CoreResources extends ResourceCommons {
    private static final String PROPERTIES = "af_core";

    private CoreResources() {
        super(PROPERTIES);
    }

    private static final String DEFAULT_PROFILE_STRATEGY_CLASS_NAME = "ru.runa.af.service.default.profile.strategy.class.name";

    private static final String CLASS_PRESENTATION_FINDER_CLASS_NAME = "ru.runa.af.class.presentation.finder.class.name";

    private static final String INITIAL_DISPLAY_FIELDS_IS_EMPTY = "ru.runa.af.presentation.display_fields.is_empty";

    public static String getDefaultProfileStrategyClassName() {
        return readProperty(DEFAULT_PROFILE_STRATEGY_CLASS_NAME, PROPERTIES);
    }

    public static String getClassPresentaionFinderClassName() {
        return readProperty(CLASS_PRESENTATION_FINDER_CLASS_NAME, PROPERTIES);
    }

    public static boolean isInitialDisplayFieldsEmpty() {
        return readPropertyIfExist(INITIAL_DISPLAY_FIELDS_IS_EMPTY, PROPERTIES, "false").equalsIgnoreCase("true");
    }
}
