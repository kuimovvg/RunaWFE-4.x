package ru.runa.gpd.formeditor.ftl;

import java.io.IOException;

import ru.runa.gpd.EditorsPlugin;

public class DefaultTagImageProvider extends DynaTagImageProvider {

    @Override
    public byte[] getImage(MethodTag tag, String[] parameters) throws IOException {
        byte[] data = EditorsPlugin.loadTagImage(tag.getBundle(), "metadata/icons/" + tag.id + ".png");
        if (data == null) {
            data = super.getImage(tag, parameters);
        }
        return data;
    }

}
