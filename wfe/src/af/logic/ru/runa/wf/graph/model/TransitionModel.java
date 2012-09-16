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
package ru.runa.wf.graph.model;

import java.util.ArrayList;
import java.util.List;

public class TransitionModel {
    private String name;

    private NodeModel nodeFrom;

    private NodeModel nodeTo;

    private final List<BendpointModel> bendpoints = new ArrayList<BendpointModel>();
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
        int hash = nodeFrom.hashCode();
        hash += 37 * nodeTo.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        TransitionModel target = (TransitionModel) obj;
        return nodeFrom.equals(target.getNodeFrom()) && nodeTo.equals(target.getNodeTo());
    }
}
