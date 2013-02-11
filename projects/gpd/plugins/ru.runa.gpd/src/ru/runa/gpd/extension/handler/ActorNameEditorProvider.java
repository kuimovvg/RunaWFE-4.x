package ru.runa.gpd.extension.handler;

import java.util.Map;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Delegable;

public class ActorNameEditorProvider extends ParamBasedProvider {
    private static final String ACTOR_CODE = "actorCode";
    private static final String ACTOR_LOGIN = "actorLogin";
    private static Config config = new Config();

    @Override
    protected ParamDefConfig getParamConfig(Delegable delegable) {
        return config;
    }

    public static class Config extends ParamDefConfig {
        public Config() {
            super("config");
            ParamDef p;
            ParamDefGroup inputGroup = new ParamDefGroup(ParamDefGroup.NAME_INPUT);
            p = new ParamDef(ACTOR_CODE, Localization.getString("ActorNameEditorProvider.param.actorCode"));
            p.setOptional(true);
            p.getFormatFilters().add(String.class.getName());
            p.getFormatFilters().add(Long.class.getName());
            inputGroup.getParameters().add(p);
            p = new ParamDef(ACTOR_LOGIN, Localization.getString("ActorNameEditorProvider.param.actorLogin"));
            p.setOptional(true);
            p.getFormatFilters().add(String.class.getName());
            inputGroup.getParameters().add(p);
            p = new ParamDef("format", Localization.getString("ActorNameEditorProvider.param.format"));
            p.setUseVariable(false);
            p.setComboItems(new String[] { "full name", "name", "email", "code", "description" });
            inputGroup.getParameters().add(p);
            ParamDefGroup outputGroup = new ParamDefGroup(ParamDefGroup.NAME_OUTPUT);
            p = new ParamDef("result", Localization.getString("ActorNameEditorProvider.param.result"));
            p.getFormatFilters().add(String.class.getName());
            outputGroup.getParameters().add(p);
            getGroups().add(inputGroup);
            getGroups().add(outputGroup);
        }

        @Override
        public boolean validate(String configuration) {
            if (!super.validate(configuration)) {
                return false;
            }
            Map<String, String> props = parseConfiguration(configuration);
            return isValid(props.get(ACTOR_CODE)) || isValid(props.get(ACTOR_LOGIN));
        }
    }
}
