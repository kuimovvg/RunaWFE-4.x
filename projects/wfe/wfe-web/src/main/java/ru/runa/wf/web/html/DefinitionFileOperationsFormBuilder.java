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

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;

/**
 * Base tag for all file operations on definition ( Created on 12.10.2004
 * 
 */
public class DefinitionFileOperationsFormBuilder {

    public static void displayTable(Form form, TD tdFormElement) {
        Input input = new Input(Input.FILE, FileForm.FILE_INPUT_NAME);
        input.setClass(Resources.CLASS_REQUIRED);
        tdFormElement.addElement(input);
        form.setEncType(Form.ENC_UPLOAD);
    }
}
