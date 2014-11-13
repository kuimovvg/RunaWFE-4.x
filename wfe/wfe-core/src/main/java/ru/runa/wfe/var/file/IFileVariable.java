package ru.runa.wfe.var.file;

import java.io.Serializable;

/**
 * Represents file variable value.
 * 
 * @author dofs
 * @since 4.2.0
 */
public interface IFileVariable extends Serializable {

    /**
     * @return file data
     */
    public byte[] getData();

    /**
     * @return file name
     */
    public String getName();

    /**
     * @return mime type
     */
    public String getContentType();
}
