package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;

/**
 * This class eliminates byte[] data transferring without usage.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableProxy extends FileVariable {
    private static final long serialVersionUID = 1L;
    private User user;
    private Long processId;
    private String variableName;

    public FileVariableProxy() {
    }

    public FileVariableProxy(User user, Long processId, String variableName, FileVariable fileVariable) {
        super(fileVariable.getName(), null, fileVariable.getContentType());
        this.user = user;
        this.processId = processId;
        this.variableName = variableName;
    }

    @Override
    public byte[] getData() {
        if (super.getData() == null) {
            setData(Delegates.getExecutionService().getFileVariableValue(user, processId, variableName));
        }
        return super.getData();
    }

}
