package ru.runa;


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
/**
 * Signals that something wrong happen within application.
 *  Created on 10.08.2004
 */
public class InternalApplicationException extends RuntimeException {

    private static final long serialVersionUID = -5831083411488819303L;

    public InternalApplicationException() {
        super();
    }

    public InternalApplicationException(String message) {
        super(message);
    }

    public InternalApplicationException(Throwable cause) {
        super(cause);
    }

    public InternalApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
