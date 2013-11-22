package ru.runa.gpd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ProcessCache {
    private static Map<IFile, ProcessDefinition> CACHE_BY_FILE = new HashMap<IFile, ProcessDefinition>();
    private static Map<String, ProcessDefinition> CACHE_BY_NAME = new HashMap<String, ProcessDefinition>();
    static {
        try {
            for (IFile file : IOUtils.getAllProcessDefinitionFiles()) {
                List<IFile> subprocessFiles = Lists.newArrayList();
                try {
                    ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                    cacheProcessDefinition(file, definition);
                    findSubProcessFiles(file.getParent(), subprocessFiles);
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("parsing process " + file, e);
                }
                for (IFile subprocessFile : subprocessFiles) {
                    try {
                        ProcessDefinition definition = NodeRegistry.parseProcessDefinition(subprocessFile);
                        cacheProcessDefinition(subprocessFile, definition);
                    } catch (Exception e) {
                        PluginLogger.logErrorWithoutDialog("parsing subprocess " + subprocessFile, e);
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    private static void findSubProcessFiles(IContainer container, List<IFile> result) throws CoreException {
        for (IResource resource : container.members()) {
            if (resource.getName().endsWith(ParContentProvider.PROCESS_DEFINITION_FILE_NAME) && !resource.getName().equals(ParContentProvider.PROCESS_DEFINITION_FILE_NAME)) {
                result.add((IFile) resource);
            }
        }
    }

    private static void cacheProcessDefinition(IFile file, ProcessDefinition definition) throws Exception {
        ParContentProvider.readAuxInfo(file, definition);
        CACHE_BY_FILE.put(file, definition);
        CACHE_BY_NAME.put(definition.getName(), definition);
    }

    public static ProcessDefinition newProcessDefinitionWasCreated(IFile file) {
        try {
            ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
            cacheProcessDefinition(file, definition);
            return definition;
        } catch (Exception e) {
            PluginLogger.logError("Parsing process definition failed: " + file.toString(), e);
            return null;
        }
    }

    public static void processDefinitionWasDeleted(IFile file) {
        try {
            ProcessDefinition definition = CACHE_BY_FILE.remove(file);
            if (definition != null) {
                CACHE_BY_NAME.remove(definition.getName());
            }
        } catch (Exception e) {
            PluginLogger.logError("Parsing process definition failed: " + file.toString(), e);
        }
    }

    public static Set<ProcessDefinition> getAllProcessDefinitions() {
        return new HashSet<ProcessDefinition>(CACHE_BY_NAME.values());
    }

    public static List<String> getAllProcessDefinitionNames() {
        List<String> list = new ArrayList<String>(CACHE_BY_NAME.keySet());
        Collections.sort(list);
        return list;
    }

    public static Map<IFile, ProcessDefinition> getAllProcessDefinitionsMap() {
        return new HashMap<IFile, ProcessDefinition>(CACHE_BY_FILE);
    }

    public static void invalidateProcessDefinition(IFile file) {
        ProcessDefinition definition = CACHE_BY_FILE.remove(file);
        if (definition != null) {
            CACHE_BY_NAME.remove(definition.getName());
            if (file.exists()) {
                getProcessDefinition(file);
            }
        }
    }

    public static ProcessDefinition getProcessDefinition(IFile file) {
        if (!CACHE_BY_FILE.containsKey(file)) {
            try {
                ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                cacheProcessDefinition(file, definition);
            } catch (Exception e) {
                throw new RuntimeException("Parsing process definition failed: " + file, e);
            }
        }
        return CACHE_BY_FILE.get(file);
    }

    public static IFile getProcessDefinitionFile(ProcessDefinition processDefinition) {
        for (Map.Entry<IFile, ProcessDefinition> entry : CACHE_BY_FILE.entrySet()) {
            if (Objects.equal(processDefinition, entry.getValue())) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No file found for " + processDefinition);
    }

    public static ProcessDefinition getFirstProcessDefinition(String name) {
        if (!CACHE_BY_NAME.containsKey(name)) {
            try {
                IFile file = getFirstProcessDefinitionFile(name);
                if (file != null) {
                    ProcessDefinition definition = NodeRegistry.parseProcessDefinition(file);
                    cacheProcessDefinition(file, definition);
                }
            } catch (Exception e) {
                PluginLogger.logError("Parsing process definition failed: " + name, e);
                return null;
            }
        }
        return CACHE_BY_NAME.get(name);
    }

    /**
     * Get process definition file or <code>null</code>.
     */
    public static IFile getFirstProcessDefinitionFile(String name) {
        try {
            for (IFile file : IOUtils.getAllProcessDefinitionFiles()) {
                String processName = file.getFullPath().segment(3);
                if (name.equals(processName)) {
                    return file;
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("Parsing process definition failed: " + name, e);
            return null;
        }
        return null;
    }
}
