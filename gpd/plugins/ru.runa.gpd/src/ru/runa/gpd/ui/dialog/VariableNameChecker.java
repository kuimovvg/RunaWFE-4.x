package ru.runa.gpd.ui.dialog;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

public class VariableNameChecker implements KeyListener {

    private final Text textField;

    public VariableNameChecker(Text textField) {
        this.textField = textField;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (textField.getCaretPosition() == 0) {
            if (!Character.isJavaIdentifierStart(e.character)) {
                e.doit = false;
            }
        } else {
            if (!Character.isJavaIdentifierPart(e.character)) {
                e.doit = false;
            }
        }
        if ('$' == e.character) {
            e.doit = false;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static boolean isNameValid(String name) {
        char[] chars = name.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    return false;
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    return false;
                }
            }
            if ('$' == chars[i]) {
                return false;
            }
        }
        return true;
    }

}
