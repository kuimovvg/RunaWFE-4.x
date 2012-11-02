package ru.runa.wfe.definition.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.cache.ProcessDefCacheCtrl;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public class ProcessDefinitionLoader implements IProcessDefinitionLoader {
    @Autowired
    private ProcessDefCacheCtrl processDefCacheCtrl;

    @Override
    public ProcessDefinition getDefinition(Long id) {
        return processDefCacheCtrl.getDefinition(id);
    }

    @Override
    public ProcessDefinition getDefinition(String name) {
        return processDefCacheCtrl.getLatestDefinition(name);
    }

    @Override
    public ProcessDefinition getDefinition(Process process) {
        return getDefinition(process.getDefinition().getId());
    }

    @Override
    public ProcessDefinition getDefinition(Task task) {
        return getDefinition(task.getProcess());
    }

    @Override
    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return processDefCacheCtrl.getLatestDefinition(definitionName);
    }

    @Override
    public List<ProcessDefinition> getLatestProcessDefinitions() {
        return processDefCacheCtrl.getLatestProcessDefinitions();
    }

}
