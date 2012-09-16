package ru.runa.bpm.ui.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.bpm.ui.DesignerLogger;

public class FormTypeProvider {
    private static final String FORMTYPE_EXT_POINT_ID = "ru.runa.bpm.ui.formtype";

    private static Map<String, FormType> formTypes = new HashMap<String, FormType>();
    private static List<FormType> sortedList = new ArrayList<FormType>();
    

    private static void init() {
        if (formTypes.size() == 0) {
            try {
                IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(FORMTYPE_EXT_POINT_ID).getExtensions();
                for (IExtension extension : extensions) {
                    IConfigurationElement[] configElements = extension.getConfigurationElements();
                    for (IConfigurationElement element : configElements) {
                        try {
                            String name = element.getAttribute("name");
                            String type = element.getAttribute("type");
                            FormType formType = (FormType) element.createExecutableExtension("contributor");
                            formType.setName(name);
                            formType.setType(type);
                            formTypes.put(type, formType);
                        } catch (Exception e) {
                            DesignerLogger.logError("Error processing form type extension", e);
                        }
                	}
                }
                sortedList.addAll(formTypes.values());
                Collections.sort(sortedList, new FormTypeComparator());
            } catch (Exception e) {
                DesignerLogger.logError("Error processing form type extension point", e);
            }
        }
    }

    public static List<FormType> getRegisteredFormTypes() {
        init();
        return sortedList;
    }

    public static FormType getFormTypeByName(String name) {
        init();
        for (FormType formType : formTypes.values()) {
            if (name.equals(formType.getName())) {
                return formType;
            }
        }
        throw new RuntimeException("No form type found, name = " + name);
    }

    public static FormType getFormType(String type) {
        init();
        if (!formTypes.containsKey(type)) {
            throw new RuntimeException("No form type found, type = " + type);
        }
        return formTypes.get(type);
    }
    
    private static class FormTypeComparator implements Comparator<FormType> {

        public int compare(FormType o1, FormType o2) {
            return o1.getType().compareTo(o2.getType());
        }
        
    }
}
