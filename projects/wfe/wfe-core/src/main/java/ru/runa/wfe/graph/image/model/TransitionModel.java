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
package ru.runa.wfe.graph.image.model;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class TransitionModel {
    private String name;

    private NodeModel nodeFrom;

    private NodeModel nodeTo;

    private final List<BendpointModel> bendpoints = Lists.newArrayList();
    private int actionsCount = 0;

    public int getActionsCount() {
        return actionsCount;
    }

    public void setActionsCount(int actionsCount) {
        this.actionsCount = actionsCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeModel getNodeFrom() {
        return nodeFrom;
    }

    public void setNodeFrom(NodeModel stateFrom) {
        nodeFrom = stateFrom;
    }

    public NodeModel getNodeTo() {
        return nodeTo;
    }

    public void setNodeTo(NodeModel stateTo) {
        nodeTo = stateTo;
    }

    public void addBendpoint(BendpointModel bendpointModel) {
        bendpoints.add(bendpointModel);
    }

    public boolean hasBendpoints() {
        return bendpoints.size() > 0;
    }

    public List<BendpointModel> getBendpoints() {
        return bendpoints;
    }

    @Override
    public String toString() {
        String repr = name != null ? name : "";
        return repr + "[" + nodeFrom + "|" + nodeTo + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeFrom, nodeTo);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TransitionModel)) {
            return false;
        }
        TransitionModel target = (TransitionModel) obj;
        return Objects.equal(nodeFrom, target.getNodeFrom()) && Objects.equal(nodeTo, target.getNodeTo());
    }
}
