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

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class NodeModel {
    public static final int STATE = 0;
    public static final int DECISION = 1;
    public static final int FORK_JOIN = 2;
    public static final int START_STATE = 3;
    public static final int END_STATE = 4;
    public static final int STATE_WITH_TIMER = 5;
    public static final int PROCESS_STATE = 6;
    public static final int WAIT_STATE = 7;
    public static final int ACTION_NODE = 8;
    public static final int MULTI_PROCESS_STATE = 9;
    public static final int SEND_MESSAGE = 10;
    public static final int RECEIVE_MESSAGE = 11;

    private String name;
    private String swimlane;
    private int type;
    private int x;
    private int y;
    private int width;
    private int height;
    private int actionsCount = 0;
    private boolean minimizedView = false;

    private final HashMap<String, TransitionModel> transitions = Maps.newHashMap();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
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
