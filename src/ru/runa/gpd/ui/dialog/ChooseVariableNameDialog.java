package ru.runa.gpd.ui.dialog;

import java.util.List;

import ru.runa.gpd.Localization;

public class ChooseVariableNameDialog extends ChooseItemDialog<String> {

    public ChooseVariableNameDialog(List<String> variableNames) {
        super(Localization.getString("ChooseVariable.title"), variableNames, true, Localization.getString("ChooseVariable.message"), true);
    }

}
