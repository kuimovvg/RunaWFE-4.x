package ru.runa.gpd.extension;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.Activator;
import ru.runa.gpd.lang.model.Variable;

public class VariableFormatRegistry extends ArtifactRegistry<VariableFormatArtifact> {
    private static final String XML_FILE_NAME = "variableFormats.xml";
    private static final VariableFormatRegistry instance = new VariableFormatRegistry();

    public static VariableFormatRegistry getInstance() {
        return instance;
    }

    public VariableFormatRegistry() {
        super(new ArtifactContentProvider<VariableFormatArtifact>());
    }

    @Override
    protected File getContentFile() {
        return new File(Activator.getPreferencesFolder(), XML_FILE_NAME);
    }

    @Override
    protected void loadDefaults(List<VariableFormatArtifact> list) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.formats").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                boolean enabled = Boolean.valueOf(configElement.getAttribute("enabled"));
                String className = configElement.getAttribute("className");
                String label = configElement.getAttribute("label");
                String variableClassName = configElement.getAttribute("variableClassName");
                list.add(new VariableFormatArtifact(enabled, className, label, variableClassName));
            }
        }
    }

    public static boolean isAssignableFrom(String superClassName, String className) {
        try {
            return Class.forName(superClassName).isAssignableFrom(Class.forName(className));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isApplicable(Variable variable, String classNameFilter) {
        String className = getInstance().getArtifact(variable.getFormat()).getVariableClassName();
        return isAssignableFrom(classNameFilter, className);
    }
}
