package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;

public class FormNodePresentation extends VariableRenameProvider<FormNode> {
    private final IFolder folder;

    public FormNodePresentation(IFolder folder, FormNode formNode) {
        this.folder = folder;
        setElement(formNode);
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        CompositeChange result = new CompositeChange(element.getName());
        if (element.hasForm()) {
            IFile file = folder.getFile(element.getFormFileName());
            result.addAll(processFile(file, Localization.getString("Search.formNode.form"), variableName, replacement));
        }
        if (element.hasFormValidation()) {
            IFile file = folder.getFile(element.getValidationFileName());
            result.addAll(processFile(file, Localization.getString("Search.formNode.validation"), variableName, replacement));
        }
        if (result.getChildren().length > 0) {
            return Arrays.asList((Change) result);
        }
        return new ArrayList<Change>();
    }

    private Change[] processFile(IFile file, final String label, String variableName, String replacement) throws Exception {
        Document document = new Document();
        String text = IOUtils.readStream(file.getContents());
        document.set(text);
        List<Change> changes = new ArrayList<Change>();
        int offset = 0; // TODO in forms use "\" + varName + "\"
        MultiTextEdit multiEdit = new MultiTextEdit();
        int len = variableName.length();
        while (offset > -1) {
            offset = document.search(offset, variableName, true, true, true);
            if (offset == -1) {
                break;
            }
            ReplaceEdit replaceEdit = new ReplaceEdit(offset, len, replacement);
            multiEdit.addChild(replaceEdit);
            offset += len; // to avoid overlapping
        }
        if (multiEdit.getChildrenSize() > 0) {
            TextFileChange fileChange = new TextFileChange(file.getName(), file) {
                @Override
                public Object getAdapter(Class adapter) {
                    if (adapter == TextEditChangeNode.class) {
                        return new GPDChangeNode(this, element, label);
                    }
                    return super.getAdapter(adapter);
                }
            };
            fileChange.setEdit(multiEdit);
            changes.add(fileChange);
        }
        return changes.toArray(new Change[changes.size()]);
    }
}
