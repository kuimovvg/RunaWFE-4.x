package ru.runa.bpm.ui.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.runa.bpm.ui.DesignerLogger;

public abstract class PhraseDecliner {
    protected static Map<String, Class<? extends PhraseDecliner>> decliners = new HashMap<String, Class<? extends PhraseDecliner>>();
    static {
        decliners.put("ru", PhraseDecliner_ru.class);
    }

    public static PhraseDecliner getDecliner() {
        String lang = Locale.getDefault().getLanguage();
        try {
            Class<? extends PhraseDecliner> declainerClass = decliners.get(lang);
            return declainerClass.newInstance();
        } catch (Throwable e) {
            DesignerLogger.logErrorWithoutDialog("Unable to create decliner " + lang, e);
            return null;
        }
    }
    
    public abstract String declineDuration(String delay, String unit);
    
}
