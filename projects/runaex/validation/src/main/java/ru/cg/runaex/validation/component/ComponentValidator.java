package ru.cg.runaex.validation.component;

import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.validation.ErrorMessage;

import java.util.List;
import java.util.Map;

/**
 * @author urmancheev
 */
public interface ComponentValidator {

  List<ErrorMessage> validate(IsComponent component, Map<String, String> parameters);
}
