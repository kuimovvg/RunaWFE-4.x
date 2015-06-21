package ru.runa.wfe.var.format;

/**
 * Text format for string representable as text areas.
 * 
 * @author dofs
 * @since 4.0
 */
public class TextFormat extends StringFormat implements VariableDisplaySupport {

    @Override
    public String getName() {
        return "text";
    }

    /**
     * FIXME: need or need't formatting?
     * 
     * @Override public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) { return super.formatHtml(user,
     *           webHelper, processId, name, object).replaceAll("\n", "<br>
     *           "); }
     */

}
