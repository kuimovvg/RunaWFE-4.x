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
package ru.runa.wfe.graph.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents multiple instance graph element.
 */
public class MultiinstanceGraphElementPresentation extends BaseGraphElementPresentation {

    private static final long serialVersionUID = 1L;

    /**
     * Identities of subprocesses, forked by this graph element. 
     * Contains subprocess definition id if used in definition page.
     */
    private final List<Long> ids = new ArrayList<Long>();

    /**
     * Flag, equals true, if subprocess is readable by current user; false otherwise.
     */
    private boolean readPermission;

    /**
     * Name of subprocess.
     */
    private final String subprocessName;

    /**
     * Creates multiple instance graph element.
     * @param name Graph element name. Can be null if not set.
     * @param subprocessName Name of subprocess.
     * @param graphConstraints Graph element position constraints.
     */
    public MultiinstanceGraphElementPresentation(String name, String subprocessName, int[] graphConstraints) {
        this(name, subprocessName, graphConstraints, null);
    }

    /**
     * Creates multiple instance graph element.
     * @param name Graph element name. Can be null if not set.
     * @param subprocessName Name of subprocess.
     * @param graphConstraints Graph element position constraints.
     * @param data Some additional data, assigned to graph element.
     */
    public MultiinstanceGraphElementPresentation(String name, String subprocessName, int[] graphConstraints, Object data) {
        super(name, graphConstraints, data);
        this.subprocessName = subprocessName;
    }

    @Override
    public void visit(GraphElementPresentationVisitor visitor) {
        visitor.onMultiinstance(this);
    }

    /**
     * Add process id to forked subprocesses list.
     * @param id Process identity.
     */
    public void addSubprocessId(Long id) {
        ids.add(id);
    }

    /**
     * Identities of subprocesses, forked by this graph element. 
     */
    public List<Long> getIds() {
        return ids;
    }

    /**
     * Flag, equals true, if subprocess is readable by current user; false otherwise.
     */
    public boolean isReadPermission() {
        return readPermission;
    }

    /**
     * Flag, equals true, if subprocess is readable by current user; false otherwise.
     */
    public void setReadPermission(boolean readPermission) {
        this.readPermission = readPermission;
    }

    /**
     * Name of subprocess.
     */
    public String getSubprocessName() {
        return subprocessName;
    }
}
