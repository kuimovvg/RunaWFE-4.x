package org.jbpm.ui.view;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jbpm.ui.SharedImages;

public class ResourcesLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.getName();
        } else if (element instanceof IFolder) {
            IFolder folder = (IFolder) element;
            return folder.getName();
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
