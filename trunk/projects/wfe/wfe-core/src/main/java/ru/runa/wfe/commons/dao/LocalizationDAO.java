package ru.runa.wfe.commons.dao;

import java.util.Map;

/**
 * DAO for managing {@link Localization}.
 * 
 * @author dofs
 * @since 4.0
 */
public class LocalizationDAO extends GenericDAO<Localization> {

    private Localization get(String name) {
        return findFirstOrNull("from Localization where name = ?", name);
    }

    /**
     * Load localized value.
     * 
     * @param name
     *            key
     * @return localized value or key if no localization exists
     */
    public String getLocalized(String name) {
        Localization localization = get(name);
        if (localization == null) {
            return name;
        }
        return localization.getValue();
    }

    /**
     * Save localizations.
     * 
     * @param localizations
     *            localizations
     * @param rewrite
     *            rewrite existing localization
     */
    public void saveLocalizations(Map<String, String> localizations, boolean rewrite) {
        for (Map.Entry<String, String> entry : localizations.entrySet()) {
            saveLocalization(entry.getKey(), entry.getValue(), rewrite);
        }
    }

    /**
     * Save localization.
     * 
     * @param name
     *            key
     * @param value
     *            localized value
     * @param rewrite
     *            rewrite existing localization
     */
    public void saveLocalization(String name, String value, boolean rewrite) {
        Localization localization = get(name);
        if (localization == null) {
            create(new Localization(name, value));
        } else if (rewrite) {
            localization.setValue(value);
            update(localization);
        }
    }

}
