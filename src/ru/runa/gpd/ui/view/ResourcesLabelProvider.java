package ru.runa.gpd.ui.view;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.SharedImages;

public class ResourcesLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof IResource) {
            return ((IResource) element).getName();
        }
        return super.getText(element);
    }

    @Override
    public Image getImage(Object element) {
        if (element instanceof IProject) {
            return SharedImages.getImage("icons/project.gif");
        }
        return SharedImages.getImage("icons/process.gif");
    }
}
