package ru.runa.gpd.data.util;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ru.cg.runaex.shared.bean.project.xml.Category;
import ru.cg.runaex.shared.bean.project.xml.Process;
import ru.cg.runaex.shared.bean.project.xml.Project;
import ru.runa.gpd.data.DaoFactory;
import ru.runa.gpd.ui.widget.treecombo.TreeCombo;
import ru.runa.gpd.ui.widget.treecombo.TreeComboItem;

public class ProjectStructureUtils {

    public static void fillTreeCombo(TreeCombo combo, String projectName) {
        fillTreeCombo(combo, projectName, false);
    }

    public static void fillTreeCombo(TreeCombo combo, String projectName, boolean selectRoot) {
        Project projectStructure = DaoFactory.getProcessCategoryDao().get(projectName);

        TreeComboItem root = new TreeComboItem(combo, SWT.NONE);
        root.setText(projectStructure.getProjectName());
        
        if (selectRoot)
            combo.select(root);

        for (Category category : projectStructure.getCategories()) {
            addProjectCategoryToTreeCombo(root, category);
        }
    }

    private static void addProjectCategoryToTreeCombo(TreeComboItem parentItem, Category category) {
        TreeComboItem categoryItem = new TreeComboItem(parentItem, SWT.NONE);
        categoryItem.setText(category.getCategoryName());

        for (Category subcategory : category.getCategories()) {
            addProjectCategoryToTreeCombo(categoryItem, subcategory);
        }
    }

    public static void addProcess(String processName, String[] categoryFullPath) {
        String projectName = categoryFullPath[0];

        Project project = DaoFactory.getProcessCategoryDao().get(projectName);

        Process process = new Process();
        process.setProcessName(processName);

        if (categoryFullPath.length == 1) {
            project.getProcesses().add(process);
        } else {
            Category category = findCategory(project.getCategories(), categoryFullPath, 1);
            category.getProcesses().add(process);
        }

        DaoFactory.getProcessCategoryDao().save(project);
    }

    private static Category findCategory(List<Category> categories, String[] fullPath, int startFrom) {
        String categoryName = fullPath[startFrom];

        for (Category category : categories) {
            if (category.getCategoryName().equals(categoryName)) {
                if (startFrom == fullPath.length - 1) {
                    return category;
                } else {
                    return findCategory(category.getCategories(), fullPath, startFrom + 1);
                }
            }
        }

        return null;
    }

    public static void removeProcess(String projectName, String processName) {
        Project project = DaoFactory.getProcessCategoryDao().get(projectName);

        Process process = findProcessWithName(project.getProcesses(), processName);

        if (process != null) {
            project.getProcesses().remove(process);
        } else {
            boolean removed = false;

            for (Category category : project.getCategories()) {
                removed = removeProcessFromCategory(category, processName);

                if (removed) {
                    break;
                }
            }
        }

        DaoFactory.getProcessCategoryDao().save(project);
    }

    private static boolean removeProcessFromCategory(Category category, String processName) {
        boolean removed = false;

        Process process = findProcessWithName(category.getProcesses(), processName);

        if (process != null) {
            category.getProcesses().remove(process);
            removed = true;
        } else {

            for (Category childCategory : category.getCategories()) {
                removed = removeProcessFromCategory(childCategory, processName);

                if (removed) {
                    break;
                }
            }
        }

        return removed;
    }

    public static void renameProcess(String projectName, String newProcessName, String oldProcessName) {
        Project project = DaoFactory.getProcessCategoryDao().get(projectName);

        Process process = findProcessWithName(project.getProcesses(), oldProcessName);

        if (process != null) {
            process.setProcessName(newProcessName);
        } else {
            process = findProcessWithNameInCategories(project.getCategories(), oldProcessName);

            if (process != null) {
                process.setProcessName(newProcessName);
            }
        }

        DaoFactory.getProcessCategoryDao().save(project);
    }

