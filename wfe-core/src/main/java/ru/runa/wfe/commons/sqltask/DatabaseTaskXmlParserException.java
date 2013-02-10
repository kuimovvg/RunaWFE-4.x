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
package ru.runa.wfe.commons.sqltask;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that problem occured Created on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DatabaseTaskXmlParserException extends InternalApplicationException {

    private static final long serialVersionUID = 3045717597605266465L;

    public DatabaseTaskXmlParserException() {
        super();
    }

    public DatabaseTaskXmlParserException(String message) {
        super(message);
    }

    public DatabaseTaskXmlParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseTaskXmlParserException(Throwable cause) {
        super(cause);
    }
}
