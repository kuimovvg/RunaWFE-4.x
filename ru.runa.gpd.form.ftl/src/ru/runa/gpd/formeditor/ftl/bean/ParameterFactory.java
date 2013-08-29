package ru.runa.gpd.formeditor.ftl.bean;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.formeditor.WYSIWYGPlugin;
import ru.runa.gpd.formeditor.ftl.MethodTag.Param;

public final class ParameterFactory {
    private static final String PARAMETER_TYPE_EXTENSION_ID = "ru.runa.gpd.form.ftl.parameter_type";
    private static Map<String, Class<? extends ComponentParameter>> parameterTypes;

    private synchronized static void initParameterTypes() {
        if (parameterTypes != null)
            return;

        parameterTypes = new HashMap<String, Class<? extends ComponentParameter>>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(PARAMETER_TYPE_EXTENSION_ID);
        try {
            for (IConfigurationElement e : config) {
                String id = e.getAttribute("id");
                ComponentParameter parameterInstance = (ComponentParameter) e.createExecutableExtension("class");
                parameterTypes.put(id, parameterInstance.getClass());
            }
        } catch (CoreException ex) {
            WYSIWYGPlugin.logError("Unable to load ftl component parameter types", ex);
        }
    }

    public static ComponentParameter createParameter(Param param) {
        initParameterTypes();
        
        Class<? extends ComponentParameter> parameterClass = parameterTypes.get(param.typeId);
        ComponentParameter parameter;
        try {
            parameter = parameterClass.newInstance();
        } catch (InstantiationException ex) {
            WYSIWYGPlugin.logError("Unable to create ftl component parameter", ex);
            parameter = new StringParameter();
        } catch (IllegalAccessException ex) {
            WYSIWYGPlugin.logError("Unable to create ftl component parameter", ex);
            parameter = new StringParameter();
        }
        
        parameter.setParam(param);
        return parameter;
    }
}
