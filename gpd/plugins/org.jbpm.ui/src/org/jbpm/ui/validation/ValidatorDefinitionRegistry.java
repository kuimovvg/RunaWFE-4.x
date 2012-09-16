package ru.runa.bpm.ui.validation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.validation.ValidatorDefinition.Param;

public class ValidatorDefinitionRegistry {
    private static final Map<String, ValidatorDefinition> definitions = new HashMap<String, ValidatorDefinition>();

    private static void init() {
        if (definitions.size() > 0) {
            return;
        }
        
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.bpm.ui.validators").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    ValidatorDefinition definition = new ValidatorDefinition(
                            configElement.getAttribute("name"),
                            configElement.getAttribute("displayName"),
                            ValidatorDefinition.FIELD_TYPE, 
                            configElement.getAttribute("description"));

                    String applicableString = configElement.getAttribute("applicable");
                    if (applicableString != null && applicableString.length() > 0) {
                        for (String string : applicableString.split(",")) {
                            definition.addApplicableType(string.trim());
                        }
                    }

                    IConfigurationElement[] paramElements = configElement.getChildren();
                    for (IConfigurationElement paramElement : paramElements) {
                        Param param = new Param(
                                paramElement.getAttribute("name"),
                                paramElement.getAttribute("displayName"),
                                paramElement.getAttribute("type"));
                        definition.addParam(param);
                    }
                    definitions.put(definition.getName(), definition);
                } catch (Exception e) {
                    DesignerLogger.logError("Error processing ru.runa.bpm.ui.validators element", e);
                }
            }
        }
        
        ValidatorDefinition global = new ValidatorDefinition(
                ValidatorDefinition.GLOBAL_VALIDATOR_NAME,
                "BSH",
                ValidatorDefinition.GLOBAL_TYPE, 
                "BSH code");
        global.addParam(new Param(ValidatorDefinition.EXPRESSION_PARAM_NAME, "", Param.BSH_TYPE));
        definitions.put(ValidatorDefinition.GLOBAL_VALIDATOR_NAME, global);
    }

    public static Map<String, ValidatorDefinition> getValidatorDefinitions() {
        init();
        return definitions;
    }

    public static ValidatorDefinition getGlobalDefinition() {
        init();
        return definitions.get(ValidatorDefinition.GLOBAL_VALIDATOR_NAME);
    }
}
