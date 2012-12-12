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

import java.util.HashMap;

import ru.runa.wfe.lang.NodeType;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class NodeModel {
    private String nodeId;
    private String name;
    private String swimlane;
    private NodeType type;
    private int x;
    private int y;
    private int width;
    private int height;
    private int actionsCount = 0;
    private boolean minimizedView;
    private boolean withTimer;
    private boolean async;

    private final HashMap<String, TransitionModel> transitions = Maps.newHashMap();

    public boolean isWithTimer() {
        return withTimer;
    }

    public void setWithTimer(boolean withTimer) {
        this.withTimer = withTimer;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public int getActionsCount() {
        return actionsCount;
    }

    public void setActionsCount(int actionsCount) {
        this.actionsCount = actionsCount;
    }

    public String getSwimlane() {
        return swimlane;
    }

    public void setSwimlane(String swimlane) {
        this.swimlane = swimlane;
    }

    public int[] getConstraints() {
        return new int[] { x, y, x + width, y + height };
    }

    public void addTransition(TransitionModel transitionModel) {
        transitions.put(transitionModel.getName(), transitionModel);
    }

    public TransitionModel getTransition(String transitionName) {
        return transitions.get(transitionName);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isMinimizedView() {
        return minimizedView;
    }

    public void setMinimizedView(boolean minimizedView) {
        this.minimizedView = minimizedView;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NodeModel)) {
            return false;
        }
        return Objects.equal(name, ((NodeModel) obj).name);
    }
}
