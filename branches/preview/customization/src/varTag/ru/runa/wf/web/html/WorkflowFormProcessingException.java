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
package ru.runa.wf.web.html;

/**
 * Signals that form containing {@link VarTag} could not be shown because of problem in {@link VarTag}'s getHtml().
 * When this exception is thrown, message is shown instead of task form.
 * Created on 05.05.2005
 */

public class WorkflowFormProcessingException extends Exception {

    private static final long serialVersionUID = 5525612498561637233L;

    public WorkflowFormProcessingException() {
        super();
    }

    public WorkflowFormProcessingException(String message) {
        super(message);
    }

    public WorkflowFormProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkflowFormProcessingException(Throwable cause) {
        super(cause);
    }
}
