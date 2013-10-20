package ru.cg.runaex.components_plugin.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.DelegableConfigurationDialog;

import ru.cg.runaex.runa_ext.handler.start_remote_process.StartRemoteProcessConfiguration;
import ru.cg.runaex.runa_ext.handler.start_remote_process.StartRemoteProcessConfigurationParser;
import ru.cg.runaex.runa_ext.handler.start_remote_process.StartRemoteProcessParameter;
import ru.cg.runaex.components_plugin.ComponentsPluginActivator;
import ru.cg.runaex.components_plugin.Localization;

public class StartRemoteProcessDescriptor extends DelegableProvider {


  @Override
  protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
    return new StartRemoteProcessConfigurationDialog(delegable.getDelegationConfiguration());
  }


  private static class StartRemoteProcessConfigurationDialog extends DelegableConfigurationDialog {
    private Text endpointName;
    private Text processName;
    private ParametersPanel requestPanel;
    private ParametersPanel responsePanel;

    private TabFolder parametersTabFolder;
    private TabItem requestTab;

    private StartRemoteProcessConfiguration initialConfiguration;

    public StartRemoteProcessConfigurationDialog(String initialValue) {
      super(initialValue);

      if (initialValue != null && !initialValue.trim().isEmpty()) {
        try {
          initialConfiguration = StartRemoteProcessConfigurationParser.parse(initialValue);
        }
        catch (Exception e) {
          ComponentsPluginActivator.logError("Could not parse webservice call configuration. Configuration: " + initialValue, e);
        }
      }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      getShell().setText(title);

      Localization localization = HandlerFactory.getStartRemoteProcessDescriptorLocalization();

      Composite dialog = new Composite(parent, SWT.NONE);
      dialog.setLayout(new GridLayout(2, false));
      dialog.setLayoutData(new GridData(GridData.FILL_BOTH));


      Label endpointNameLabel = new Label(dialog, SWT.NONE);
      endpointNameLabel.setText(localization.get("endpointName"));

      endpointName = new Text(dialog, SWT.BORDER);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      endpointName.setLayoutData(gridData);
      endpointName.setText("");

      Label processNameLabel = new Label(dialog, SWT.NONE);
      processNameLabel.setText(localization.get("processName"));

      processName = new Text(dialog, SWT.BORDER);
      gridData = new GridData();
      gridData.horizontalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      processName.setLayoutData(gridData);
      processName.setText("");


      parametersTabFolder = new TabFolder(dialog, SWT.NONE);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.horizontalSpan = 3;
      gridData.horizontalAlignment = SWT.FILL;
      gridData.verticalAlignment = SWT.FILL;
      gridData.grabExcessHorizontalSpace = true;
      parametersTabFolder.setLayoutData(gridData);

      requestTab = new TabItem(parametersTabFolder, SWT.NONE);
      requestTab.setText(localization.get("requestTabHeader"));
      requestPanel = new ParametersPanel(parametersTabFolder, localization, SWT.NONE);
      requestTab.setControl(requestPanel);

      TabItem responseTab = new TabItem(parametersTabFolder, SWT.NONE);
      responseTab.setText(localization.get("responseTabHeader"));
      responsePanel = new ParametersPanel(parametersTabFolder, localization, SWT.NONE);
      responseTab.setControl(responsePanel);

      initValues();

      return dialog;
    }

    public void initValues() {
      if (initialConfiguration != null) {
    	if(initialConfiguration.getEndpointName() != null)
    	  endpointName.setText(initialConfiguration.getEndpointName());
    	if(initialConfiguration.getProcessName() != null)
          processName.setText(initialConfiguration.getProcessName());
        requestPanel.setParameters(initialConfiguration.getRequestParameters());
        responsePanel.setParameters(initialConfiguration.getResponseParameters());
      }
    }

//    @Override
    protected String assembleResult() {
      StartRemoteProcessConfiguration configuration = new StartRemoteProcessConfiguration();
      configuration.setEndpointName(StringUtils.trimToNull(endpointName.getText()));
      configuration.setProcessName(StringUtils.trimToNull(processName.getText()));
      configuration.setRequestParameters(requestPanel.getParameters());
      configuration.setResponseParameters(responsePanel.getParameters());


      return StartRemoteProcessConfigurationParser.serializeToString(configuration);
    }
  }

  private static class ParametersPanel extends Composite {
    private TableViewer parametersViewer;
    private List<StartRemoteProcessParameter> parameters;

    private Localization localization;

    private final String NAME_COLUMN = "name";
    private final String SOURCE_COLUMN = "source";

    private String[] columnNames = new String[] {
        NAME_COLUMN,
        SOURCE_COLUMN
    };

    public ParametersPanel(Composite parent, Localization localization, int style) {
      super(parent, style);
      this.localization = localization;

      parameters = new ArrayList<StartRemoteProcessParameter>();

      this.setLayout(new GridLayout(1, false));
      this.setLayoutData(new GridData(GridData.FILL_BOTH));

      createParametersControls();
      createParametersTable();
    }

    private void createParametersControls() {
      Composite buttonPanel = new Composite(this, SWT.NONE);
      RowLayout layout = new RowLayout(SWT.NONE);
      layout.wrap = true;
      layout.pack = true;
      layout.justify = false;
      layout.type = SWT.HORIZONTAL;
      buttonPanel.setLayout(layout);

      Button add = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
      add.setText(localization.get("addButton"));
      add.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          StartRemoteProcessParameter parameter = new StartRemoteProcessParameter();
          parameter.setName("");
          parameter.setSource("");
          parameters.add(parameter);

          parametersViewer.add(parameter);

//          TableItem tableItem = parametersViewer.getTable().getItem(parameters.size() -1);
//          tableItem.setBackground(new Color(getDisplay(), 1, 1, 1));

          parametersViewer.editElement(parameter, 0);
        }
      });

      Button remove = new Button(buttonPanel, SWT.PUSH | SWT.CENTER);
      remove.setText(localization.get("removeButton"));
      remove.addSelectionListener(new SelectionAdapter() {
        public void widgetSelected(SelectionEvent e) {
          StartRemoteProcessParameter parameter = (StartRemoteProcessParameter) ((IStructuredSelection) parametersViewer.getSelection()).getFirstElement();

          if (parameter != null) {
            parametersViewer.remove(parameter);
            parameters.remove(parameter);
          }
        }
      });
    }

    private void createParametersTable() {
      Table parametersTable = new Table(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
      parametersTable.setLinesVisible(true);
      parametersTable.setHeaderVisible(true);
//      parametersTable.setBackground(getDisplay().getSystemColor(SWT.COLOR_GRAY));

      GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
      data.heightHint = 200;
      parametersTable.setLayoutData(data);

      TableColumn column = new TableColumn(parametersTable, SWT.NONE, 0);
      column.setText(localization.get("nameColumnHeader"));
      column.setWidth(300);

      column = new TableColumn(parametersTable, SWT.FILL, 1);
      column.setText(localization.get("sourceColumnHeader"));
      column.setWidth(330);

      parametersViewer = new TableViewer(parametersTable);
      parametersViewer.setUseHashlookup(true);
      parametersViewer.setColumnProperties(columnNames);

      CellEditor[] editors = new CellEditor[columnNames.length];
//			((Text) textEditor.getControl()).setTextLimit(60);
      editors[0] = new TextCellEditor(parametersTable);
      editors[1] = new TextCellEditor(parametersTable);

      parametersViewer.setCellEditors(editors);
      parametersViewer.setCellModifier(new ParameterCellModifier(columnNames, parametersViewer));

      parametersViewer.setLabelProvider(new ParameterLabelProvider());
      parametersViewer.setContentProvider(new ArrayContentProvider());
      parametersViewer.setInput(parameters);
    }

    public List<StartRemoteProcessParameter> getParameters() {
      return parameters;
    }

    public void setParameters(List<StartRemoteProcessParameter> parameters) {
      this.parameters.clear();
      this.parameters.addAll(parameters);
      parametersViewer.refresh();
    }
  }

  private static class ParameterLabelProvider extends LabelProvider implements ITableLabelProvider {

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
      StartRemoteProcessParameter parameter = (StartRemoteProcessParameter) element;

      switch (columnIndex) {
        case 0:
          return parameter.getName();
        case 1:
          return parameter.getSource();
      }
      return null;
    }
  }

  private static class ParameterCellModifier implements ICellModifier {
    private TableViewer viewer;
    private String[] columnNames;

    private ParameterCellModifier(String[] columnNames, TableViewer viewer) {
      this.columnNames = columnNames;
      this.viewer = viewer;
    }

    @Override
    public boolean canModify(Object element, String property) {
      return true;
    }

    @Override
    public Object getValue(Object element, String property) {
      int columnIndex = Arrays.asList(columnNames).indexOf(property);
      StartRemoteProcessParameter parameter = (StartRemoteProcessParameter) element;

      switch (columnIndex) {
        case 0:
          return parameter.getName();
        case 1:
          return parameter.getSource();
      }
      return null;
    }

    @Override
    public void modify(Object element, String property, Object value) {
      int columnIndex = Arrays.asList(columnNames).indexOf(property);

      TableItem item = (TableItem) element;
      StartRemoteProcessParameter parameter = (StartRemoteProcessParameter) item.getData();

      switch (columnIndex) {
        case 0:
          parameter.setName(((String) value).trim());
          break;
        case 1:
          parameter.setSource(((String) value).trim());
          break;
      }

      viewer.update(parameter, null);
    }

  }

}
