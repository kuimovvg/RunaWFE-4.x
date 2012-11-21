package ru.runa.gpd.settings;

import ru.runa.gpd.handler.HandlerArtifact;
import ru.runa.gpd.handler.HandlerRegistry;

public class HandlerPreferencePage extends ArtifactPreferencePage<HandlerArtifact> {
    public HandlerPreferencePage() {
        super(HandlerRegistry.getInstance());
    }
}
