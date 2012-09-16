package ru.runa.bpm.ui.editor.ltk;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.swt.graphics.Image;
import ru.runa.bpm.ui.common.ElementEntry;
import ru.runa.bpm.ui.common.model.GraphElement;

public class GPDChangeNode extends TextEditChangeNode {
    private final ElementEntry elementEntry;
    private final String notation;
    private String label;

    public GPDChangeNode(TextEditBasedChange change, Object element) {
        super(change);
        if (element != null && element instanceof GraphElement) {
            GraphElement graphElement = (GraphElement) element;
            elementEntry = graphElement.getTypeDefinition().getEntry();
            notation = graphElement.getProcessDefinition().getNotation();
        } else {
            elementEntry = null;
            notation = null;
        }
    }

    public GPDChangeNode(TextEditBasedChange change, Object element, String label) {
        this(change, element);
        this.label = label;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        if (elementEntry != null) {
            return elementEntry.getImageDescriptor(notation);
        }
        return super.getImageDescriptor();
    }

    public Image getImage() {
        if (elementEntry != null) {
            return elementEntry.getImage(notation);
        }
        return null;
    }

    @Override
    public String getText() {
        if (label != null) {
            return label;
        }
        return super.getText();
    }
}
