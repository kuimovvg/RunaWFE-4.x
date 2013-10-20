package ru.runa.gpd.formeditor.ftl.view;

import org.eclipse.swt.graphics.Image;

/**
 * TODO replace with MethodTag
 */
public class ToolPalleteMethodTag {
    private String tagName;
    private String tagLabel;
    private String helpUrl;
    private Image image;

    public ToolPalleteMethodTag(String tagName, String tagLabel, String helpUrl, Image image) {
        this.tagName = tagName;
        this.tagLabel = tagLabel;
        this.helpUrl = helpUrl;
        this.image = image;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagLabel() {
        return tagLabel;
    }

    public void setTagLabel(String tagLabel) {
        this.tagLabel = tagLabel;
    }
    
    public String getHelpUrl() {
        return helpUrl;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}