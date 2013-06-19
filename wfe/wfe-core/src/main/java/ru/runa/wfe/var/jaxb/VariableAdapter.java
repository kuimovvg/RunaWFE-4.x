package ru.runa.wfe.var.jaxb;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import ru.runa.wfe.var.VariableDefinition;

public class VariableAdapter extends XmlAdapter<WfVariable, ru.runa.wfe.var.dto.WfVariable> {

    @Override
    public WfVariable marshal(ru.runa.wfe.var.dto.WfVariable variable) {
        WfVariable wsVariable = new WfVariable();
        wsVariable.name = variable.getDefinition().getName();
        wsVariable.scriptingName = variable.getDefinition().getScriptingName();
        wsVariable.formatClassName = variable.getDefinition().getFormatClassName();
        if (variable.getValue() instanceof byte[]) {
            wsVariable.bytesValue = (byte[]) variable.getValue();
        } else if (variable.getValue() instanceof Long) {
            wsVariable.longValue = (Long) variable.getValue();
        } else if (variable.getValue() instanceof Boolean) {
            wsVariable.booleanValue = (Boolean) variable.getValue();
        } else if (variable.getValue() instanceof Date) {
            wsVariable.dateValue = (Date) variable.getValue();
        } else {
            wsVariable.stringValue = String.valueOf(variable.getValue());
        }
        return wsVariable;
    }

    @Override
    public ru.runa.wfe.var.dto.WfVariable unmarshal(WfVariable wsVariable) {
        Object value = null;
        if (wsVariable.bytesValue != null) {
            value = wsVariable.bytesValue;
        } else if (wsVariable.booleanValue != null) {
            value = wsVariable.booleanValue;
        } else if (wsVariable.dateValue != null) {
            value = wsVariable.dateValue;
        } else if (wsVariable.longValue != null) {
            value = wsVariable.longValue;
        } else if (wsVariable.stringValue != null) {
            value = wsVariable.stringValue;
        }
        VariableDefinition definition = new VariableDefinition(false, wsVariable.name, wsVariable.formatClassName, wsVariable.scriptingName);
        return new ru.runa.wfe.var.dto.WfVariable(definition, value);
    }

}
