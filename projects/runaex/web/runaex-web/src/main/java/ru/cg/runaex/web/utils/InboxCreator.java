package ru.cg.runaex.web.utils;

import java.util.List;

import ru.cg.runaex.web.model.CategoryModel;
import ru.cg.runaex.web.model.ProcessModel;

/**
 * @author golovlyev
 */
public final class InboxCreator {

  public static String printCategories(List<CategoryModel> categoryModels) {
    StringBuilder html = new StringBuilder();

    int totalTaskCount = 0;
    for (CategoryModel categoryModel : categoryModels) {
      totalTaskCount += categoryModel.getTaskCount();
    }
    makeFirstLiElem(html, totalTaskCount);

    for (CategoryModel categoryModel : categoryModels) {
      addCategory(categoryModel, html);
    }

    return html.toString();
  }

  private static void makeFirstLiElem(StringBuilder html, int totalTaskCount) {
    html.append("<li>\n")
        .append("<span>\n")
        .append("<a onclick='javascript: refreshTable()'>Задачи</a>\n")
        .append("</span>\n")
        .append("<span class=\"pull-right\">").append(totalTaskCount).append("</span>\n")
        .append("</li>");
  }

  public static void addCategory(CategoryModel categoryModel, StringBuilder html) {
    html.append("<li>\n");
    addCategoryFilter(categoryModel.getId(), categoryModel.getName(), html);
    html.append("<span class=\"pull-right opensubheader pReload\">").append(categoryModel.getTaskCount()).append("</span>\n")
        .append("</li>\n<li class=\"open\">\n")
        .append("<ul class=\"nav nav-list subcategoryitems\">");

    for (CategoryModel childCategory : categoryModel.getCategories()) {
      addCategory(childCategory, html);
    }

    for (ProcessModel processModel : categoryModel.getProcesses()) {
      addProcess(processModel, html);
    }

    html.append("</ul>\n</li>");
  }

  public static void addCategoryFilter(Long id, String name, StringBuilder html) {
    html.append("<a class=\"opensubheader subexpandable\" onclick='javascript: addCategoryFilter(\"").append(id).append("\");\'\n")
        .append("document.getElementById(\"processName\").innerHTML = \"").append(name).append(" ::\";table.setVisible(1,false);'>\n")
        .append("<i class=\"icon-chevron-down opensubheader pReload\"></i>")
        .append(name).append("\n")
        .append("</a>\n");
  }

  public static void addProcess(ProcessModel processModel, StringBuilder html) {
    html.append("<li>\n")
        .append("<a href='javascript: addBusinessProcessFilter(\"").append(processModel.getName()).append("\");\'\n")
        .append("document.getElementById(\"processName\").innerHTML = \"").append(processModel.getName()).append(" ::\"; table.setVisible(1,false);'>\n")
        .append("<i class=\"opensubheader pReload\"></i>").append(processModel.getName()).append("</a>\n")
        .append("<span class=\"pull-right\">").append(processModel.getTaskCount()).append("</span>\n")
        .append("</li>");
  }
}
