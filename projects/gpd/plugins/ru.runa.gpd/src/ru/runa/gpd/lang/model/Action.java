package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;
import ru.runa.gpd.handler.HandlerArtifact;
import ru.runa.gpd.handler.LocalizationRegistry;

public class Action extends GraphElement implements Delegable {
    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    public String getDisplayName() {
        String className = getDelegationClassName();
        if (className == null || className.length() == 0) {
            className = Localization.getString("label.new");
        }
        return LocalizationRegistry.getDisplayName(className);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
