package ru.runa.gpd.extension;

import org.dom4j.Element;

public class HandlerContentProvider extends ArtifactContentProvider<HandlerArtifact> {
    private static final String CONFIGURER_ATTR = "configurer";
    private static final String TYPE_ATTR = "type";

    @Override
    protected HandlerArtifact createArtifact() {
        return new HandlerArtifact();
    }

    @Override
    protected void loadArtifact(HandlerArtifact artifact, Element element) {
        super.loadArtifact(artifact, element);
        artifact.setType(element.attributeValue(TYPE_ATTR));
        artifact.setConfigurerClassName(element.attributeValue(CONFIGURER_ATTR));
    }

    @Override
    protected void saveArtifact(HandlerArtifact artifact, Element element) {
        super.saveArtifact(artifact, element);
        element.addAttribute(TYPE_ATTR, artifact.getType());
        element.addAttribute(CONFIGURER_ATTR, artifact.getConfigurerClassName());
    }
}
