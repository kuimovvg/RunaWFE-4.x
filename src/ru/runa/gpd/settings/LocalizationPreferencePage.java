package ru.runa.gpd.settings;

import ru.runa.gpd.handler.Artifact;
import ru.runa.gpd.handler.LocalizationRegistry;

public class LocalizationPreferencePage extends ArtifactPreferencePage<Artifact> {
    public LocalizationPreferencePage() {
        super(LocalizationRegistry.getInstance());
    }
}
