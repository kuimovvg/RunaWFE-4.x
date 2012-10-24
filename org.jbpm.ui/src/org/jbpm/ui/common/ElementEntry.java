package org.jbpm.ui.common;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.jbpm.ui.SharedImages;
import org.osgi.framework.Bundle;

public class ElementEntry {
    private final String id;
    private final String categoryId;
    private final String label;
    private final String type;
    private final String imageName;
    private final Bundle bundle;

    public ElementEntry(IConfigurationElement element) {
        id = element.getAttribute("id");
        categoryId = element.getAttribute("category");
        label = element.getAttribute("label");
        type = element.getAttribute("type");
        imageName = element.getAttribute("icon");
        bundle = Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }
    
    public String getImageName() {
        return imageName;
    }

    public ImageDescriptor getImageDescriptor(String notation) {
        return SharedImages.getImageDescriptor(bundle, "icons/" + notation + "/palette/" + imageName, true);
    }

    public Image getImage(String notation) {
        return SharedImages.getImage(bundle, "icons/" + notation + "/palette/" + imageName);
    }

}
