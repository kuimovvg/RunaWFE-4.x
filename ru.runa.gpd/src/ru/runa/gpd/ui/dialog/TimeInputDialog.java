package ru.runa.gpd.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.gpd.Localization;

public class TimeInputDialog extends UserInputDialog {

    public TimeInputDialog(String initialValue) {
        super(Localization.getString("BSH.InputTime"), initialValue);
    }

    @Override
    protected void postCreation() {
        label.setText(Localization.getString("format") + ": HH:MM");
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            new SimpleDateFormat("H:mm").parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
