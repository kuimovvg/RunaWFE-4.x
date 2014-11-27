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
     * @return file name
     */
    public String getName();

    /**
     * @return mime type
     */
    public String getContentType();

    /**
     * @return file data
     */
    public byte[] getData();

    /**
     * @return string representation of external storage, can be
     *         <code>null</code>
     */
    public String getStringValue();

}
