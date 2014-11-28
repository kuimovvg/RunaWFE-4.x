package ru.runa.wfe.var.file;

/**
 * Used as transfer object containing external storage identifier.
 * 
 * @author dofs
 * @since 4.2.0
 */
public class FileVariableIdentifier implements IFileVariable {
    private static final long serialVersionUID = 1L;
    private String stringValue;

    public FileVariableIdentifier() {
    }

    public FileVariableIdentifier(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

}
