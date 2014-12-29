package ru.runa.gpd.form;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.lang.model.FormNode;

public abstract class FormType {
    private String type;
    private String name;
    private int order;

    /**
     * For deprecated form types.
     * 
     * @return
     */
    public boolean isCreationAllowed() {
        return true;
    }

    /**
     * Open form editor.
     */
    public abstract IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException;

    /**
     * Retrieve variables defined in form.
     */
    public abstract Map<String, FormVariableAccess> getFormVariableNames(IFile formFile, FormNode formNode) throws Exception;

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

    public int getOrder() {
        return order;
    }

    void setOrder(int order) {
        this.order = order;
    }
}
