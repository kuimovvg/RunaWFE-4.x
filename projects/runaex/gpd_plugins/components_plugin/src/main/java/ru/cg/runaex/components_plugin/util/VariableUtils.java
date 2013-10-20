package ru.cg.runaex.components_plugin.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.ProjectFinder;

public final class VariableUtils {

    private VariableUtils() {

    }

    public static List<Variable> getVariables() {
        IFile adjacentFile = ProjectFinder.getCurrentFile();
        ProcessDefinition currentDefinition = null;
        if (adjacentFile != null && adjacentFile.getParent().exists()) {
            IFile definitionFile = ProjectFinder.getProcessDefinitionFile((IFolder) adjacentFile.getParent());
            if (definitionFile != null && definitionFile.exists()) {
                currentDefinition = ProcessCache.getProcessDefinition(definitionFile);
            }
        }

        return currentDefinition != null ? currentDefinition.getVariables(true) : new ArrayList<Variable>(0);
    }
}
