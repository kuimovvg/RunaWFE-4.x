package ru.runa.wfe.service.client;

import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.file.LocalFileSystemVariable;

/**
 * This class eliminates byte[] data transferring without usage.
 * 
 * @author dofs
 * @since 4.0
 */
public class FileVariableProxy implements IFileVariable {
    private static final long serialVersionUID = 1L;
    private User user;
    private Long processId;
    private String variableName;
    private String name;
    private String contentType;
    private byte[] data;
    // TODO remove this
    private String variablePath;

    public FileVariableProxy() {
    }

    public FileVariableProxy(User user, Long processId, String variableName, IFileVariable fileVariable) {
        name = fileVariable.getName();
        contentType = fileVariable.getContentType();
        this.user = user;
        this.processId = processId;
        this.variableName = variableName;
        if (fileVariable instanceof LocalFileSystemVariable) {
            variablePath = ((LocalFileSystemVariable) fileVariable).getVariablePath();
        }
    }

    public String getVariablePath() {
        return variablePath;
    }

    @Override
    public byte[] getData() {
        if (data == null) {
            data = Delegates.getExecutionService().getFileVariableValue(user, processId, variableName);
        }
        return data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
