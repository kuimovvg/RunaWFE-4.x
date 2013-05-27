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
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import ru.runa.gpd.lang.model.Delegable;

public class DelegablePresentation extends VariableRenameProvider<Delegable> {
    private final Document document = new Document();
    private final String name;

    public DelegablePresentation(final Delegable delegable, String name) {
        this.name = name;
        setElement(delegable);
        document.addDocumentListener(new IDocumentListener() {
            @Override
            public void documentAboutToBeChanged(DocumentEvent de) {
            }

            @Override
            public void documentChanged(final DocumentEvent de) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        delegable.setDelegationConfiguration(de.getDocument().get());
                    }
                });
            }
        });
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        document.set(element.getDelegationConfiguration());
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
                return new GPDChangeNode(this, element);
            }
            return super.getAdapter(adapter);
        }
    }
}
