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
package ru.runa.af.presentation;

import ru.runa.af.Permission;
import ru.runa.af.Relation;

public class RelationGroupClassPresentation extends ClassPresentation {

    private static final long serialVersionUID = 1L;

    private static final ClassPresentation INSTANCE = new RelationGroupClassPresentation();

    private static final String RELATION_GROUP_NAME = "batch_presentation.relation.name";
    private static final String RELATION_GROUP_DESCRIPTION = "batch_presentation.relation.description";

    public RelationGroupClassPresentation() {
        super(Relation.class, "", false, new FieldDescriptor[] {
                //                           display name                field type                              DB source                 isSort         filter mode                       get value/show in web                  getter parameters
                new FieldDescriptor(RELATION_GROUP_NAME, String.class.getName(), new DefaultDBSource(Relation.class, "name"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "name" }),
                new FieldDescriptor(RELATION_GROUP_DESCRIPTION, String.class.getName(), new DefaultDBSource(Relation.class, "description"), true,
                        FieldFilterMode.DATABASE, "ru.runa.common.web.html.PropertyTDBuilder", new Object[] { new Permission(), "description" }) });
    }

    public static ClassPresentation getInstance() {
        return INSTANCE;
    }
}
