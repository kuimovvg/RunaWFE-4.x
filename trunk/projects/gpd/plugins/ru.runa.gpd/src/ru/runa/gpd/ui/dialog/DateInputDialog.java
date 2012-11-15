package ru.runa.gpd.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.gpd.Localization;

public class DateInputDialog extends UserInputDialog {

    public DateInputDialog(String initialValue) {
        super(Localization.getString("BSH.InputDate"), initialValue);
    }

    @Override
    protected void postCreation() {
        label.setText(Localization.getString("format") + ": dd.MM.yyyy");
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            new SimpleDateFormat("dd.MM.yyyy").parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
