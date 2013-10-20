package ru.cg.runaex.components.validation;

import java.util.List;

import ru.cg.runaex.components.validation.annotation.CustomValidation;

/**
 * Имеет кастомную проверку
 *
 * @author urmancheev
 */
@CustomValidation
public interface ComponentWithCustomValidation {
  /**
   * Проводит проверку специфичную для компоненты
   *
   * @return коды ошибок
   */
  List<String> customValidate();
}
