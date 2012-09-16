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
package ru.runa.af;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import ru.runa.bpm.taskmgmt.exe.TaskInstance;

import com.google.common.base.Objects;

@Entity
@Table(name = "EXECUTOR_OPEN_TASKS", uniqueConstraints = @UniqueConstraint(columnNames = { "EXECUTOR_ID", "TASK_ID" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ExecutorOpenTask {
    private Long id;

    private Executor executor;
    private TaskInstance taskInstance;

    public ExecutorOpenTask() {
    }

    public ExecutorOpenTask(Executor executor, TaskInstance taskInstance) {
        this.executor = executor;
        this.taskInstance = taskInstance;
    }

    @ManyToOne(targetEntity = Executor.class)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false, insertable = true, updatable = false)
    @Index(name = "EXTSK_EXEC_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public Executor getExecutor() {
        return executor;
    }

    protected void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @ManyToOne(targetEntity = TaskInstance.class)
    @JoinColumn(name = "TASK_ID", nullable = false, insertable = true, updatable = false)
    @Index(name = "EXTSK_TASK_ID_IDX")
    @Fetch(FetchMode.JOIN)
    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    protected void setTaskInstance(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_EXECUTOR_OPEN_TASKS")
    @Column(name = "ID", nullable = false)
    protected Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExecutorOpenTask)) {
            return false;
        }
        ExecutorOpenTask task = (ExecutorOpenTask) obj;
        return Objects.equal(executor, task.executor) && Objects.equal(taskInstance, task.taskInstance);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(executor, taskInstance);
    }
}
