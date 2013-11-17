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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represents multiple instance graph element.
 */
public class MultiinstanceGraphElementPresentation extends SubprocessGraphElementPresentation {
    private static final long serialVersionUID = 1L;

    /**
     * Identities of subprocesses, forked by this graph element. Contains
     * subprocess definition id if used in definition page.
     */
    private final List<Long> subprocessIds = Lists.newArrayList();

    @Override
    public void visit(GraphElementPresentationVisitor visitor) {
        visitor.onMultiSubprocess(this);
    }

    /**
     * Add process id to forked subprocesses list.
     * 
     * @param id
     *            Process identity.
     */
    public void addSubprocessId(Long id) {
        subprocessIds.add(id);
    }

    /**
     * Identities of subprocesses, forked by this graph element.
     */
    public List<Long> getSubprocessIds() {
        return subprocessIds;
    }

}