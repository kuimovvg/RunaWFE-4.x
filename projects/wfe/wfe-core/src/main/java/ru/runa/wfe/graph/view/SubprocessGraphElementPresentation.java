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

/**
 * Represents Subprocess element on process graph.
 */
public class SubprocessGraphElementPresentation extends BaseGraphElementPresentation {

    private static final long serialVersionUID = 1L;

    /**
     * Forked subprocess identity.
     */
    private Long subprocessId;

    /**
     * Flag, equals true, if subprocess is readable by current user; false
     * otherwise.
     */
    private boolean readPermission;

    /**
     * Name of subprocess.
     */
    private final String subprocessName;

    /**
     * @param subprocessName
     *            Name of subprocess.
     */
    public SubprocessGraphElementPresentation(String subprocessName) {
        this.subprocessName = subprocessName;
    }

    @Override
    public void visit(GraphElementPresentationVisitor visitor) {
        visitor.onSubprocess(this);
    }

    /**
     * Forked subprocess identity.
     */
    public Long getSubprocessId() {
        return subprocessId;
    }

    /**
     * Set forked subprocess identity.
     */
    public void setSubprocessId(Long subprocessId) {
        this.subprocessId = subprocessId;
    }

    /**
     * Flag, equals true, if subprocess is readable by current user; false
     * otherwise.
     */
    public boolean isReadPermission() {
        return readPermission;
    }

    /**
     * Set flag, equals true, if subprocess is readable by current user; false
     * otherwise.
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