    private static Process findProcessWithName(List<Process> processes, String processName) {
        if (processes == null) {
            return null;
        }

        for (Process process : processes) {
            if (process.getProcessName().equals(processName)) {
                return process;
            }
        }

        return null;
    }

    private static Process findProcessWithNameInCategories(List<Category> categories, String processName) {
        Process process = null;

        if (categories == null) {
            return null;
        }

        for (Category category : categories) {
            process = findProcessWithName(category.getProcesses(), processName);

            if (process == null) {
                process = findProcessWithNameInCategories(category.getCategories(), processName);
            }

            if (process != null) {
                return process;
            }
        }

        return null;
    }

    public static void fillTree(Tree tree, String projectName) {
        fillTree(tree, projectName, false);
    }

    public static void fillTree(Tree tree, String projectName, boolean selectRoot) {
        Project projectStructure = DaoFactory.getProcessCategoryDao().get(projectName);

        TreeItem root = new TreeItem(tree, SWT.NONE);
        root.setText(projectStructure.getProjectName());

		if (selectRoot) {
		    tree.select(root);
		}
		
        for (Category category : projectStructure.getCategories()) {
            addProjectCategoryToTree(root, category);
        }
    }

    private static void addProjectCategoryToTree(TreeItem parentItem, Category category) {
        TreeItem categoryItem = new TreeItem(parentItem, SWT.NONE);
        categoryItem.setText(category.getCategoryName());

        for (Category subcategory : category.getCategories()) {
            addProjectCategoryToTree(categoryItem, subcategory);
        }
    }

    private static Category findCategoryInProject(Project project, String[] categoryFullPath) {
        List<Category> categories = project.getCategories();

        Category targetCategory = null;
        for (int i = 1; i < categoryFullPath.length; ++i) {
            targetCategory = null;
            for (Category category : categories) {
                if (categoryFullPath[i].equals(category.getCategoryName())) {
                    targetCategory = category;
                    categories = targetCategory.getCategories();
                    break;
                }
            }

            if (targetCategory == null) {
                return null;
            }
        }

        return targetCategory;
    }

    public static void addCategory(String[] parentCategoryFullPath, String newCategoryName) {
        Project project = DaoFactory.getProcessCategoryDao().get(parentCategoryFullPath[0]);

        Category newCategory = new Category();
        newCategory.setCategoryName(newCategoryName);

        if (parentCategoryFullPath.length == 1) {
            project.getCategories().add(newCategory);
        } else {
            Category parentCategory = findCategoryInProject(project, parentCategoryFullPath);
            parentCategory.getCategories().add(newCategory);
        }

        DaoFactory.getProcessCategoryDao().save(project);
    }

    public static void renameCategory(String[] categoryFullPath, String newName) {
        Project project = DaoFactory.getProcessCategoryDao().get(categoryFullPath[0]);

        Category editedCategory = findCategoryInProject(project, categoryFullPath);
        editedCategory.setCategoryName(newName);

        DaoFactory.getProcessCategoryDao().save(project);
    }

    public static void deleteCategory(String[] categoryFullPath) {
        Project project = DaoFactory.getProcessCategoryDao().get(categoryFullPath[0]);

        List<Category> targetCategoryLevel = null;
        if (categoryFullPath.length == 2) {
            targetCategoryLevel = project.getCategories();
        } else {
            String[] parentElemetFullPath = new String[categoryFullPath.length - 1];
            System.arraycopy(categoryFullPath, 0, parentElemetFullPath, 0, parentElemetFullPath.length);

            Category parentCategory = findCategoryInProject(project, parentElemetFullPath);
            targetCategoryLevel = parentCategory.getCategories();
        }

        String deletedCategoryName = categoryFullPath[categoryFullPath.length - 1];
        Category deletedCategory = null;
        for (Category category : targetCategoryLevel) {
            if (deletedCategoryName.equals(category.getCategoryName())) {
                deletedCategory = category;
                break;
            }
        }
        targetCategoryLevel.remove(deletedCategory);

        DaoFactory.getProcessCategoryDao().save(project);
    }
}
