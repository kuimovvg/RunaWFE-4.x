package ru.cg.runaex.generate_security.dao;

import java.util.List;

import ru.cg.runaex.generate_security.model.Function;


/**
 * @author urmancheev
 */
public interface FunctionDao {

  Long save(Function function);

  void saveParameters(Long functionId, List<Function.Parameter> parameters);
}
