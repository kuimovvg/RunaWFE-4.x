package ru.runa.wf.dao;

import org.hibernate.Session;

import ru.runa.bpm.graph.exe.ProcessInstance;

public interface ArchiveDAO {
    public void initSessionTransferToArchive();

    public void initSessionTransferFromArchive();

    public void copyProcessInstanceInArchive(ProcessInstance instance);

    public void copyProcessDefinitionInArchive(Long processDefinitionId);

    public void copyProcessInstanceFromArchive(ProcessInstance instance);

    public void copyProcessDefinitionFromArchive(Long processDefinitionId);

    public ProcessInstance getProcessInstance(Long processInstanceId);

    public Session getArchiveSession();
}
