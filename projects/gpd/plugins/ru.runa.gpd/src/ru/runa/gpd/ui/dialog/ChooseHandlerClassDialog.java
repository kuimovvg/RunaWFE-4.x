package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.Artifact;
import ru.runa.gpd.extension.HandlerRegistry;

public class ChooseHandlerClassDialog extends ChooseItemDialog {
    private String type;
    private String defaultClassName;

    public ChooseHandlerClassDialog(String type, String defaultClassName) {
        super(Localization.getString("ChooseClass.title"), Localization.getString("ChooseClass.message"), false);
        this.type = type;
        this.defaultClassName = defaultClassName;
    }

    public String openDialog() {
        try {
            setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((Artifact) element).getLabel();
                }
            });
            setItems(HandlerRegistry.getInstance().getAll(type, true));
            if (defaultClassName != null) {
                setSelectedItem(HandlerRegistry.getInstance().getArtifact(defaultClassName));
            }
            if (open() != IDialogConstants.CANCEL_ID) {
                return ((Artifact) getSelectedItem()).getName();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;
    }
}
