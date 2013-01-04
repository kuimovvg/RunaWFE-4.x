package ru.runa.wfe.var.format;

/**
 * Text format for string representable as text areas.
 * 
 * @author dofs
 * @since 4.0
 */
public class TextFormat extends StringFormat {

    @Override
    public String format(String obj) {
        if (obj == null) {
            return "";
        }
        String string = obj;
        string = string.replaceAll("\n", "<br>");
        string = string.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        string = string.replaceAll(" ", "&nbsp;");
        // TODO
        // "<div style='display: block; padding-left: 5px; background-color: #FFC; border-color: #FC6;'>"
        // +
        return string;
    }

}
