package ru.runa.bpm.ui.custom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.model.Decision;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.util.MappingContentProvider;
import ru.runa.bpm.ui.util.TypeNameMapping;
import org.osgi.framework.Bundle;

public class CustomizationRegistry {
    private static final Map<String, Set<String>> handlers = new HashMap<String, Set<String>>();
    private static Map<String, DelegableProvider> customDelegableProviders = new HashMap<String, DelegableProvider>();
    private static DelegableProvider DEFAULT_DELEGABLE_PROVIDER = new DelegableProvider();
    private static DelegableProvider DEFAULT_DECISION_PROVIDER = new DefaultDecisionProvider();

    public static void init(IJavaProject project) {
        if (handlers.size() > 0) {
            return;
        }
        Set<String> classNames = new HashSet<String>();
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.delegation.ActionHandler"));
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.graph.def.ActionHandler"));
        handlers.put(Delegable.ACTION_HANDLER, classNames);
        
        classNames = new HashSet<String>();
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.delegation.DecisionHandler"));
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.graph.node.DecisionHandler"));
        handlers.put(Delegable.DECISION_HANDLER, classNames);
        
        classNames = new HashSet<String>();
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.delegation.AssignmentHandler"));
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.bpm.taskmgmt.def.AssignmentHandler"));
        handlers.put(Delegable.ASSIGNMENT_HANDLER, classNames);
        
        classNames = new HashSet<String>();
        classNames.addAll(ClassLoaderUtil.getChildsOfType(project, "ru.runa.commons.format.WebFormat"));
        handlers.put("FORMAT", classNames);

        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.bpm.ui.delegablePropertyDescriprors").getExtensions();
        for (IExtension extension : extensions) {
            Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                String className = configElement.getAttribute("className");
                String type = configElement.getAttribute("type");
                try {
                    DelegableProvider provider = (DelegableProvider) configElement.createExecutableExtension("cellEditorProvider");
                    provider.setBundle(bundle);
                    if (Delegable.DECISION_HANDLER.equals(type) && !(provider instanceof IDecisionProvider)) {
                        throw new Exception("Custom decision provider should implement IDecisionProvider interface.");
                    }
                    if (!TypeNameMapping.containsMapping(className)) {
                        TypeNameMapping.addMapping(className, configElement.getAttribute("name"));
                        MappingContentProvider.INSTANCE.saveToInput();
                    }
                    customDelegableProviders.put(className, provider);
                    handlers.get(type).add(className);
                } catch (Exception e) {
                    DesignerLogger.logError("Error processing ru.runa.bpm.ui.delegablePropertyDescriprors extension for: " + className, e);
                }
            }
        }
    }

    private static DelegableProvider getProvider(String className, DelegableProvider defaultProvider) {
        if (customDelegableProviders.containsKey(className)) {
            return customDelegableProviders.get(className);
        }
        return defaultProvider;
    }

    public static DelegableProvider getProvider(String className) {
        return getProvider(className, DEFAULT_DELEGABLE_PROVIDER);
    }
    
    public static IDecisionProvider getProvider(Decision decision) {
        return (IDecisionProvider) getProvider(decision.getDelegationClassName(), DEFAULT_DECISION_PROVIDER);
    }

    public static Set<String> getHandlerClasses(String type) {
        return handlers.get(type);
    }
    
    public static boolean isTypeRegistered(String className) {
        for (Set<String> set : handlers.values()) {
            if (set.contains(className))
                return true;
        }
        return false;
    }

    public static boolean isTypeRegisteredForType(String type, String className) {
        return getHandlerClasses(type).contains(className);
    }
}
