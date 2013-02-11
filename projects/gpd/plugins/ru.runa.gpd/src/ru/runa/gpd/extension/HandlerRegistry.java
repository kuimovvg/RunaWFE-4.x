package ru.runa.gpd.extension;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.decision.DefaultDecisionProvider;
import ru.runa.gpd.extension.decision.IDecisionProvider;
import ru.runa.gpd.lang.model.Decision;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class HandlerRegistry extends ArtifactRegistry<HandlerArtifact> {
    private static Map<String, DelegableProvider> customDelegableProviders = new HashMap<String, DelegableProvider>();
    private static DelegableProvider DEFAULT_DELEGABLE_PROVIDER = new DelegableProvider();
    private static DelegableProvider DEFAULT_DECISION_PROVIDER = new DefaultDecisionProvider();
    private static final String XML_FILE_NAME = "handlers.xml";
    private static final HandlerRegistry instance = new HandlerRegistry();

    public static HandlerRegistry getInstance() {
        return instance;
    }

    public HandlerRegistry() {
        super(new HandlerContentProvider());
    }

    @Override
    protected File getContentFile() {
        return new File(Activator.getPreferencesFolder(), XML_FILE_NAME);
    }

    @Override
    protected void loadDefaults(List<HandlerArtifact> list) { // TODO load task handlers
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.handlers").getExtensions();
        for (IExtension extension : extensions) {
            Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                boolean enabled = Boolean.valueOf(configElement.getAttribute("enabled"));
                String className = configElement.getAttribute("className");
                String label = configElement.getAttribute("label");
                String type = configElement.getAttribute("type");
                String providerClassName = configElement.getAttribute("cellEditorProvider");
                try {
                    if (providerClassName != null) {
                        DelegableProvider provider = (DelegableProvider) configElement.createExecutableExtension("cellEditorProvider");
                        provider.setBundle(bundle);
                        if (HandlerArtifact.DECISION.equals(type) && !(provider instanceof IDecisionProvider)) {
                            throw new Exception("Custom decision provider should implement IDecisionProvider interface.");
                        }
                        customDelegableProviders.put(className, provider);
                    }
                    list.add(new HandlerArtifact(enabled, className, label, type, providerClassName));
                } catch (Exception e) {
                    PluginLogger.logError("Error processing 'handlers' extension for: " + className, e);
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

    public List<HandlerArtifact> getAll(String type, boolean onlyEnabled) {
        List<HandlerArtifact> list = Lists.newArrayList();
        for (HandlerArtifact handlerArtifact : getAll()) {
            if (onlyEnabled && !handlerArtifact.isEnabled()) {
                continue;
            }
            if (Objects.equal(type, handlerArtifact.getType())) {
                list.add(handlerArtifact);
            }
        }
        return list;
    }

    public boolean isArtifactRegistered(String type, String className) {
        HandlerArtifact handlerArtifact = getArtifact(className);
        return handlerArtifact != null && Objects.equal(type, handlerArtifact.getType());
    }
}
