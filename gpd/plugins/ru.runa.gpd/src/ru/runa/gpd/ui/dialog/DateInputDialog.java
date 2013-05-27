package ru.runa.gpd.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.PrefConstants;

public class DateInputDialog extends UserInputDialog {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Activator.getPrefString(PrefConstants.P_DATE_FORMAT_PATTERN));

    public DateInputDialog() {
        super(Localization.getString("InputDate"));
    }

    @Override
    protected void postCreation() {
        label.setText(Localization.getString("format") + ": " + dateFormat.toPattern());
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            dateFormat.parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
