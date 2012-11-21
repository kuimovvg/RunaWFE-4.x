package ru.runa.gpd.settings;

import ru.runa.gpd.handler.VariableFormatArtifact;
import ru.runa.gpd.handler.VariableFormatRegistry;

public class VariableFormatPreferencePage extends ArtifactPreferencePage<VariableFormatArtifact> {
    public VariableFormatPreferencePage() {
        super(VariableFormatRegistry.getInstance());
    }
}
