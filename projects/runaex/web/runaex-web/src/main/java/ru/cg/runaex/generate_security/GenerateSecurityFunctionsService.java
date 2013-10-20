package ru.cg.runaex.generate_security;

import ru.cg.runaex.database.bean.ParFile;

import java.util.List;

/**
 * @author urmancheev
 */
public interface GenerateSecurityFunctionsService {
  void generateSecurityFunctions(List<ParFile> parFiles);
}
