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
package ru.runa.wf;

/**
 * Signals that process definition doesn't format is bad (process definition format must be .jar in UTF8 encoding).
 * Created on 14.12.2005
 * 
 */
public class ProcessDefinitionXMLFormatException extends ProcessDefinitionArchiveException {
    private static final long serialVersionUID = 1L;

    public ProcessDefinitionXMLFormatException(String message) {
        super(message);
    }

    public ProcessDefinitionXMLFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
