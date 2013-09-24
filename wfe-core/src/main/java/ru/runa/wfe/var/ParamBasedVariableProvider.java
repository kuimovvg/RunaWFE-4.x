package ru.runa.wfe.var;

import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Strings;

public class ParamBasedVariableProvider extends DelegableVariableProvider {
    private final ParamsDef paramsDef;

    public ParamBasedVariableProvider(IVariableProvider delegate, ParamsDef paramsDef) {
        super(delegate);
        this.paramsDef = paramsDef;
    }

    public ParamsDef getParamsDef() {
        return paramsDef;
    }

    private ParamDef getInputParamDef(String source) {
        // TODO back compatibility until 4.1.0
        if (paramsDef.getInputParams().containsKey("param:" + source)) {
            return paramsDef.getInputParams().get("param:" + source);
        }
        return paramsDef.getInputParam(source);
    }

    private String getVariableName(String source, ParamDef paramDef) {
        if (paramDef == null) {
            return source;
        }
        return paramDef.getVariableName();
    }

    @Override
    public Object getValue(String variableName) {
        ParamDef paramDef = getInputParamDef(variableName);
        if (paramDef != null && Strings.isNullOrEmpty(paramDef.getVariableName())) {
            return paramDef.getValue();
        }
        variableName = getVariableName(variableName, paramDef);
        return super.getValue(variableName);
    }

    @Override
    public WfVariable getVariable(String variableName) {
        variableName = getVariableName(variableName, getInputParamDef(variableName));
        return super.getVariable(variableName);
    }

}
