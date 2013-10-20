package ru.cg.runaex.components_plugin.handler;

import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.components_plugin.LocalizationImpl;

public final class HandlerFactory {
  private static Localization webserviceCallDescriptorLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.handler.WebserviceCallDescriptorLocalization");
  private static Localization startRemoteProcessDescriptorLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.handler.StartRemoteProcessDescriptorLocalization");
  private static Localization dbVariableHandlerDescriptorLocalization = new LocalizationImpl("ru.cg.runaex.components_plugin.handler.DBVariableHandlerDescriptorLocalization");  

  public static Localization getWebserviceCallDescriptorLocalization() {
    return webserviceCallDescriptorLocalization;
  }

  public static Localization getStartRemoteProcessDescriptorLocalization() {
    return startRemoteProcessDescriptorLocalization;
  }
  
  public static Localization getDBVariableHandlerDescriptorLocalization() {
	return dbVariableHandlerDescriptorLocalization; 
  }

}
