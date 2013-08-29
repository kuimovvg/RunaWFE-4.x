package ru.runa.gpd.extension;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

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
                String javaClassName = configElement.getAttribute("javaClassName");
                list.add(new VariableFormatArtifact(enabled, className, label, javaClassName));
            }
        }
    }

    public static boolean isAssignableFrom(String superClassName, String className) {
        try {
            return isAssignableFrom(Class.forName(superClassName), className);
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(superClassName, th);
            return false;
        }
    }

    public static boolean isAssignableFrom(Class<?> superClass, String className) {
        try {
            Class<?> testingClass = Class.forName(className);
            return superClass.isAssignableFrom(testingClass);
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog(className, th);
            return false;
        }
    }

    public static boolean isApplicable(Variable variable, String classNameFilter) {
        return isAssignableFrom(classNameFilter, variable.getJavaClassName());
    }

    public VariableFormatArtifact getArtifactByJavaClassName(String javaClassName) {
        for (VariableFormatArtifact artifact : getAll()) {
            if (Objects.equal(javaClassName, artifact.getJavaClassName())) {
                return artifact;
            }
        }
        return null;
    }

    public VariableFormatArtifact getArtifactNotNullByJavaClassName(String javaClassName) {
        VariableFormatArtifact artifact = getArtifactByJavaClassName(javaClassName);
        if (artifact == null) {
            throw new RuntimeException("Artifact javaClassName='" + javaClassName + "' does not exist");
        }
        return artifact;
    }

    public List<String> getSuperClassNames(String className) {
        List<String> result = Lists.newArrayList();
        for (VariableFormatArtifact artifact : getAll()) {
            if (isAssignableFrom(artifact.getJavaClassName(), className)) {
                result.add(artifact.getJavaClassName());
            }
        }
        return result;
    }
}
