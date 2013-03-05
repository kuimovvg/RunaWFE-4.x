package ru.runa.wfe.var;

/**
 * This class eliminates need to persist large bytes array in database.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableDescriptor extends FileVariable {
    private static final long serialVersionUID = 1L;
    private String variablePath;
    private transient byte[] localData;

    public FileVariableDescriptor() {
    }

    public FileVariableDescriptor(Variable<?> variable, FileVariable fileVariable) {
        super(fileVariable.getName(), null, fileVariable.getContentType());
        long version = variable.getVersion() != null ? variable.getVersion() + 1 : 0;
        variablePath = variable.getProcess().getId() + "/" + variable.getName() + "/" + version;
        localData = fileVariable.getData();
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public byte[] getData() {
        if (super.getData() == null) {
            return localData;
        }
        return super.getData();
    }

}
