package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;

@SuppressWarnings("unchecked")
public class DelegablePresentation implements VariableRenameProvider {

    private Delegable delegable;
    private final Document document = new Document();
    private final String name;

    public DelegablePresentation(final Delegable delegable, String name) {
        this.name = name;
        document.addDocumentListener(new IDocumentListener() {

            public void documentAboutToBeChanged(DocumentEvent de) {
            }

            public void documentChanged(DocumentEvent de) {
                delegable.setDelegationConfiguration(de.getDocument().get());
            }
        });
        setElement((GraphElement) delegable);
    }

    public void setElement(GraphElement element) {
        this.delegable = (Delegable) element;
    }

    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        document.set(delegable.getDelegationConfiguration());

        int offset = 0;
        MultiTextEdit multiEdit = new MultiTextEdit();
        int len = variableName.length();
        while (offset > -1) {
            if (offset >= document.getLength()) {
                break;
            }
            offset = document.search(offset, variableName, true, true, true);
            if (offset == -1) {
                break;
            }
            ReplaceEdit replaceEdit = new ReplaceEdit(offset, len, replacement);
            multiEdit.addChild(replaceEdit);
            offset += len; // to avoid overlapping
        }

        if (multiEdit.getChildrenSize() > 0) {
            DocumentChange change = new DocumentChangeExt(name, document);
            change.setEdit(multiEdit);
            changes.add(change);
        }
        return changes;
    }

    private class DocumentChangeExt extends DocumentChange {

        public DocumentChangeExt(String name, IDocument document) {
            super(name, document);
        }

        @Override
        public Object getAdapter(Class adapter) {
            if (adapter == TextEditChangeNode.class) {
                return new GPDChangeNode(this, delegable);
            }
            return super.getAdapter(adapter);
        }
    }
}
