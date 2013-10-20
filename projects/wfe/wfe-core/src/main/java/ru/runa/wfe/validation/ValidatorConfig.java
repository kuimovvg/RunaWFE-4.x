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
package ru.runa.wfe.validation;

import java.util.Map;

import com.google.common.base.Objects;

public class ValidatorConfig {
    private final String type;
    private final Map<String, String> params;
    private final String message;

    public ValidatorConfig(String type, Map<String, String> params, String message) {
        this.type = type;
        this.params = params;
        this.message = message;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("type", type).add("params", params).toString();
    }
}
