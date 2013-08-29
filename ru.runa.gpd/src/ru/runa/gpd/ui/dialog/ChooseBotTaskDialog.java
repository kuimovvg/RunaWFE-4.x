package ru.runa.gpd.ui.dialog;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.Localization;

public class ChooseBotTaskDialog extends ChooseItemDialog {
    private final List<String> botTaskNames;

    public ChooseBotTaskDialog(List<String> botTaskNames) {
        super(Localization.getString("ChooseBotTask.title"), Localization.getString("ChooseBotTask.title"), false);
        this.botTaskNames = botTaskNames;
    }

    public String openDialog() {
        try {
            Collections.sort(botTaskNames);
            setItems(botTaskNames);
            if (open() != IDialogConstants.CANCEL_ID) {
                return (String) getSelectedItem();
            }
        } catch (Exception e) {
            // ignore this and return null;
        }
        return null;
    }
}
