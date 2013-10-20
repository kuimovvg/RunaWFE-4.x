package ru.cg.runaex.components_plugin.handler;

import java.util.HashMap;
import java.util.Map;
import javax.wsdl.WSDLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import org.jdom2.JDOMException;

import ru.cg.runaex.runa_ext.handler.webservice_call_handler.WebserviceCallConfiguration;
import ru.cg.runaex.runa_ext.handler.webservice_call_handler.WebserviceCallConfigurationHelper;
import ru.cg.runaex.components_plugin.ComponentsPluginActivator;
import ru.cg.runaex.components_plugin.Localization;
import ru.cg.runaex.wsdl_analyzer.bean.OperationInfo;
import ru.cg.runaex.wsdl_analyzer.bean.ServiceInfo;
import ru.cg.runaex.wsdl_analyzer.builder.ComponentBuilder;

public class WebserviceCallDescriptor extends DelegableProvider {

  @Override
  protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
    return new WebserviceCallHandlerConfigurationDialog(delegable.getDelegationConfiguration());
  }

  private static class WebserviceCallHandlerConfigurationDialog extends DelegableConfigurationDialog {
    private Text wsdlUri;
    private Text esbRelativeUrl;
    private Combo servicesCombo;
    private Combo operationsCombo;
    private StyledText requestText;
    private StyledText responseText;

    private TabFolder messagesFolder;
    private TabItem requestTab;

    private Button analyzeWsdl;

    private WebserviceCallConfiguration initialConfiguration;
    private Map<String, ServiceInfo> services = new HashMap<String, ServiceInfo>();
    private Map<String, OperationInfo> currentServiceOperations = new HashMap<String, OperationInfo>();


    public WebserviceCallHandlerConfigurationDialog(String initialValue) {
      super(initialValue);

      if (initialValue != null && !initialValue.trim().isEmpty()) {
        try {
          initialConfiguration = WebserviceCallConfigurationHelper.parse(initialValue);
        }
        catch (JDOMException e) {
          ComponentsPluginActivator.logError("Could not parse webservice call configuration. Configuration: " + initialValue, e);
        }
        catch (Exception e) {
          ComponentsPluginActivator.logError("Could not parse webservice call configuration. Configuration: " + initialValue, e);
        }
      }
    }

    @Override
    protected Point getInitialSize() {
      return new Point(700, 500);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      getShell().setText(title);

      Localization localization = HandlerFactory.getWebserviceCallDescriptorLocalization();

      Composite dialog = new Composite(parent, SWT.NONE);
      dialog.setLayout(new GridLayout(3, false));
      dialog.setLayoutData(new GridData(GridData.FILL_BOTH));

      Label wsdlLabel = new Label(dialog, SWT.NONE);
      wsdlLabel.setText(localization.get("wsdlUri"));

      wsdlUri = new Text(dialog, SWT.BORDER);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      wsdlUri.setLayoutData(gridData);
      wsdlUri.setText("");

      analyzeWsdl = new Button(dialog, SWT.PUSH);
      analyzeWsdl.setText(localization.get("analyzeWsdlButton"));
      analyzeWsdl.setLayoutData(new GridData());
      analyzeWsdl.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onAnalyzeWsdl();
        }
      });

      Label esbRelativeUrlLabel = new Label(dialog, SWT.NONE);
      esbRelativeUrlLabel.setText(localization.get("esbRelativeUrl"));

      esbRelativeUrl = new Text(dialog, SWT.BORDER);
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      esbRelativeUrl.setLayoutData(gridData);
      esbRelativeUrl.setText("");

      Label servicesLabel = new Label(dialog, SWT.NONE);
      servicesLabel.setText(localization.get("service"));
      gridData = new GridData();
      gridData.verticalAlignment = SWT.TOP;
      servicesLabel.setLayoutData(gridData);

      servicesCombo = new Combo(dialog, SWT.NONE);
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      servicesCombo.setLayoutData(gridData);
      servicesCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onServiceSelected();
        }
      });

      Label operationsLabel = new Label(dialog, SWT.NONE);
      operationsLabel.setText(localization.get("operation"));
      gridData = new GridData();
      gridData.verticalAlignment = SWT.TOP;
      operationsLabel.setLayoutData(gridData);

      operationsCombo = new Combo(dialog, SWT.NONE);
      gridData = new GridData();
      gridData.horizontalSpan = 2;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      operationsCombo.setLayoutData(gridData);
      operationsCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          onOperationSelected();
        }
      });

      messagesFolder = new TabFolder(dialog, SWT.NONE);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.horizontalSpan = 3;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      messagesFolder.setLayoutData(gridData);

      requestTab = new TabItem(messagesFolder, SWT.NONE);
      requestTab.setText(localization.get("requestTabHeader"));
      Composite composite = new Composite(messagesFolder, SWT.NONE);
      composite.setLayout(new FillLayout(SWT.VERTICAL));
      requestText = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      requestTab.setControl(composite);

      TabItem responseTab = new TabItem(messagesFolder, SWT.NONE);
      responseTab.setText(localization.get("responseTabHeader"));
      composite = new Composite(messagesFolder, SWT.NONE);
      composite.setLayout(new FillLayout(SWT.VERTICAL));

      responseText = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      responseTab.setControl(composite);

      initValues();

      return dialog;
    }

    private void onAnalyzeWsdl() {
      ComponentBuilder builder = new ComponentBuilder();
      try {
        services = builder.buildComponents(wsdlUri.getText());
        currentServiceOperations.clear();

        servicesCombo.removeAll();
        operationsCombo.removeAll();
        requestText.setText("");
        responseText.setText("");
        messagesFolder.setSelection(requestTab);

        String[] serviceNames = new String[services.keySet().size()];
        services.keySet().toArray(serviceNames);
        servicesCombo.setItems(serviceNames);

        if (serviceNames.length > 0) {
          servicesCombo.select(0);
          onServiceSelected();
        }
      }
      catch (WSDLException ex) {
        ComponentsPluginActivator.logError("Could not analyze wsdl " + wsdlUri.getText(), ex);
      }
      catch (Exception ex) {
        ComponentsPluginActivator.logError("Could not analyze wsdl " + wsdlUri.getText(), ex);
      }
    }

    private void onServiceSelected() {
      int selection = servicesCombo.getSelectionIndex();
      if (selection != -1) {
        String serviceName = servicesCombo.getItem(selection);
        ServiceInfo service = services.get(serviceName);

        currentServiceOperations = service.getOperationsByName();
        String[] operationsArray = new String[currentServiceOperations.keySet().size()];
        currentServiceOperations.keySet().toArray(operationsArray);
        operationsCombo.setItems(operationsArray);

        if (operationsArray.length > 0) {
          operationsCombo.select(0);
          onOperationSelected();
        }
      }
      else {
        currentServiceOperations.clear();
        operationsCombo.removeAll();
        requestText.setText("");
        responseText.setText("");
      }
    }

    private void onOperationSelected() {
      int selection = operationsCombo.getSelectionIndex();
      if (selection != -1) {
        String operationName = operationsCombo.getItem(selection);
        OperationInfo operation = currentServiceOperations.get(operationName);

        requestText.setText(operation.getInputMessageStub());
        responseText.setText(operation.getOutputMessageStub());
      }
      else {
        requestText.setText("");
        responseText.setText("");
      }
    }

    public void initValues() {
      if (initialConfiguration != null) {
        esbRelativeUrl.setText(initialConfiguration.getEsbRelativeUrl());
        servicesCombo.setText(initialConfiguration.getService());
        operationsCombo.setText(initialConfiguration.getOperation());
        requestText.setText(initialConfiguration.getRequest());
        responseText.setText(initialConfiguration.getResponse());
      }
    }

//    @Override
    protected String assembleResult() {
      WebserviceCallConfiguration configuration = new WebserviceCallConfiguration();
      configuration.setEsbRelativeUrl(esbRelativeUrl.getText());
      configuration.setService(servicesCombo.getText());
      configuration.setOperation(operationsCombo.getText());
      configuration.setRequest(requestText.getText());
      configuration.setResponse(responseText.getText());

      return WebserviceCallConfigurationHelper.serializeToString(configuration);
    }
  }
}
