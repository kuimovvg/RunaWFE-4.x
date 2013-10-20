package ru.runa.gpd.ui.view;

import org.eclipse.ui.views.properties.IPropertySource;

public interface IRequiredPropertiesSource extends IPropertySource {
	public boolean isPropertyRequired(Object propertyId);
}
