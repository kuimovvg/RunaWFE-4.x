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
package ru.runa.wfe.definition;

import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.DefaultDBSource;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.SubstringDBSource;
import ru.runa.wfe.presentation.filter.AnywhereStringFilterCriteria;

/**
 * Class presentation for process definition.
 * 
 * @author Dofs
 * @since 4.0
 */
public class DefinitionClassPresentation extends ClassPresentation {
    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_NAME = "batch_presentation.process_definition.name";
    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_DESCRIPTION = "batch_presentation.process_definition.description";
    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_VERSION = "batch_presentation.process_definition.version";
    public static final String PROCESS_DEFINITION_BATCH_PRESENTATION_TYPE = "batch_presentation.process_definition.process_type";

    private static final ClassPresentation INSTANCE = new DefinitionClassPresentation();

    private DefinitionClassPresentation() {
        super(Deployment.class, classNameSQL + ".version=(select max(temp.version) from " + Deployment.class.getName() + " as temp where "
                + classNameSQL + ".name=temp.name)", false, new FieldDescriptor[] {
                // display name field type DB source isSort filter mode
                // get value/show in web getter parameters
                new FieldDescriptor(PROCESS_DEFINITION_BATCH_PRESENTATION_NAME, String.class.getName(),
                        new DefaultDBSource(Deployment.class, "name"), true, FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder",
                        new Object[] { DefinitionPermission.START_PROCESS, "name" }),
                new FieldDescriptor(PROCESS_DEFINITION_BATCH_PRESENTATION_DESCRIPTION, String.class.getName(), new SubstringDBSource(
                        Deployment.class, "description"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.DescriptionProcessTDBuilder",
                        new Object[] {}),
                new FieldDescriptor(PROCESS_DEFINITION_BATCH_PRESENTATION_TYPE, AnywhereStringFilterCriteria.class.getName(), new DefaultDBSource(
                        Deployment.class, "category"), true, FieldFilterMode.DATABASE, "ru.runa.wf.web.html.TypeProcessTDBuilder", new Object[] {},
                        true) });
    }

    public static final ClassPresentation getInstance() {
        return INSTANCE;
    }
}
