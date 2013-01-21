package ru.runa.wfe.user;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;

import ru.runa.wfe.commons.hibernate.Proxies;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.task.Task;

/**
 * Used for assigning escalated tasks.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "E")
public class EscalationGroup extends TemporaryGroup {
    private static final long serialVersionUID = 1L;

    private Executor originalExecutor;
    private int escalationLevel;

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ESCALATION_EXECUTOR_ID")
    @ForeignKey(name = "FK_GROUP_ESCALATION_EXECUTOR")
    public Executor getOriginalExecutor() {
        return Proxies.getImplementation(originalExecutor);
    }

    public void setOriginalExecutor(Executor originalExecutor) {
        this.originalExecutor = originalExecutor;
    }

    @Column(name = "ESCALATION_LEVEL")
    public int getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(int escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public static EscalationGroup create(Process process, Task task, Executor originalExecutor, int escalationLevel) {
        EscalationGroup escalationGroup = new EscalationGroup();
        escalationGroup.setName(GROUP_PREFIX + "_" + process.getId() + "_" + task.getId());
        escalationGroup.setOriginalExecutor(originalExecutor);
        escalationGroup.setEscalationLevel(escalationLevel);
        return escalationGroup;
    }

    @Override
    public String toString() {
        return getName() + " (" + originalExecutor + "|" + escalationLevel + ")";
    }
}
