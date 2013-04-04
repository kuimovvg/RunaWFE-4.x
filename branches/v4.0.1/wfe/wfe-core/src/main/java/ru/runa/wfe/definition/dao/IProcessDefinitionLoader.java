package ru.runa.wfe.definition.dao;

import java.util.List;

import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;

public interface IProcessDefinitionLoader {

    public ProcessDefinition getDefinition(Long id) throws DefinitionDoesNotExistException;

    public ProcessDefinition getDefinition(String name) throws DefinitionDoesNotExistException;

    public ProcessDefinition getDefinition(Process process) throws DefinitionDoesNotExistException;

    public ProcessDefinition getDefinition(Task task) throws DefinitionDoesNotExistException;

    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException;

    public List<ProcessDefinition> getLatestProcessDefinitions();
}
