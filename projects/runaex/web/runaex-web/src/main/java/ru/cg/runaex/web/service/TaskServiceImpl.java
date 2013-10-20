package ru.cg.runaex.web.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.database.bean.model.Category;
import ru.cg.runaex.database.dao.MetadataDao;
import ru.cg.runaex.web.model.CategoryModel;
import ru.cg.runaex.web.model.ProcessDefinition;
import ru.cg.runaex.web.model.ProcessModel;
import ru.cg.runaex.web.security.SecurityUtils;

/**
 * @author Петров А.
 */
@Repository
public class TaskServiceImpl implements TaskService {

  @Autowired
  private MetadataDao metadataDao;

  @Autowired
  private RunaWfeService runaWfeService;

  @Override
  public List<CategoryModel> loadStructuredTasks(Set<String> businessProcessesByTasks) {
    List<ProcessDefinition> processDefinitions = runaWfeService.getProcessDefinitionsWithTasks(SecurityUtils.getCurrentRunaUser());

    List<Category> categories = null;
    Map<String, Long> linkProcessCategories = null;

    if (businessProcessesByTasks != null && !businessProcessesByTasks.isEmpty()) {
      linkProcessCategories = metadataDao.loadLinksWithInboxFilter(businessProcessesByTasks);
      Set<Long> categoriesByTasks = new HashSet<Long>(linkProcessCategories.values());
      categories = metadataDao.loadProjectsWithInboxFilter(categoriesByTasks);
    }
    else {
      categories = metadataDao.loadAllProjects();
      linkProcessCategories = metadataDao.loadAllProcessesCategoriesLinks();
    }

    // Fake category. For processes without mapping
    Category cgCategory = new Category();
    cgCategory.setName("cg");
    cgCategory.setId(-1L);
    cgCategory.setParentId(0L);
    categories.add(cgCategory);

    Map<Long, CategoryModel> categoryModelMap = toCategoryModelMap(categories);

    List<CategoryModel> categoriesTree = buildCategoryTree(categories, categoryModelMap);

    addProcessesToCategories(processDefinitions, categoryModelMap, linkProcessCategories);

    calculateTaskCounts(categoriesTree);

    removeEmptyProcesses(categoriesTree);

    return categoriesTree;
  }

  private void removeEmptyProcesses(List<CategoryModel> categoriesTree) {
    for (Iterator<CategoryModel> categoryIterator = categoriesTree.iterator(); categoryIterator.hasNext(); ) {
      CategoryModel nextCategoryModel = categoryIterator.next();

      if (nextCategoryModel.getTaskCount() == 0) {
        categoryIterator.remove();
        continue;
      }
      List<ProcessModel> processes = nextCategoryModel.getProcesses();
      List<CategoryModel> categoryModels = nextCategoryModel.getCategories();
      for (Iterator<ProcessModel> processModelIterator = processes.iterator(); processModelIterator.hasNext(); ) {
        ProcessModel processModel = processModelIterator.next();
        if (processModel.getTaskCount() == 0) {
          processModelIterator.remove();
        }
      }
      if (processes.isEmpty()) {
        categoryIterator.remove();
      }

      if (categoryModels != null) {
        removeEmptyProcesses(categoryModels);
      }
    }
  }

  private Map<Long, CategoryModel> toCategoryModelMap(List<Category> categories) {
    Map<Long, CategoryModel> result = new HashMap<Long, CategoryModel>();

    for (Category category : categories) {
      CategoryModel categoryModel = new CategoryModel();
      categoryModel.setId(category.getId());
      categoryModel.setName(category.getName());
      result.put(category.getId(), categoryModel);
    }

    return result;
  }

  private List<CategoryModel> buildCategoryTree(List<Category> categories, Map<Long, CategoryModel> categoryModelMap) {
    List<CategoryModel> treeRoots = new LinkedList<CategoryModel>();

    CategoryModel categoryModel;
    CategoryModel parentCategoryModel;
    for (Category category : categories) {
      categoryModel = categoryModelMap.get(category.getId());

      if (category.getParentId() == 0) {
        treeRoots.add(categoryModel);
      }
      else {
        parentCategoryModel = categoryModelMap.get(category.getParentId());
        parentCategoryModel.getCategories().add(categoryModel);
      }
    }

    return treeRoots;
  }

  private void addProcessesToCategories(List<ProcessDefinition> definitions, Map<Long, CategoryModel> categoryModelMap, Map<String, Long> linkProcessCategories) {
    Long categoryModelId;
    CategoryModel categoryModel;
    for (ProcessDefinition definition : definitions) {
      categoryModelId = linkProcessCategories.get(definition.getName());
      if (categoryModelId == null)
        categoryModelId = -1L;
      categoryModel = categoryModelMap.get(categoryModelId);

      ProcessModel processModel = new ProcessModel();
      processModel.setName(definition.getName());
      processModel.setTaskCount(definition.getTasks().size());
      processModel.getTasks().addAll(definition.getTasks());

      categoryModel.getProcesses().add(processModel);
    }
  }

  private void calculateTaskCounts(List<CategoryModel> categoryTree) {
    for (CategoryModel categoryModel : categoryTree) {
      calculateCategoryModelTaskCount(categoryModel);
    }
  }

  private void calculateCategoryModelTaskCount(CategoryModel categoryModel) {
    int taskCount = 0;

    for (ProcessModel processModel : categoryModel.getProcesses()) {
      taskCount += processModel.getTaskCount();
    }

    for (CategoryModel childCategory : categoryModel.getCategories()) {
      calculateCategoryModelTaskCount(childCategory);

      taskCount += childCategory.getTaskCount();
    }

    categoryModel.setTaskCount(taskCount);
  }
}
