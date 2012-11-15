package ru.runa.gpd.form;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.lang.model.FormNode;

public abstract class FormType {
    public static final Integer READ_ACCESS = 1;
    public static final Integer WRITE_ACCESS = 2;
    public static final Integer DOUBTFUL = 3;
    
    private String type;
    private String name;
    
    public abstract IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException;

    public abstract String getFormFileName(IFile definitionFile, FormNode formNode);

    public abstract Map<String, Integer> getFormVariableNames(IFile formFile, FormNode formNode) throws Exception;

    public abstract void validate(IFile formFile, FormNode formNode);
    
    public String getType() {
        return type;
    }
    
    void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
    
}
