package ru.runa.wf.dao;

import java.util.Date;
import java.util.List;

import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.StartedSubprocesses;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

// TODO split in future
public interface TmpDAO {
    // move to commonDAO

    <T extends Object> T get(Class<T> entityClass, Long id);

    void save(Object entity);

    void delete(Object entity);

    // move to to BPDefinitionDAO

    void updateBPDefinitionInfo(String definitionName, List<String> processType);

    void deleteBPDefinitionInfo(String definitionName);

    // move to to BPInstanceDAO

    // addAutoSaveProcessInstance
    void saveProcessInstance(ProcessInstance instance);

    List<TaskInstance> getProcessInstanceTasks(Long bpInstanceId);

    List<Token> getProcessInstanceTokens(Long bpInstanceId);

    List<TaskInstance> getTokenWithSameSwimlane(TaskInstance taskInstance);

    List<ProcessInstance> getProcessInstancesForDefinitionName(String definitionName);

    List<ProcessInstance> getProcessInstancesForDefinitionVersion(String definitionName, Long version);

    List<ProcessInstance> getProcessInstanceByDate(Date startDate, Date endDate, boolean isFinishedOnly);

    List<ProcessInstance> getProcessInstanceByStartDateInterval(Date startDateFrom, Date startDateTill, boolean isFinishedOnly);

    List<ProcessInstance> getProcessInstanceByIdInterval(Long idFrom, Long idTill, boolean isFinishedOnly);

    List<StartedSubprocesses> getRootSubprocesses(Long bpInstanceId);

    List<StartedSubprocesses> getSubprocesses(Long bpInstanceId);

}
