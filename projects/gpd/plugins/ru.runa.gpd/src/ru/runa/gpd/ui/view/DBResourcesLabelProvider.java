package ru.runa.gpd.ui.view;

import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Schema;
import org.apache.ddlutils.model.Table;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import ru.runa.gpd.SharedImages;

public class DBResourcesLabelProvider extends LabelProvider {
  @Override
  public String getText(Object element) {
    if (element instanceof String) {
      return element.toString();
    }
    if (element instanceof Schema) {
      return ((Schema) element).getName();
    }
    if (element instanceof Table) {
      return ((Table) element).getName();
    }

    if (element instanceof Column) {
      return ((Column) element).getName();
    }

    if (element instanceof IResource) {
      return ((IResource) element).getName();
    }
    return super.getText(element);
  }

  @Override
  public Image getImage(Object element) {

    if (element instanceof String) {
      return SharedImages.getImage("icons/project.gif");
    }
    if (element instanceof Schema) {
      return SharedImages.getImage("icons/database-schema.png");
    }
    if (element instanceof Table) {
      return SharedImages.getImage("icons/db_table.png");
    }

    if (element instanceof Column) {
      return SharedImages.getImage("icons/db-column.png");
    }

    return SharedImages.getImage("icons/project.gif");
  }
}
