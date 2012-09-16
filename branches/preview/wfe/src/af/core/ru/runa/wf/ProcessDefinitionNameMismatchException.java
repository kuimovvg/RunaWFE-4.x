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
 * Signals that definition contains name differes with name of definition you want to redeploy.
 * Created on 11.10.2004
 */
public class ProcessDefinitionNameMismatchException extends Exception {
    private static final long serialVersionUID = -2137340395617831247L;
    private final String givenProcessDefinitionName;
    private final String deployedProcessDefinitionName;

    public ProcessDefinitionNameMismatchException(String message, String givenProcessDefinitionName, String deployedProcessDefinitionName) {
        super(message);
        this.givenProcessDefinitionName = givenProcessDefinitionName;
        this.deployedProcessDefinitionName = deployedProcessDefinitionName;
    }

    public String getDeployedProcessDefinitionName() {
        return deployedProcessDefinitionName;
    }

    public String getGivenProcessDefinitionName() {
        return givenProcessDefinitionName;
    }
}
