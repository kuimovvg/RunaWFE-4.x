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
package ru.runa.wfe.var.format;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class DoubleFormat implements VariableFormat<Number> {
    private final NumberFormat decimalFormat = new DecimalFormat("0.####################################");

    @Override
    public Number parse(String[] source) throws ParseException {
        Double result = null;
        if (source != null) {
            result = new Double(decimalFormat.parse(source[0]).doubleValue());
        }
        return result;
    }

    @Override
    public String format(Number obj) {
        return decimalFormat.format(obj);
    }
}
