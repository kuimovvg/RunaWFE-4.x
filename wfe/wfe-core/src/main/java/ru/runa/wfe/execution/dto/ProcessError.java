package ru.runa.wfe.execution.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessError implements Serializable {
    private String nodeId;
    private String taskName;
    private BotTask botTask;
    private Date occuredDate = new Date();
    private Throwable throwable;

    public ProcessError() {
    }

    public ProcessError(String nodeId, String taskName, BotTask botTask, Throwable throwable) {
        this.nodeId = nodeId;
        this.taskName = taskName;
        this.botTask = botTask;
        this.throwable = throwable;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getTaskName() {
        return taskName;
    }

    public BotTask getBotTask() {
        return botTask;
    }

    public Date getOccuredDate() {
        return occuredDate;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessError) {
            ProcessError bti = (ProcessError) obj;
            return Objects.equal(nodeId, bti.nodeId);
        }
        return super.equals(obj);
    }
}