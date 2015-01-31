package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class DefaultComponentValidator implements IComponentValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component) {
        List<ValidationError> list = Lists.newArrayList();
        for (ComponentParameter parameter : component.getType().getParameters()) {
            if (parameter.isRequired()) {
                Object value = component.getParameterValue(parameter);
                if ((value instanceof String && Strings.isNullOrEmpty((String) value)) || (value instanceof List && ((List) value).size() == 0)) {
                    list.add(ValidationError.createError(formNode, Messages.getString("validation.requiredComponentParameterIsNotSet", component.getType().getLabel())));
                    return list;
                }
            }
        }
        return list;
    }
}
