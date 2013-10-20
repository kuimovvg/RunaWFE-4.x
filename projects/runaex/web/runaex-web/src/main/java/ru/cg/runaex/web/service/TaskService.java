package ru.cg.runaex.web.service;

import java.util.List;
import java.util.Set;

import ru.cg.runaex.web.model.CategoryModel;

/**
 * @author Петров А.
 */
public interface TaskService {

  List<CategoryModel> loadStructuredTasks(Set<String> businessProcessesByTasks);
}
