package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction;

import com.google.common.collect.Lists;

public class BotTask implements Delegable, Comparable<BotTask> {
    public static final String SWIMLANE_DEFINITION_NAME = ExecutorByNameFunction.class.getName();
    private BotTaskType type = BotTaskType.SIMPLE;
    private String name;
    private String delegationClassName = "";
    private String delegationConfiguration = "";
    private ParamDefConfig paramDefConfig;

    public BotTask() {
    }

    public BotTaskType getType() {
        return type;
    }

    public void setType(BotTaskType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.TASK_HANDLER;
    }

    @Override
    public String getDelegationClassName() {
        return delegationClassName;
    }

    @Override
    public void setDelegationClassName(String delegateClassName) {
        this.delegationClassName = delegateClassName;
    }

    @Override
    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    @Override
    public void setDelegationConfiguration(String configuration) {
        delegationConfiguration = configuration;
    }

    public boolean isDelegationConfigurationInXml() {
        if (HandlerRegistry.getProvider(delegationClassName) instanceof XmlBasedConstructorProvider) {
            return true;
        }
        return XmlUtil.isXml(delegationConfiguration);
    }

    /**
     * param-based config
     * 
     * @return null for simple bot task type
     */
    public ParamDefConfig getParamDefConfig() {
        return paramDefConfig;
    }

    /**
     * param-based config
     */
    public void setParamDefConfig(ParamDefConfig paramDefConfig) {
        this.paramDefConfig = paramDefConfig;
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
        List<String> result = Lists.newArrayList();
        if (paramDefConfig != null) {
            for (ParamDefGroup group : paramDefConfig.getGroups()) {
                for (ParamDef paramDef : group.getParameters()) {
                    boolean applicable = typeClassNameFilters == null || typeClassNameFilters.length == 0;
                    if (!applicable && paramDef.getFormatFilters().size() > 0) {
                        for (String typeClassNameFilter : typeClassNameFilters) {
                            if (VariableFormatRegistry.isAssignableFrom(typeClassNameFilter, paramDef.getFormatFilters().get(0))) {
                                applicable = true;
                                break;
                            }
                        }
                    }
                    if (applicable) {
                        result.add(paramDef.getName());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(BotTask o) {
        if (name == null || o == null || o.name == null) {
            return -1;
        }
        return name.compareTo(o.name);
    }
}
