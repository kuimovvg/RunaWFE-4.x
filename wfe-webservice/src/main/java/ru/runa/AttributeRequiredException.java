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
package ru.runa;

public class AttributeRequiredException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String object;
    private final String field;

    public AttributeRequiredException(String object, String field) {
        super("The field " + field + " doesn't specified in object " + object);
        this.object = object;
        this.field = field;
    }

    public String getObject() {
        return object;
    }

    public String getField() {
        return field;
    }
}
