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
package ru.runa.wf.web.forms.format;

import ru.runa.commons.format.WebFormat;

/**
 * Format object that converts given object to string.
 * 
 * Created on 24.11.2006
 * 
 */
public class StringFormat implements WebFormat {

    private static final long serialVersionUID = 5682764580473779270L;

    @Override
    public Object parse(String[] source) {
        return source[0];
    }

    @Override
    public String format(Object obj) {
        return obj.toString();
    }
}
