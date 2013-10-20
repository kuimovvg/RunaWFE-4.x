package ru.cg.runaex.web.service;

import ru.cg.runaex.validation.ErrorMessage;
import ru.cg.runaex.database.bean.FtlComponent;

import java.util.Collection;
import java.util.List;

/**
 * @author Bagautdinov
 */
public interface ComponentValidatorService {

  List<ErrorMessage> validateComponents(Collection<FtlComponent> ftlComponents);
}
