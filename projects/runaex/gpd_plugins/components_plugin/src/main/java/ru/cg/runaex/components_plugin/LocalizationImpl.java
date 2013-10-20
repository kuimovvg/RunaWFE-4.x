package ru.cg.runaex.components_plugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocalizationImpl implements Localization {
  private ResourceBundle bundle;

  public LocalizationImpl(String messageBundle) {
    bundle = ResourceBundle.getBundle(messageBundle);
  }

  @Override
  public String get(String key) {
    try {
      return bundle.getString(key);
    }
    catch (MissingResourceException e) {
      System.out.println("Missed localization: '" + key + "'");
      return key;
    }
  }

}
