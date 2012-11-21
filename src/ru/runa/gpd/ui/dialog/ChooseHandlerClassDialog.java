package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;

import ru.runa.gpd.Localization;
import ru.runa.gpd.handler.Artifact;
import ru.runa.gpd.handler.HandlerRegistry;

public class ChooseHandlerClassDialog extends ChooseItemDialog {
    private String type;

    public ChooseHandlerClassDialog(String type) {
        super(Localization.getString("ChooseClass.title"), Localization.getString("ChooseClass.message"), false);
        this.type = type;
    }

    public String openDialog() {
        try {
            setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    return ((Artifact) element).getDisplayName();
                }
            });
            setItems(HandlerRegistry.getInstance().getAll(type, true));
            if (open() != IDialogConstants.CANCEL_ID) {
                return ((Artifact) getSelectedItem()).getName();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;
    }
}
