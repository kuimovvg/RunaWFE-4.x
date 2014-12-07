package ru.runa.gpd.formeditor.ftl;

import java.io.IOException;

public interface ITagImageProvider {

    public byte[] getImage(MethodTag tag, String[] parameters) throws IOException;

}
