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
package ru.runa.wfe.validation.impl;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.validation.FieldValidator;

public class RequiredFieldValidator extends FieldValidator {
    private static final boolean oracleDatabase = ApplicationContextFactory.getDBType() == DBType.ORACLE;

    @Override
    public void validate() {
        Object value = getFieldValue();
        if (value == null) {
            addError();
            return;
        }
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() == 0) {
                addError();
            } else if (oracleDatabase && " ".equals(s)) {
                addError();
            }
        }
        if (value instanceof List) {
            List s = (List) value;
            if (s.size() == 0) {
                addError();
            }
        }
        if (value instanceof Map) {
            Map s = (Map) value;
            if (s.size() == 0) {
                addError();
            }
        }
    }
}
