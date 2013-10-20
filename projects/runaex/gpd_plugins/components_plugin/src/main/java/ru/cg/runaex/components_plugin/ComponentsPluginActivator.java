package ru.cg.runaex.components_plugin;

import java.util.ResourceBundle;
import javax.validation.Validation;
import javax.validation.Validator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;


public class ComponentsPluginActivator extends Plugin {
  private static ResourceBundle resourceBundle;
  private static Validator validator;
  private static ComponentsPluginActivator plugin;

  public ComponentsPluginActivator() {
    plugin = this;
  }
  
  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);

    resourceBundle = getLocalization(context, getLocaleStr());
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  public ResourceBundle getLocalization(BundleContext context, String locale) {
    ServiceTracker localizationTracker = new ServiceTracker(context, BundleLocalization.class.getName(), null);
    localizationTracker.open();
    BundleLocalization location = (BundleLocalization) localizationTracker.getService();
    if (location != null)
      return location.getLocalization(context.getBundle(), locale);

    return null;
  }

  public static String getLocaleStr() {
    return Platform.getNL();
  }

  public static ResourceBundle getResourceBundle() {
    return resourceBundle;
  }

  public static Validator getValidator() {
    return validator;
  }

  public static void log(int severity, int code, String message, Throwable exception) {
    getDefault().getLog().log(new Status(severity, getDefault().getBundle().getSymbolicName(), code, message, exception));
  }

  public static void logInfo(String message) {
    log(IStatus.INFO, IStatus.OK, message, null);
  }

  public static void logError(String message, Throwable exception) {
    log(IStatus.ERROR, IStatus.OK, message, exception);
  }

  public static ComponentsPluginActivator getDefault() {
    return plugin;
  }

}
