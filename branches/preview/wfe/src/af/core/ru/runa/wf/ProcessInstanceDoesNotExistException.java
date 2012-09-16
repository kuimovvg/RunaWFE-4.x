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
 * Signals that instance does not exists in system.
 */
public class ProcessInstanceDoesNotExistException extends RuntimeException {

    private static final long serialVersionUID = -4878556882418093646L;
    private String name;

    public ProcessInstanceDoesNotExistException(String name) {
        super("Instance " + name + " does not exists.");
        this.name = name;
    }

    public ProcessInstanceDoesNotExistException(Long id) {
        this("with id = " + String.valueOf(id));
    }

    public String getName() {
        return name;
    }

}
