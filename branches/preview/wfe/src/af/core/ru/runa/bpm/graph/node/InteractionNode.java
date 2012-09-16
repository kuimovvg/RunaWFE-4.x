package ru.runa.bpm.graph.node;

import java.util.List;

import ru.runa.InternalApplicationException;
import ru.runa.bpm.graph.def.Node;
import ru.runa.bpm.taskmgmt.def.Task;

import com.google.common.collect.Lists;

public class InteractionNode extends Node {
    private static final long serialVersionUID = 1L;
    protected List<Task> tasks = Lists.newArrayList();

    public void addTask(Task task) {
        tasks.add(task);
        task.setNode(this);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Task getFirstTaskNotNull() {
        if (tasks.size() > 0) {
            return tasks.get(0);
        }
        throw new InternalApplicationException("There are no tasks in " + this);
    }

}
