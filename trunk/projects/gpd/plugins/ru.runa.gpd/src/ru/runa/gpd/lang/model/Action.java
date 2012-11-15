package ru.runa.gpd.lang.model;


import ru.runa.gpd.Localization;
import ru.runa.gpd.util.TypeNameMapping;

public class Action extends GraphElement implements Delegable {

    public String getDelegationType() {
        return ACTION_HANDLER;
    }
    
    public String getDisplayName() {
        String className = getDelegationClassName();
        if (className == null || className.length() == 0) {
            className = Localization.getString("label.new");
        }
		className = TypeNameMapping.getTypeName(className);
        int index = className.lastIndexOf(".");
        if (index > 0) {
            className = className.substring(index + 1);
        }
        return className;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
