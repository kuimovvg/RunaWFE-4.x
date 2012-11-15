package ru.runa.gpd.lang;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import ru.runa.gpd.SharedImages;

public class GEFPaletteEntry {
    private final String id;
    private final String categoryId;
    private final String type;
    private final String imageName;
    private final Bundle bundle;

    public GEFPaletteEntry(IConfigurationElement element) {
        id = element.getAttribute("id");
        categoryId = element.getAttribute("category");
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
