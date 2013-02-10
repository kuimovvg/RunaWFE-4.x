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

import ru.runa.common.WebResources;
import ru.runa.wf.web.StartFormBuilder;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.WfException;

/**
 * Created on 17.11.2004
 * 
 */
public class FormBuilderFactory {
    /**
     * Returns implementation of TaskFormBuilder defined in wf.web.property file
     * 
     * @param formFileType
     *            type of from taken from forms.xml
     * @return
     */
    public static TaskFormBuilder createTaskFormBuilder(String formFileType) {
        String taskFormBuilderClassName = WebResources.getTaskFormBuilderClassName(formFileType);
        if (taskFormBuilderClassName == null) {
            throw new WfException("No task form builder registered for form type '" + formFileType + "'");
        }
        return ru.runa.wfe.commons.ClassLoaderUtil.instantiate(taskFormBuilderClassName);
    }

    /**
     * Returns implementation of StartFormBuilder defined in wf.web.property
     * file
     * 
     * @param formFileType
     *            type of from taken from forms.xml
     * @return
     */
    public static StartFormBuilder createStartFormBuilder(String formFileType) {
        String taskFormBuilderClassName = WebResources.getStartFormBuilderClassName(formFileType);
        if (taskFormBuilderClassName == null) {
            throw new WfException("No start task form builder registered for form type '" + formFileType + "'");
        }
        return ru.runa.wfe.commons.ClassLoaderUtil.instantiate(taskFormBuilderClassName);
    }

}
