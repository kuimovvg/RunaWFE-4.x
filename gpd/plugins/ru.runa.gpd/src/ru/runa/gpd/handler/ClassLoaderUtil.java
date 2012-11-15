package ru.runa.gpd.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import ru.runa.gpd.PluginLogger;

public class ClassLoaderUtil {
    public static List<String> getChildsOfType(IJavaProject project, String parentClassName) {
        List<String> classNames = new ArrayList<String>();
        try {
            IType type = project.findType(parentClassName);
            if (type == null) {
                PluginLogger.logInfo("ERROR: Type not found: " + parentClassName);
                return classNames;
            }
            // get all subclasses of parent class
            IType[] types = type.newTypeHierarchy(project, null).getAllSubtypes(type);
            for (IType childType : types) {
                try {
                    int flags = childType.getFlags();
                    if (!Flags.isAbstract(flags) && !Flags.isEnum(flags) && !Flags.isDeprecated(flags)) {
                        classNames.add(childType.getFullyQualifiedName());
                    }
                } catch (JavaModelException e) {
                    PluginLogger.logErrorWithoutDialog("Exception while testing type (bad class?) " + childType.getFullyQualifiedName(), e);
                }
            }
        } catch (JavaModelException e) {
            PluginLogger.logErrorWithoutDialog("Error while search types of the class: " + parentClassName, e);
        }
        return classNames;
    }
}
